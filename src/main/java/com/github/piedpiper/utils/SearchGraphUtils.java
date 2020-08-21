package com.github.piedpiper.utils;

import java.util.Optional;

import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.storage.VersionType;
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

	public static VersionType getVersionType(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.VERSION_TYPE))
				.map(projectNameNode -> projectNameNode.asText())
				.map(versionTypeStr -> VersionType.valueOf(versionTypeStr)).orElse(null);
	}
	
	public static Long getVersion(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.VERSION))
				.map(projectNameNode -> projectNameNode.asLong()).orElse(null);
	}
	
	public static String getAlias(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.ALIAS))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}
	
	public static SortType getSortType(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.SORT_TYPE))
				.map(projectNameNode -> projectNameNode.asText())
				.map(sortTypeStr -> SortType.valueOf(sortTypeStr)).orElse(SortType.Descending);
	}

	public static String getSearchTerm(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.SEARCH_TERM))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static String getGraphCacheKey(String projectName, String graphName) {
		return String.format("%s/%s", projectName, graphName);
	}
}
