package com.github.piedpiper.graph.api.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GraphInput {
	
	private JsonNode graphJson;
	
	private JsonNode inputJson;

	public GraphInput(JsonNode graphJson, JsonNode inputJson) {
		this.graphJson = graphJson;
		this.inputJson = inputJson;
	}

	public JsonNode getGraphJson() {
		return graphJson;
	}

	public JsonNode getInputJson() {
		return inputJson;
	}

}
