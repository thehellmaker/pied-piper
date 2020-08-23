package com.github.piedpiper.graph.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.storage.dynamo.PutNewAliasInput;

public interface IGraphStorage {

	JsonNode postNewVersion(PostNewVersionInput input) throws Exception;
	
	ArrayNode search(QueryGraphInput queryGraphInput) throws Exception;

	JsonNode putAlias(PutNewAliasInput input) throws Exception;

}
