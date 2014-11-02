package net.aquaton;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public abstract class UserVerticle extends BusModBase {

	public UserVerticle() {
		super();
	}

	protected <T> void sendToUser(T dataRequest, String path, String username) {
		JsonObject criteria = new JsonObject();
		criteria.putString("username", username);
		JsonObject json = new JsonObject()
				.putString("collection", "users")
				.putString("action", "findone")
				.putObject("matcher", criteria);
		vertx.eventBus().send("vertx.mongopersistor", json,
				(Message<JsonObject> findSessionIdResponse) -> {
					if (findSessionIdResponse.body().getString("status").equals("ok")) {
						final JsonObject result = findSessionIdResponse.body().getObject("result");
						if (result != null) {
							vertx.eventBus().send(
									"vertx.basicauthmanager.authorise",
									new JsonObject()
											.putString("sessionID", result.getString("sessionID")),
									(Message<JsonObject> authorizeSessionIdResponse) -> {
										if (authorizeSessionIdResponse.body().getString("status").equals("ok")) {
											vertx.eventBus().send("aquaton.user." + result.getString("sessionID") + "." + path,
													dataRequest);
										}
									});
						}
					}

				});
	}

}