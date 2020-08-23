package com.github.piedpiper.transformer;

import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.google.api.client.util.Sets;

public class UniqueGraphBranchFilter implements Function<ArrayNode, ArrayNode> {

	@Override
	public ArrayNode apply(ArrayNode searchResults) {
		ArrayNode filteredResults = JsonUtils.mapper.createArrayNode();
		Set<String> uniqueProjectGraphNameSet = Sets.newHashSet();
		if(searchResults == null || searchResults.size() == 0) return filteredResults;
		for(JsonNode result: searchResults) {
			String projectName = result.get(PiedPiperConstants.GRAPH_JSON).get(PiedPiperConstants.PROJECT_NAME).asText();
			String graphName = result.get(PiedPiperConstants.GRAPH_JSON).get(PiedPiperConstants.GRAPH_NAME).asText();
			String branchName = result.get(PiedPiperConstants.BRANCH_NAME).asText();
			String projectGraphBranchName = projectName + "-" + graphName + "-" + branchName;
			
			if(!uniqueProjectGraphNameSet.contains(projectGraphBranchName)) {
				uniqueProjectGraphNameSet.add(projectGraphBranchName);
				ObjectNode newResult = JsonUtils.mapper.createObjectNode();
				newResult.put(PiedPiperConstants.PROJECT_NAME, projectName);
				newResult.put(PiedPiperConstants.GRAPH_NAME, graphName);
				newResult.put(PiedPiperConstants.BRANCH_NAME, branchName);
				filteredResults.add(newResult);
			}
		}
		return filteredResults;
	}

}
