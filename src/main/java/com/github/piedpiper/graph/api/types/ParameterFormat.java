package com.github.piedpiper.graph.api.types;

public enum ParameterFormat {
	
	TEXT("ace/mode/text"), JAVASCRIPT("ace/mode/javascript"), JSON("ace/mode/json");
	
	private String aceViewValue;
	
	private ParameterFormat(String aceViewValue) {
		this.aceViewValue = aceViewValue;
	}
	
	public String getAceViewValue() {
		return aceViewValue;
	}
	
}
