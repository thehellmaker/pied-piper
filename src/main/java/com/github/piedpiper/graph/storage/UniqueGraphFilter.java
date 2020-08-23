package com.github.piedpiper.graph.storage;

import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.google.api.client.util.Sets;

public class UniqueGraphFilter implements Function<ArrayNode, ArrayNode>{

	@Override
	public ArrayNode apply(ArrayNode searchResults) {
		ArrayNode filteredResults = JsonUtils.mapper.createArrayNode();
		Set<String> uniqueProjectGraphNameSet = Sets.newHashSet();
		if(searchResults == null || searchResults.size() == 0) return filteredResults;
		for(JsonNode result: searchResults) {
			System.out.println(result.toString());
			String projectName = result.get(PiedPiperConstants.GRAPH_JSON).get(PiedPiperConstants.PROJECT_NAME).asText();
			String graphName = result.get(PiedPiperConstants.GRAPH_JSON).get(PiedPiperConstants.GRAPH_NAME).asText();
			String projectGraphName = projectName + "-" + graphName;
			
			if(!uniqueProjectGraphNameSet.contains(projectGraphName)) {
				uniqueProjectGraphNameSet.add(projectGraphName);
				ObjectNode newResult = JsonUtils.mapper.createObjectNode();
				newResult.put(PiedPiperConstants.PROJECT_NAME, projectName);
				newResult.put(PiedPiperConstants.GRAPH_NAME, graphName);
				filteredResults.add(newResult);
			}
		}
		return filteredResults;
	}

}
