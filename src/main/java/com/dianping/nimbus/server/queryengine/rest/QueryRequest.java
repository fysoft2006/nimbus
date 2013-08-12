package com.dianping.nimbus.server.queryengine.rest;

public class QueryRequest {
	private String id;
	private String sql;
	private String mode;
	private String database;
	private String username;
	private String password;
	private boolean storeResult;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isStoreResult() {
		return storeResult;
	}

	public void setStoreResult(boolean storeResult) {
		this.storeResult = storeResult;
	}
}
