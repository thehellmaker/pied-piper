package com.github.piedpiper.transformer;

import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.graph.save.ISaveTypeHandler;
import com.github.piedpiper.graph.save.PostNewVersionGraphHandler;
import com.github.piedpiper.graph.save.PutAliasGraphHandler;
import com.github.piedpiper.graph.save.PostLatestGraphHandler;
import com.google.inject.Injector;

public class SaveGraphFunction implements Function<JsonNode, JsonNode> {

	private Injector injector;
	private ILogger logger;

	public SaveGraphFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
	}

	@Override
	public JsonNode apply(JsonNode inputJson) {
		try {
			String saveType = inputJson.get("saveType").asText();
			ISaveTypeHandler saveHandler = getSaveHandler(saveType);
			return saveHandler.apply(inputJson);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private ISaveTypeHandler getSaveHandler(String saveType) throws Exception {
		switch (saveType) {
		case "POST_LATEST":
			return injector.getInstance(PostLatestGraphHandler.class);
		case "POST_NEW_VERSION":
			return injector.getInstance(PostNewVersionGraphHandler.class);
		case "PUT_ALIAS":
			return injector.getInstance(PutAliasGraphHandler.class);
		default:
			throw new Exception(String.format("Unknown saveType:%s handler", saveType));
		}
	}



}
