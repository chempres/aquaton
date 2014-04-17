package net.aquaton;

import java.io.Serializable;

import org.vertx.java.core.json.impl.Json;

public class Data implements Serializable {

	private static final long serialVersionUID = 4940360603663840126L;

	private String username;
	private String humidity;
	private String temperature;
	private String parcel;

	public Data() {
		super();
	}

	public Data(String username, String parcel, String humidity,
			String temperature) {
		super();
		this.username = username;
		this.parcel = parcel;
		this.humidity = humidity;
		this.temperature = temperature;
	}

	public String getHumidity() {
		return humidity;
	}

	public String getTemperature() {
		return temperature;
	}

	public String getParcel() {
		return parcel;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return Json.encodePrettily(this);
	}

	public static Data fromString(String json) {
		return Json.decodeValue(json, Data.class);
	}

}
