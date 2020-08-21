package com.github.piedpiper.transformer;

import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.storage.VersionType;
import com.github.piedpiper.utils.SearchGraphUtils;
import com.google.inject.Injector;

public class SearchGraphFunction implements Function<JsonNode, JsonNode> {

	private Injector injector;

	private ILogger logger;

	private IGraphStorage graphStorage;

	public SearchGraphFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
		this.graphStorage = this.injector.getInstance(IGraphStorage.class);
	}

	@Override
	public JsonNode apply(JsonNode inputJson) {
		try {
			String projectName = SearchGraphUtils.getProjectName(inputJson);
			String graphName = SearchGraphUtils.getGraphName(inputJson);
			VersionType versionType = SearchGraphUtils.getVersionType(inputJson);
			String alias = SearchGraphUtils.getAlias(inputJson);
			Long version = SearchGraphUtils.getVersion(inputJson);
			SortType sortType = SearchGraphUtils.getSortType(inputJson);
			return graphStorage.search(projectName, graphName, versionType, alias, version, sortType);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

}
