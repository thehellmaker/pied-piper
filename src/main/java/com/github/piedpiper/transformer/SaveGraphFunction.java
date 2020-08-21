package com.github.piedpiper.transformer;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.google.inject.Injector;

public class SaveGraphFunction implements Function<JsonNode, JsonNode> {

	private Injector injector;

	private ILogger logger;

	private IGraphStorage graphStorage;

	public SaveGraphFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
		this.graphStorage = this.injector.getInstance(IGraphStorage.class);
	}

	@Override
	public JsonNode apply(JsonNode inputJson) {
		try {
			String saveType = inputJson.get("saveType").asText();
			return handleSaveType(saveType, inputJson);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private JsonNode handleSaveType(String saveType, JsonNode inputJson) throws Exception {
		switch (saveType) {
		case "POST_STAGING_VERSION":
			return handlePostNewStagingVersionGraph(inputJson);
		case "POST_PUBLISHED_VERSION":
			return handlePostNewPublishedVersionGraph(inputJson);
		case "POST_ALIAS_VERSION":
			return handlePostNewAliasVersionGraph(inputJson);
		default:
			throw new Exception(String.format("Unknown saveType:%s handler", saveType));
		}
	}

	private JsonNode handlePostNewAliasVersionGraph(JsonNode inputJson) {
		String projectName = inputJson.get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH_NAME).asText();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();
		Long version = Optional.ofNullable(inputJson.get(PiedPiperConstants.VERSION))
				.map(versionNode -> versionNode.asLong()).orElse(null);
		String alias = Optional.ofNullable(inputJson.get(PiedPiperConstants.ALIAS))
				.map(versionNode -> versionNode.asText()).orElse(null);

		return this.graphStorage.putAlias(projectName, graphName, alias, version, versionDescription);
	}

	private JsonNode handlePostNewPublishedVersionGraph(JsonNode inputJson) {
		String projectName = inputJson.get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH_NAME).asText();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();
		Long version = Optional.ofNullable(inputJson.get(PiedPiperConstants.VERSION))
				.map(versionNode -> versionNode.asLong()).orElse(null);

		return this.graphStorage.postNewVersion(projectName, graphName, version, versionDescription);
	}

	private JsonNode handlePostNewStagingVersionGraph(JsonNode inputJson) {
		String projectName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.GRAPH_NAME).asText();
		String graphJson = inputJson.get(PiedPiperConstants.GRAPH).asText();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();

		return this.graphStorage.postStagingVersion(projectName, graphName, graphJson, versionDescription);
	}

}
