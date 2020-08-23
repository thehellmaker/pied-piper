package com.github.piedpiper.graph.storage.dynamo;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.AliasType;
import com.github.piedpiper.graph.storage.QueryGraphInput;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.utils.DynamoDBUtils;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DynamoDBQueryGraphFunction implements Function<QueryGraphInput, ArrayNode> {

	private DynamoDBReaderNode readerNode;
	private LoadingCache<String, String> cacheLoader;
	private JsonNode globalConstants;

	@Inject
	public DynamoDBQueryGraphFunction(DynamoDBReaderNode readerNode,
			@Named(PiedPiperConstants.AWS_SSM_CACHE) LoadingCache<String, String> cacheLoader,
			@Named(PiedPiperConstants.GLOBAL_CONFIG) JsonNode globalConstants) {
		this.readerNode = readerNode;
		this.cacheLoader = cacheLoader;
		this.globalConstants = globalConstants;
	}

	@Override
	public ArrayNode apply(QueryGraphInput queryGraphInput) {

		if (StringUtils.isBlank(queryGraphInput.getProjectName())) {
			throw new RuntimeException("Project name cannot be blank during search");
		}

		if (StringUtils.isBlank(queryGraphInput.getGraphName())) {
			return scan(queryGraphInput);
		}

		return query(queryGraphInput);
	}

	private ArrayNode scan(QueryGraphInput queryGraphInput) {
		try {
			ArrayNode dynamoRecord = (ArrayNode) readerNode.apply(scanGraphNodeInput(queryGraphInput)).getOutput();

			ArrayNode filteredRecords = JsonUtils.mapper.createArrayNode();
			for (JsonNode node : dynamoRecord) {
				if (isMatch(node, queryGraphInput)) {
					filteredRecords.add(node);
				}
			}

			return filteredRecords;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isMatch(JsonNode node, QueryGraphInput queryGraphInput) {
		String projectName = node.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_HASH_KEY_NAME).asText()
				.split("_")[0];

		String graphName = node.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_HASH_KEY_NAME).asText().split("_")[1];

		String branchName = node.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME).asText()
				.split("_")[0];

		boolean isMatch = projectName.equals(queryGraphInput.getProjectName())
				&& (queryGraphInput.getBranchName() == null || queryGraphInput.getBranchName().equals(branchName));
		return isMatch;
	}

	private NodeInput scanGraphNodeInput(QueryGraphInput queryGraphInput) throws ExecutionException {
		String accessKey = cacheLoader.get(PiedPiperConstants.ACCESS_KEY);
		String secretKey = cacheLoader.get(PiedPiperConstants.SECRET_KEY);

		String tableName = globalConstants.get(PiedPiperConstants.GRAPH_VERSION_TABLE_NAME).asText();
		String region = globalConstants.get(PiedPiperConstants.REGION).asText();

		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(), ParameterUtils.createParamValueNode(region));

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);
		return nodeInput;
	}

	protected ArrayNode query(QueryGraphInput queryGraphInput) {
		try {
			ArrayNode dynamoRecord = (ArrayNode) readerNode.apply(queryGraphNodeInput(queryGraphInput)).getOutput();
			return dynamoRecord;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected NodeInput queryGraphNodeInput(QueryGraphInput input) throws JsonProcessingException, ExecutionException {
		String accessKey = cacheLoader.get(PiedPiperConstants.ACCESS_KEY);
		String secretKey = cacheLoader.get(PiedPiperConstants.SECRET_KEY);

		String tableName = globalConstants.get(PiedPiperConstants.GRAPH_VERSION_TABLE_NAME).asText();
		String region = globalConstants.get(PiedPiperConstants.REGION).asText();

		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(), ParameterUtils.createParamValueNode(region));

		if (StringUtils.isBlank(input.getBranchName()) || (input.getVersion() == null && input.getAlias() == null) ) {
			augmentQueryNodeWithRangeQuery(queryNode, input.getBranchName(), input.getVersion(), input.getSortType());
		} else {
			System.out.println("Exact");
			augmentQueryNodeWithExactKeyQuery(queryNode, input.getBranchName(), input.getVersion(), input.getAlias());
		}

		String hashKey = String.format("%s_%s", input.getProjectName(), input.getGraphName());

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.HASH_KEY_PARAMETER),
				ParameterUtils.createParamValueNode(hashKey));

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);
		return nodeInput;
	}

	private void augmentQueryNodeWithExactKeyQuery(ObjectNode queryNode, String branchName, Long version, AliasType aliasType) {
		String keyQuery = DynamoDBGraphStorageImpl.GRAPH_EXACT_QUERY;

		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));
		if(aliasType == null) {
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_PARAMETER),
					ParameterUtils.createParamValueNode(getExactQueryRangeKeyValue(branchName, version)));
		} else {
			queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_PARAMETER),
					ParameterUtils.createParamValueNode(getExactQueryRangeKeyValue(branchName, aliasType.name())));
		}
		
	}

	private Object getExactQueryRangeKeyValue(String branchName, Long version) {
		String rangeVersion = DynamoDBUtils.formatWithLongMaxLength(version);
		return String.format(DynamoDBGraphStorageImpl.GRAPH_BRANCH_VERSION_RANGE_KEY_PATTERN, branchName, rangeVersion);
	}
	
	private Object getExactQueryRangeKeyValue(String branchName, String aliasType) {
		return String.format(DynamoDBGraphStorageImpl.GRAPH_BRANCH_VERSION_RANGE_KEY_PATTERN, branchName, aliasType);
	}

	protected void augmentQueryNodeWithRangeQuery(ObjectNode queryNode, String branchName, Long version,
			SortType sortType) {
		String keyQuery = DynamoDBGraphStorageImpl.GRAPH_PREFIX_RANGE_QUERY;

		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_BEGIN_PARAMETER),
				ParameterUtils.createParamValueNode(getRangeQueryRangeKeyValue(branchName, version, true)));
		
		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_END_PARAMETER),
				ParameterUtils.createParamValueNode(getRangeQueryRangeKeyValue(branchName, version, false)));

		Boolean scanIndexForward = sortType == SortType.Descending ? Boolean.FALSE : Boolean.TRUE;

		queryNode.set(DynamoDBReaderNode.SCAN_INDEX_FORWARD.getParameterName(),
				ParameterUtils.createParamValueNode(scanIndexForward));
	}

	protected String getRangeQueryRangeKeyValue(String branchName, Long version, Boolean isRangeBegin) {
		if (StringUtils.isBlank(branchName)) {
			return isRangeBegin ? "0000000000000000000" : "zzzzzzzzzzzzzzzzzzzzzz";
		} else {
			String rangeVersion = isRangeBegin == true ? DynamoDBUtils.formatWithLongMaxLength(0)
					: String.valueOf(Long.MAX_VALUE);
			return String.format(DynamoDBGraphStorageImpl.GRAPH_BRANCH_VERSION_RANGE_KEY_PATTERN, branchName,
					rangeVersion);
		}
	}

}
