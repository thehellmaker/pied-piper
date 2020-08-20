package com.github.piedpiper.graph.save;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

public interface ISaveTypeHandler extends Function<JsonNode, JsonNode> {
	
}
