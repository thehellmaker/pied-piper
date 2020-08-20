package com.github.piedpiper.graph.save;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.IGraphStorage;

public class PostLatestGraphHandler implements ISaveTypeHandler {

	private IGraphStorage graphStorage;

	public PostLatestGraphHandler(IGraphStorage graphStorage) {
		this.graphStorage = graphStorage;
	}

	@Override
	public JsonNode apply(JsonNode inputJson) {
		String projectName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.GRAPH_NAME).asText();
		String graphJson = inputJson.get(PiedPiperConstants.GRAPH).asText();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();
		
		return this.graphStorage.postStagingVersion(projectName, graphName, graphJson, versionDescription);
	}
	
}
