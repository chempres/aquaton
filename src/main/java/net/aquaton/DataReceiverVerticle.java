package net.aquaton;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class DataReceiverVerticle extends UserVerticle {

	@Override
	public void start() {

		vertx.eventBus().registerHandler(
				"aquaton.parcel.parameters",
				(Message<JsonObject> dataRequest) -> {
					JsonObject criteria = new JsonObject();
					criteria.putObject("parcels", new JsonObject()
							.putObject("$elemMatch", new JsonObject().putString("name", dataRequest.body().getString("parcel"))));
					JsonObject newObject = new JsonObject();
					newObject.putObject(
							"$push",
							new JsonObject()
									.putObject("parcels.$.parameters",
											new JsonObject().putString("temperature", dataRequest.body().getString("temperature"))
													.putString("humidity", dataRequest.body().getString("humidity"))));
					JsonObject json = new JsonObject()
							.putString("collection", "users")
							.putString("action", "update")
							.putObject("criteria", criteria)
							.putBoolean("upsert", true)
							.putObject("objNew", newObject);
					vertx.eventBus().send("vertx.mongopersistor", json);
					sendToUser(dataRequest.body(), "parcel.parameters", dataRequest.body().getString("username"));
				});
	}
}
