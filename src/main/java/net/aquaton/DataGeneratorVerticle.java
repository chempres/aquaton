package net.aquaton;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class DataGeneratorVerticle extends Verticle {

	private int counter = 0;

	@Override
	public void start() {
		vertx.setPeriodic(10000, (timeEvent) -> {
			JsonObject find = new JsonObject();
			find.putString("action", "find");
			find.putString("collection", "users");
			find.putObject("matcher", new JsonObject().putString("username", "john"));
			Handler<Message<JsonObject>> searchResponseHandler = (event) -> {
				JsonArray results = event.body().getArray("results");
				for (int i = 0; i < results.size(); i++) {
					JsonObject user = results.get(i);
					JsonArray parcels = user.getArray("parcels");
					if (parcels != null) {
						for (int j = 0; j < parcels.size(); j++) {
							JsonObject parcel = parcels.get(j);
							vertx.eventBus().send("aquaton.data",
									new Data(user.getString("username"), parcel.getString("name"), "HUMIDIY-" + counter,
											"TEMP-" + counter).toString());
							counter++;
						}
					}

				}

			};
			vertx.eventBus().send("vertx.mongopersistor", find, searchResponseHandler);
		});

	}
}
