package com.github.piedpiper.transformer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.GraphUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.github.piedpiper.utils.SearchGraphUtils;
import com.google.common.base.Joiner;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class SearchGraphFunction implements Function<JsonNode, JsonNode>  {

	public static final String EXACT_RANGE_QUERY = "hashKey = :hashKey and rangeKey = :rangeKey";

	public static final String PREFIX_RANGE_QUERY = "hashKey = :hashKey and begins_with(rangeKey, :rangeKey)";

	private Injector injector;

	private ILogger logger;

	public SearchGraphFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
	}



	@Override
	public JsonNode apply(JsonNode inputJson) {
		try {
			logger.log("Input: " + inputJson);
			DynamoDBReaderNode readerNode = (DynamoDBReaderNode) injector.getInstance(DynamoDBReaderNode.class);
			readerNode.setILogger(logger);
			ArrayNode dynamoRecord = (ArrayNode) readerNode.apply(getSearchNodeInput(inputJson)).getOutput();
			// Dynamo stores the graphJson as a string and hence returns a list of strings. The below loop iterates through the 
			// string graph jsons and converts them info json objects.
			for (JsonNode eachDynamoRecord : dynamoRecord) {
				JsonNode graphJson = JsonUtils.mapper.readTree(eachDynamoRecord.get(PiedPiperConstants.GRAPH).asText());
				((ObjectNode) eachDynamoRecord).set(PiedPiperConstants.GRAPH, graphJson);
			}
			return JsonUtils.mapper.valueToTree(dynamoRecord);
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private NodeInput getSearchNodeInput(JsonNode inputJson) throws JsonProcessingException, ExecutionException {
		LoadingCache<String, String> cacheLoader = injector.getInstance(
				Key.get(new TypeLiteral<LoadingCache<String, String>> () {}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		String accessKeyName = Optional.ofNullable(inputJson.get(AWSNode.ACCESS_KEY.getParameterName()))
				.map(accessKeyNode -> accessKeyNode.asText()).orElse(PiedPiperConstants.DEFAULT_ACCESS_KEY);
		String accessKey = cacheLoader.get(accessKeyName);
		String secretKeyName = Optional.ofNullable(inputJson.get(AWSNode.SECRET_KEY.getParameterName()))
				.map(accessKeyNode -> accessKeyNode.asText()).orElse(PiedPiperConstants.DEFAULT_SECRET_KEY);
		String secretKey = cacheLoader.get(secretKeyName);
		String tableName = inputJson.get(DynamoDBBaseNode.TABLE_NAME.getParameterName()).asText();

		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(),
				ParameterUtils.createParamValueNode(Regions.US_EAST_1.getName()));

		switch (getSearchType(inputJson)) {
		case SEARCH:
			augmentQueryNodeWithFilterQuery(queryNode, inputJson);
			break;
		case SCAN:
		case GET:
			augmentQueryNodeWithKeyQuery(queryNode, inputJson);
			break;
		}

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);
		return nodeInput;
	}

	private void augmentQueryNodeWithFilterQuery(ObjectNode queryNode, JsonNode inputJson) {
		List<String> filterList = Lists.newArrayList();
		String[] termList = StringUtils.split(SearchGraphUtils.getSearchTerm(inputJson), ' ');
		for (String term : termList) {
			String trimmedTerm = term.trim();
			filterList.add(String.format(PiedPiperConstants.CONTAINS_FILTER_FORMAT, PiedPiperConstants.GRAPH_INDEX,
					ParameterUtils.getDynamoParamPlaceHolderName(trimmedTerm)));
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(trimmedTerm),
					ParameterUtils.createParamValueNode(trimmedTerm));
		}

		String keyQuery = PiedPiperConstants.HASH_KEY_QUERY;
		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));
		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY),
				ParameterUtils.createParamValueNode(PiedPiperConstants.GRAPH));
		queryNode.set(DynamoDBReaderNode.FILTER_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(Joiner.on(PiedPiperConstants.OR_SEPARATOR).join(filterList)));

	}

	private void augmentQueryNodeWithKeyQuery(ObjectNode queryNode, JsonNode inputJson) {
		String projectName = SearchGraphUtils.getProjectName(inputJson);
		String graphName = SearchGraphUtils.getGraphName(inputJson);
		if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(graphName)) {
			String keyQuery = EXACT_RANGE_QUERY;
			queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
					ParameterUtils.createParamValueNode(keyQuery));
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY),
					ParameterUtils.createParamValueNode(PiedPiperConstants.GRAPH));
			String rangeKey = GraphUtils.getRangeKeyEquals(projectName, graphName);
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY),
					ParameterUtils.createParamValueNode(rangeKey));
		} else if (StringUtils.isNotBlank(projectName) && StringUtils.isBlank(graphName)) {
			String keyQuery = PREFIX_RANGE_QUERY;
			queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
					ParameterUtils.createParamValueNode(keyQuery));
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY),
					ParameterUtils.createParamValueNode(PiedPiperConstants.GRAPH));
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY),
					ParameterUtils.createParamValueNode(projectName));
		}
	}

	private SearchType getSearchType(JsonNode inputJson) {
		String projectName = SearchGraphUtils.getProjectName(inputJson);
		String graphName = SearchGraphUtils.getGraphName(inputJson);
		String searchTerm = SearchGraphUtils.getSearchTerm(inputJson);
		if ((StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(graphName))
				|| StringUtils.isNotBlank(projectName)) {
			return SearchType.GET;
		} else if (StringUtils.isNotBlank(searchTerm)) {
			return SearchType.SEARCH;
		} else {
			throw new IllegalArgumentException("Unknown search type");
		}
	}

	private enum SearchType {
		SEARCH, GET, SCAN
	}

}
