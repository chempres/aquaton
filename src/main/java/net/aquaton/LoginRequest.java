package net.aquaton;

import org.vertx.java.core.json.impl.Json;

public class LoginRequest {

	private String username;
	private String password;

	public LoginRequest(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public LoginRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return Json.encodePrettily(this);
	}

	public static LoginRequest fromString(String json) {
		return Json.decodeValue(json, LoginRequest.class);
	}

}
