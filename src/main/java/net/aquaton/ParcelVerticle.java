package net.aquaton;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class ParcelVerticle extends BusModBase {

	@Override
	public void start() {
		super.start();
		eb.registerHandler("aquaton.user.parcel.create",
				new Handler<Message<String>>() {

					@Override
					public void handle(final Message<String> createParcel) {
						final JsonObject parcel = new JsonObject(createParcel
								.body());
						JsonObject criteria = new JsonObject();
						criteria.putString("username",
								parcel.getString("username"));
						JsonArray coordinates = parcel.getArray("coordinates");
						JsonArray convertedCords = new JsonArray();
						for (int i = 0; i < coordinates.size(); i++) {
							double longitude = Double
									.valueOf(((JsonArray) coordinates.get(i))
											.get(0).toString());
							double latitude = Double
									.valueOf(((JsonArray) coordinates.get(i))
											.get(1).toString());
							convertedCords.addArray(new JsonArray(new Double[] {
									longitude, latitude }));
						}
						JsonObject newObject = new JsonObject();
						newObject.putObject(
								"$addToSet",
								new JsonObject().putObject(
										"parcels",
										new JsonObject()
												.putString(
														"name",
														parcel.getString("name"))
												.putObject(
														"location",
														new JsonObject()

																.putString(
																		"type",
																		"Polygon")
																.putArray(
																		"coordinates",
																		new JsonArray()
																				.add(convertedCords)))));

						JsonObject json = new JsonObject()
								.putString("collection", "users")
								.putString("action", "update")
								.putObject("criteria", criteria)
								.putBoolean("upsert", true)
								.putObject("objNew", newObject);
						eb.send("vertx.mongopersistor", json,
								new Handler<Message<JsonObject>>() {

									@Override
									public void handle(Message<JsonObject> event) {
										event.body().putObject("created",
												parcel);
										createParcel.reply(event.body()
												.toString());

									}
								});
					}

				});

		eb.registerHandler("aquaton.user.parcel.get",
				new Handler<Message<String>>() {

					@Override
					public void handle(final Message<String> getParcel) {
						JsonObject parcel = new JsonObject(getParcel.body());
						JsonObject criteria = new JsonObject();
						criteria.putString("username",
								parcel.getString("username"));

						JsonObject json = new JsonObject()
								.putString("collection", "users")
								.putString("action", "findone")
								.putObject("matcher", criteria);
						eb.send("vertx.mongopersistor", json,
								new Handler<Message<JsonObject>>() {

									@Override
									public void handle(Message<JsonObject> event) {
										getParcel
												.reply(event.body().toString());

									}
								});
					}
				});
	}
}
