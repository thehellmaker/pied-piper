package com.github.piedpiper.utils;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.AliasType;
import com.github.piedpiper.graph.storage.FilterType;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;

public class SearchGraphUtils {

	public static String getGraphName(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.GRAPH_NAME))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static String getProjectName(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.PROJECT_NAME))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static Long getVersion(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.VERSION))
				.map(projectNameNode -> projectNameNode.asLong()).orElse(null);
	}

	public static AliasType getAlias(JsonNode inputJson) {
		try {
			return AliasType.valueOf(inputJson.get(PiedPiperConstants.ALIAS).asText());
		} catch(Exception e) {
			return null;
		}
	}

	public static SortType getSortType(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.SORT_TYPE))
				.map(projectNameNode -> projectNameNode.asText()).map(sortTypeStr -> SortType.valueOf(sortTypeStr))
				.orElse(SortType.Descending);
	}

	public static String getSearchTerm(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.SEARCH_TERM))
				.map(projectNameNode -> projectNameNode.asText()).orElse(null);
	}

	public static String getGraphCacheKey(String projectName, String graphName) {
		return String.format("%s/%s", projectName, graphName);
	}

	public static String getBranchName(JsonNode inputJson) {
		return Optional.ofNullable(inputJson).map(node -> node.get(PiedPiperConstants.BRANCH_NAME))
				.map(projectNameNode -> projectNameNode.asText()).orElse("master");
	}

	public static FilterType getFilterType(JsonNode inputJson) {
		try {
			return FilterType.valueOf(inputJson.get(PiedPiperConstants.FILTER_TYPE).asText());
		} catch(Exception e) {
			return FilterType.NONE;
		}
	}
}
