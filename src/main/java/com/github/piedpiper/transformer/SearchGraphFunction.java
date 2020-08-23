package com.github.piedpiper.transformer;

import java.util.function.Function;

import javax.naming.directory.InvalidSearchFilterException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.graph.AliasType;
import com.github.piedpiper.graph.storage.FilterType;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.QueryGraphInput;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.storage.UniqueGraphFilter;
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
			String branchName = SearchGraphUtils.getBranchName(inputJson);
			AliasType aliasType = SearchGraphUtils.getAlias(inputJson);
			Long version = SearchGraphUtils.getVersion(inputJson);
			SortType sortType = SearchGraphUtils.getSortType(inputJson);
			
			ArrayNode result;
			if(aliasType == null ) {
				result = graphStorage.search(new QueryGraphInput(projectName, graphName, branchName, version, sortType));
			} else {
				result = graphStorage.search(new QueryGraphInput(projectName, graphName, branchName, aliasType, sortType));
			}
			FilterType filterType = SearchGraphUtils.getFilterType(inputJson); 
			Function<ArrayNode, ArrayNode> searchFilter = getFilter(filterType);
			System.out.println(result);
			return searchFilter.apply(result);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private Function<ArrayNode, ArrayNode> getFilter(FilterType filterType) {
		switch(filterType) {
		case UNIQUE_GRAPH_LIST_FILTER:
			return new UniqueGraphFilter();
		case UNIQUE_GRAPH_BRANCH_LIST_FILTER:
			return new UniqueGraphBranchFilter();
		case NONE:
		default:
			return new Function<ArrayNode, ArrayNode>() {
				@Override
				public ArrayNode apply(ArrayNode input) {
					return input;
				}
			};
		}
	}

}
