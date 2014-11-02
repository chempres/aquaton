package net.aquaton;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class AuthVerticle extends BusModBase {

	@Override
	public void start() {
		registerLogin();
	}

	private void registerLogin() {
		vertx.eventBus().registerHandler(
				"aquaton.user.login",
				(Message<JsonObject> loginEvent) -> {
					JsonObject loginRequest = new JsonObject();
					loginRequest.putString("username", loginEvent.body().getString("username"));
					loginRequest.putString("password", loginEvent.body().getString("password"));
					login(loginRequest, (Message<JsonObject> loginResponse) -> {
						if (loginResponse.body().getString("status").equals("ok")) {
							JsonObject criteria = new JsonObject();
							criteria.putString("username", loginEvent.body().getString("username"));
							JsonObject newObject = new JsonObject();
							newObject.putObject("$set", new JsonObject()
									.putString("sessionID", loginResponse.body().getString("sessionID")));
							JsonObject json = new JsonObject()
									.putString("collection", "users")
									.putString("action", "update")
									.putObject("criteria", criteria)
									.putObject("objNew", newObject);
							vertx.eventBus().send("vertx.mongopersistor", json);
							registerLogout(loginResponse);
						}
						loginEvent.reply(loginResponse.body());
					});
				});
	}

	private void registerLogout(Message<JsonObject> loginResponse) {
		vertx.eventBus().registerHandler(
				"aquaton.user.logout",
				(Message<String> logoutRequest) -> {
					logout(new JsonObject()
							.putString("sessionID", loginResponse.body().getString("sessionID")),
							(Message<JsonObject> logoutResponse) -> {
								logoutRequest.reply();
							});
				});
	}

	private void login(JsonObject loginRequest, Handler<Message<JsonObject>> handler) {
		vertx.eventBus().send("vertx.basicauthmanager.login", loginRequest, handler);
	}

	private void logout(JsonObject logoutRequest, Handler<Message<JsonObject>> handler) {
		vertx.eventBus().send("vertx.basicauthmanager.logout", logoutRequest, handler);
	}
}
