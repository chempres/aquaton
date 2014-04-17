package net.aquaton;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class DataReceiverVerticle extends Verticle {

	@Override
	public void start() {

		vertx.eventBus().registerHandler("aquaton.data",
				new Handler<Message<String>>() {
					@Override
					public void handle(final Message<String> event) {
						System.out.println("RECEIVED: " + event.body());
						final Data data = Data.fromString(event.body());
						JsonObject criteria = new JsonObject();
						criteria.putObject("parcels", new JsonObject()
								.putObject(
										"$elemMatch",
										new JsonObject().putString("name",
												data.getParcel())));
						JsonObject newObject = new JsonObject();
						newObject.putObject("$push", new JsonObject()
								.putObject(
										"parcels.$.parameters",
										new JsonObject().putString(
												"temperature",
												data.getTemperature())
												.putString("humidity",
														data.getHumidity())));
						JsonObject json = new JsonObject()
								.putString("collection", "users")
								.putString("action", "update")
								.putObject("criteria", criteria)
								.putBoolean("upsert", true)
								.putObject("objNew", newObject);
						vertx.eventBus().send("vertx.mongopersistor", json);

						criteria = new JsonObject();
						criteria.putString("username", data.getUsername());
						json = new JsonObject()
								.putString("collection", "users")
								.putString("action", "findone")
								.putObject("matcher", criteria);
						vertx.eventBus().send("vertx.mongopersistor", json,
								new Handler<Message<JsonObject>>() {

									@Override
									public void handle(
											Message<JsonObject> event2) {
										if (event2.body().getString("status")
												.equals("ok")) {
											final JsonObject result = event2
													.body().getObject("result");
											if (result != null) {
												vertx.eventBus()
														.send("vertx.basicauthmanager.authorise",
																new JsonObject()
																		.putString(
																				"sessionID",
																				result.getString("sessionID")),
																new Handler<Message<JsonObject>>() {

																	@Override
																	public void handle(
																			Message<JsonObject> event3) {
																		if (event3
																				.body()
																				.getString(
																						"status")
																				.equals("ok")) {
																			vertx.eventBus()
																					.send("aquaton.user."
																							+ result.getString("sessionID"),
																							event.body());
																		}

																	}
																});
											}
										}

									}
								});

					}
				});
	}
}
