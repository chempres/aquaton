package net.aquaton;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class AuthVerticle extends Verticle {

	@Override
	public void start() {
		vertx.eventBus().registerHandler(
				"aquaton.login",
				(Message<String> loginEvent) -> {
					final LoginRequest request = LoginRequest
							.fromString(loginEvent.body());
					JsonObject loginRequest = new JsonObject();
					loginRequest.putString("username",
							request.getUsername());
					loginRequest.putString("password",
							request.getPassword());
					vertx.eventBus().send(
							"vertx.basicauthmanager.login",
							new JsonObject(loginRequest.toString()),
							(Message<JsonObject> loginResponse) -> {
								if (loginResponse.body().getString("status").equals("ok")) {
									JsonObject criteria = new JsonObject();
									criteria.putString("username", request.getUsername());
									JsonObject newObject = new JsonObject();
									newObject.putObject("$set", new JsonObject()
											.putString("sessionID", loginResponse.body().getString("sessionID")));
									JsonObject json = new JsonObject()
											.putString("collection", "users")
											.putString("action", "update")
											.putObject("criteria", criteria)
											.putObject("objNew", newObject);
									vertx.eventBus().send("vertx.mongopersistor", json);
									vertx.eventBus().registerHandler(
											"aquaton.user." + loginResponse.body().getString("sessionID") + ".logout",
											(Message<String> logoutRequest) -> {
												vertx.eventBus().send("vertx.basicauthmanager.logout",
														new JsonObject()
																.putString("sessionID", loginResponse.body().getString("sessionID")),
														(Message<JsonObject> logoutResponse) -> {
															logoutRequest.reply();
														});
											});
								}
								loginEvent.reply(loginResponse.body());
							});
				});
	}
}
