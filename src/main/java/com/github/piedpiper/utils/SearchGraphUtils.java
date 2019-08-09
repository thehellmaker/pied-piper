package com.github.piedpiper.utils;

import java.util.Optional;

import com.github.piedpiper.common.PiedPiperConstants;
import com.fasterxml.jackson.databind.JsonNode;

public class SearchGraphUtils {

	public static String getGraphName(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.GRAPH_NAME))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static String getProjectName(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.PROJECT_NAME))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static String getSearchTerm(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.SEARCH_TERM))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}
	
	public static String getGraphCacheKey(String projectName, String graphName) {
		return String.format("%s/%s", projectName, graphName);
	}
}
