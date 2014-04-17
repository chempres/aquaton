package net.aquaton;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class AuthVerticle extends Verticle {

	@Override
	public void start() {
		vertx.eventBus().registerHandler("aquaton.login",
				new Handler<Message<String>>() {

					@Override
					public void handle(final Message<String> loginEvent) {
						final LoginRequest request = LoginRequest
								.fromString(loginEvent.body());
						JsonObject loginRequest = new JsonObject();
						loginRequest.putString("username",
								request.getUsername());
						loginRequest.putString("password",
								request.getPassword());
						vertx.eventBus().send("vertx.basicauthmanager.login",
								new JsonObject(loginRequest.toString()),
								new Handler<Message<JsonObject>>() {
									@Override
									public void handle(
											final Message<JsonObject> event) {
										if (event.body().getString("status")
												.equals("ok")) {

											JsonObject criteria = new JsonObject();
											criteria.putString("username",
													request.getUsername());

											JsonObject newObject = new JsonObject();
											newObject
													.putObject(
															"$set",
															new JsonObject()
																	.putString(
																			"sessionID",
																			event.body()
																					.getString(
																							"sessionID")));
											JsonObject json = new JsonObject()
													.putString("collection",
															"users")
													.putString("action",
															"update")
													.putObject("criteria",
															criteria)
													.putObject("objNew",
															newObject);
											vertx.eventBus().send(
													"vertx.mongopersistor",
													json);
											vertx.eventBus()
													.registerHandler(
															"aquaton.user."
																	+ event.body()
																			.getString(
																					"sessionID")
																	+ ".logout",
															new Handler<Message<String>>() {

																@Override
																public void handle(
																		final Message<String> event1) {
																	vertx.eventBus()
																			.send("vertx.basicauthmanager.logout",
																					new JsonObject()
																							.putString(
																									"sessionID",
																									event.body()
																											.getString(
																													"sessionID")),
																					new Handler<Message<JsonObject>>() {

																						@Override
																						public void handle(
																								Message<JsonObject> event) {
																							event1.reply();

																						}
																					});
																}
															});
										}
										loginEvent.reply(event.body());
									}
								});
					}
				});
	}
}
