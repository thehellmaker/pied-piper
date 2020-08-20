package com.github.piedpiper.graph.storage.dynamo;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.GetGraphVersionInput;
import com.github.piedpiper.graph.storage.VersionType;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.utils.DynamoDBUtils;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DynamoDBGetGraphVersionFunction implements Function<GetGraphVersionInput, JsonNode> {

	private DynamoDBReaderNode readerNode;
	private LoadingCache<String, String> cacheLoader;
	private JsonNode globalConstants;

	@Inject
	public DynamoDBGetGraphVersionFunction(DynamoDBReaderNode readerNode,
			@Named(PiedPiperConstants.AWS_SSM_CACHE) LoadingCache<String, String> cacheLoader,
			@Named(PiedPiperConstants.GLOBAL_CONFIG) JsonNode globalConstants) {
		this.readerNode = readerNode;
		this.cacheLoader = cacheLoader;
		this.globalConstants = globalConstants;
	}

	@Override
	public JsonNode apply(GetGraphVersionInput getGraphVersionInput) {
		try {
			ArrayNode dynamoRecord = (ArrayNode) readerNode
					.apply(getLatestGraphStagingQueryNodeInput(getGraphVersionInput.getProjectName(),
							getGraphVersionInput.getGraphName(), getGraphVersionInput.getVersionType(),
							getGraphVersionInput.getAliasName(), getGraphVersionInput.getVersion()))
					.getOutput();
			return dynamoRecord.isEmpty() ? null : dynamoRecord.get(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected NodeInput getLatestGraphStagingQueryNodeInput(String projectName, String graphName,
			VersionType versionType, String aliasName, Long version)
			throws JsonProcessingException, ExecutionException {
		String accessKey = cacheLoader.get(PiedPiperConstants.ACCESS_KEY);
		String secretKey = cacheLoader.get(PiedPiperConstants.SECRET_KEY);

		String tableName = globalConstants.get(PiedPiperConstants.GRAPH_VERSION_TABLE_NAME).asText();
		String region = globalConstants.get(PiedPiperConstants.REGION).asText();

		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(), ParameterUtils.createParamValueNode(region));

		if (version == null) {
			augmentQueryNodeWithRangeQuery(queryNode, versionType, aliasName, version);
		} else {
			augmentQueryNodeWithExactKeyQuery(queryNode, versionType, aliasName, version);
		}

		String hashKey = String.format("%s_%s", projectName, graphName);

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.HASH_KEY_PARAMETER),
				ParameterUtils.createParamValueNode(hashKey));

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);
		return nodeInput;
	}

	private void augmentQueryNodeWithExactKeyQuery(ObjectNode queryNode, VersionType versionType, String aliasName,
			Long version) {
		String keyQuery = DynamoDBGraphStorageImpl.GRAPH_EXACT_QUERY;

		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_PARAMETER),
				ParameterUtils.createParamValueNode(getExactQueryRangeKeyValue(versionType, aliasName, version)));
	}

	private Object getExactQueryRangeKeyValue(VersionType versionType, String aliasName, Long version) {
		String rangeVersion = DynamoDBUtils.formatWithLongMaxLength(version);
		if (versionType == VersionType.Alias) {
			return String.format(DynamoDBGraphStorageImpl.GRAPH_ALIAS_VERSION_TYPE_RANGE_KEY_PATTERN, aliasName,
					rangeVersion);
		} else {
			return String.format(DynamoDBGraphStorageImpl.GRAPH_VERSION_TYPE_RANGE_KEY_PATTERN, versionType.name(),
					rangeVersion);
		}
	}

	protected void augmentQueryNodeWithRangeQuery(ObjectNode queryNode, VersionType versionType, String aliasName,
			Long version) {
		String keyQuery = DynamoDBGraphStorageImpl.GRAPH_PREFIX_RANGE_QUERY;

		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_BEGIN_PARAMETER),
				ParameterUtils.createParamValueNode(getRangeQueryRangeKeyValue(versionType, aliasName, version, true)));

		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.RANGE_KEY_END_PARAMETER),
				ParameterUtils
						.createParamValueNode(getRangeQueryRangeKeyValue(versionType, aliasName, version, false)));

		queryNode.set(DynamoDBReaderNode.SCAN_INDEX_FORWARD.getParameterName(),
				ParameterUtils.createParamValueNode(Boolean.FALSE));
	}

	protected String getRangeQueryRangeKeyValue(VersionType versionType, String aliasName, Long version,
			Boolean isRangeBegin) {
		String rangeVersion = isRangeBegin == true ? DynamoDBUtils.formatWithLongMaxLength(0)
				: String.valueOf(Long.MAX_VALUE);
		if (versionType == VersionType.Alias) {
			return String.format(DynamoDBGraphStorageImpl.GRAPH_ALIAS_VERSION_TYPE_RANGE_KEY_PATTERN, aliasName,
					rangeVersion);
		} else {
			return String.format(DynamoDBGraphStorageImpl.GRAPH_VERSION_TYPE_RANGE_KEY_PATTERN, versionType.name(),
					rangeVersion);
		}
	}

}
