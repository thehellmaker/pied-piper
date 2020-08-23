package com.github.piedpiper.transformer;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.AliasType;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.PostNewVersionInput;
import com.github.piedpiper.graph.storage.dynamo.PutNewAliasInput;
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
			String saveType = Optional.ofNullable(inputJson.get("saveType")).map(saveTypeNode -> saveTypeNode.asText())
					.orElse("Unknown");
			return handleSaveType(saveType, inputJson);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private JsonNode handleSaveType(String saveType, JsonNode inputJson) throws Exception {
		switch (saveType) {
		case "POST_NEW_VERSION":
			return handlePostNewVersion(inputJson);
		case "PUT_ALIAS":
			return handlePutAlias(inputJson);
		default:
			throw new Exception(String.format("Unknown saveType:%s handler", saveType));
		}
	}

	private JsonNode handlePutAlias(JsonNode inputJson) throws Exception {
		String projectName = inputJson.get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH_NAME).asText();
		String branchName = inputJson.get(PiedPiperConstants.BRANCH_NAME).asText();
		Long version = inputJson.get(PiedPiperConstants.VERSION).asLong();
		String versionDescription = inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION).asText();
		AliasType alias = Optional.ofNullable(inputJson.get(PiedPiperConstants.ALIAS))
				.map(versionNode -> versionNode.asText()).map(aliasStr -> AliasType.valueOf(aliasStr)).orElse(null);

		return this.graphStorage
				.putAlias(new PutNewAliasInput(projectName, graphName, branchName, version, alias, versionDescription));
	}

	private JsonNode handlePostNewVersion(JsonNode inputJson) throws Exception {
		String projectName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.PROJECT_NAME).asText();
		String graphName = inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.GRAPH_NAME).asText();
		String graphJson = inputJson.get(PiedPiperConstants.GRAPH).toString();
		String branchName = inputJson.get(PiedPiperConstants.BRANCH_NAME).asText();
		String previousProjectName = Optional.ofNullable(inputJson.get(PiedPiperConstants.PREVIOUS_PROJECT_NAME))
				.map(dataNode -> dataNode.asText()).orElse(null);
		String previousGraphName = Optional.ofNullable(inputJson.get(PiedPiperConstants.PREVIOUS_GRAPH_NAME))
				.map(dataNode -> dataNode.asText()).orElse(null);
		String previousBranchName = Optional.ofNullable(inputJson.get(PiedPiperConstants.PREVIOUS_BRANCH_NAME))
				.map(dataNode -> dataNode.asText()).orElse(null);
		Long previousVersion = Optional.ofNullable(inputJson.get(PiedPiperConstants.PREVIOUS_VERSION))
				.map(dataNode -> dataNode.asLong()).orElse(null);
		String versionDescription = Optional.ofNullable(inputJson.get(PiedPiperConstants.VERSION_DESCRIPTION))
				.map(dataNode -> dataNode.asText()).orElse(null);
		return this.graphStorage.postNewVersion(new PostNewVersionInput(projectName, graphName, graphJson, branchName,
				versionDescription, previousProjectName, previousGraphName, previousBranchName, previousVersion));
	}

}
