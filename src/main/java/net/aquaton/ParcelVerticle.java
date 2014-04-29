package net.aquaton;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class ParcelVerticle extends UserVerticle {

	@Override
	public void start() {
		super.start();
		eb.registerHandler("aquaton.user.parcel.create",
				(Message<String> createParcel) -> {
					final JsonObject parcel = new JsonObject(createParcel.body());
					JsonObject criteria = new JsonObject();
					criteria.putString("username", parcel.getString("username"));
					JsonArray coordinates = parcel.getArray("coordinates");
					JsonArray convertedCords = new JsonArray();
					for (int i = 0; i < coordinates.size(); i++) {
						double longitude = Double.valueOf(((JsonArray) coordinates.get(i)).get(0).toString());
						double latitude = Double.valueOf(((JsonArray) coordinates.get(i)).get(1).toString());
						convertedCords.addArray(new JsonArray(new Double[] { longitude, latitude }));
					}
					JsonObject newParcel = new JsonObject()
							.putString("name", parcel.getString("name"))
							.putObject("location",
									new JsonObject()
											.putString("type", "Polygon")
											.putArray("coordinates", new JsonArray().add(convertedCords)));
					JsonObject newObject = new JsonObject();
					newObject.putObject("$addToSet",
							new JsonObject()
									.putObject("parcels", newParcel));
					JsonObject json = new JsonObject()
							.putString("collection", "users")
							.putString("action", "update")
							.putObject("criteria", criteria)
							.putBoolean("upsert", true)
							.putObject("objNew", newObject);
					eb.send("vertx.mongopersistor", json,
							(Message<JsonObject> event) -> {
								publishEvent(EventType.CREATE, parcel.getString("username"), newParcel);
							});
				});

		eb.registerHandler("aquaton.user.parcel.list",
				(Message<String> getParcel) -> {
					JsonObject parcel = new JsonObject(getParcel.body());
					JsonObject criteria = new JsonObject();
					criteria.putString("username", parcel.getString("username"));
					JsonObject json = new JsonObject()
							.putString("collection", "users")
							.putString("action", "findone")
							.putObject("matcher", criteria);
					eb.send("vertx.mongopersistor", json,
							(Message<JsonObject> event) -> {
								getParcel.reply(event.body().toString());
							});
				});
		eb.registerHandler(
				"aquaton.user.parcel.delete",
				(Message<String> deleteRequest) -> {
					JsonObject parcel = new JsonObject(deleteRequest.body());
					JsonObject criteria = new JsonObject();
					criteria.putString("username",
							parcel.getString("username"));
					JsonObject update = new JsonObject();
					update.putObject("$pull",
							new JsonObject().putObject("parcels", new JsonObject().putString("name", parcel.getString("name"))));
					JsonObject json = new JsonObject()
							.putString("collection", "users")
							.putString("action", "update")
							.putObject("criteria", criteria)
							.putObject("objNew", update);
					eb.send("vertx.mongopersistor", json,
							(Message<JsonObject> deleteResponse) -> {
								publishEvent(EventType.DELETE, parcel.getString("username"), parcel.getString("name"));
							});
				});
	}

	private <T> void publishEvent(EventType eventType, String username, T parcel) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putValue("parcel", parcel);
		jsonObject.putString("event", eventType.toString());
		sendToUser(jsonObject.toString(), "parcel", username);
	}

	public enum EventType {
		CREATE,
		UPDATE,
		DELETE

	}
}
