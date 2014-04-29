package net.aquaton;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class DataReceiverVerticle extends UserVerticle {

	@Override
	public void start() {

		vertx.eventBus().registerHandler(
				"aquaton.data",
				(Message<String> dataRequest) -> {
					final Data data = Data.fromString(dataRequest.body());
					JsonObject criteria = new JsonObject();
					criteria.putObject("parcels", new JsonObject()
							.putObject("$elemMatch", new JsonObject().putString("name", data.getParcel())));
					JsonObject newObject = new JsonObject();
					newObject.putObject("$push", new JsonObject()
							.putObject("parcels.$.parameters", new JsonObject().putString("temperature", data.getTemperature())
									.putString("humidity", data.getHumidity())));
					JsonObject json = new JsonObject()
							.putString("collection", "users")
							.putString("action", "update")
							.putObject("criteria", criteria)
							.putBoolean("upsert", true)
							.putObject("objNew", newObject);
					vertx.eventBus().send("vertx.mongopersistor", json);
					sendToUser(dataRequest.body(), "parcel.data", data.getUsername());
				});
	}
}
