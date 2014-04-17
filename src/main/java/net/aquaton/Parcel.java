package net.aquaton;

import org.vertx.java.core.json.impl.Json;

public class Parcel {

	private String name;
	private int[][][] coordinates;

	public Parcel() {
		super();
	}

	public Parcel(String name, int[][][] coordinates) {
		super();
		this.name = name;
		this.coordinates = coordinates;
	}

	public String getName() {
		return name;
	}

	public int[][][] getCoordinates() {
		return coordinates;
	}

	@Override
	public String toString() {
		return Json.encodePrettily(this);
	}

	public static Parcel fromString(String json) {
		return Json.decodeValue(json, Parcel.class);
	}

}
