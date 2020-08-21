package com.github.piedpiper.graph.storage.dynamo;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.piedpiper.graph.storage.QueryGraphInput;
import com.google.inject.Inject;

public class DynamoDBGetGraphFunction implements Function<QueryGraphInput, JsonNode>{

	private DynamoDBQueryGraphFunction queryGraphFunction;

	@Inject
	public DynamoDBGetGraphFunction(DynamoDBQueryGraphFunction queryGraphFunction) {
		this.queryGraphFunction = queryGraphFunction;
	}
	
	@Override
	public JsonNode apply(QueryGraphInput input) {
		ArrayNode resultArrayNode = queryGraphFunction.apply(input);
		return resultArrayNode.isEmpty() ? null : resultArrayNode.get(0);
	}

}
