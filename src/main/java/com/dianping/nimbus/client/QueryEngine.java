package com.dianping.nimbus.client;

public enum QueryEngine {
	SHARK("shark"), HIVE("hive");

	private final String engineName;

	private QueryEngine(String name) {
		engineName = name;
	}

	public String getName() {
		return engineName;
	}

	@Override
	public String toString() {
		return engineName;
	}
}
