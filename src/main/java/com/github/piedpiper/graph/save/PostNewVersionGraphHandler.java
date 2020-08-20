package com.github.piedpiper.graph.save;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.google.inject.Inject;

public class PostNewVersionGraphHandler implements ISaveTypeHandler {

	private IGraphStorage graphStorage;

	@Inject
	public PostNewVersionGraphHandler(IGraphStorage graphStorage) {
		this.graphStorage = graphStorage;
	}

	@Override
	public JsonNode apply(JsonNode inputJson) {
		String projectName = inputJson.get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH_NAME).asText();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();
		Long version = Optional.ofNullable(inputJson.get(PiedPiperConstants.VERSION))
				.map(versionNode -> versionNode.asLong()).orElse(null);

		return this.graphStorage.postNewVersion(projectName, graphName, version, versionDescription);

	}

}
