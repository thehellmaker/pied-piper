package com.github.piedpiper.graph.storage.dynamo;

import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.AliasType;
import com.github.piedpiper.graph.storage.PutGraphVersionInput;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBWriterNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DynamoDBPutGraphVersionFunction implements Function<PutGraphVersionInput, JsonNode> {

	private static final String PUT_IF_ABSENT_CONDITION_EXPRESSION = String.format(
			"attribute_not_exists(%s) and attribute_not_exists(%s)",
			DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_HASH_KEY_NAME,
			DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME);

	private DynamoDBWriterNode writerNode;
	private LoadingCache<String, String> cacheLoader;
	private JsonNode globalConstants;

	@Inject
	public DynamoDBPutGraphVersionFunction(DynamoDBWriterNode writerNode,
			@Named(PiedPiperConstants.AWS_SSM_CACHE) LoadingCache<String, String> cacheLoader,
			@Named(PiedPiperConstants.GLOBAL_CONFIG) JsonNode globalConstants) {
		this.writerNode = writerNode;
		this.cacheLoader = cacheLoader;
		this.globalConstants = globalConstants;
	}

	@Override
	public JsonNode apply(PutGraphVersionInput input) {
		try {
			return JsonUtils.mapper.valueToTree(writerNode.apply(getNodeInput(input)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private NodeInput getNodeInput(PutGraphVersionInput input) throws JsonProcessingException, ExecutionException {
		String accessKey = cacheLoader.get(PiedPiperConstants.ACCESS_KEY);
		String secretKey = cacheLoader.get(PiedPiperConstants.SECRET_KEY);

		String tableName = globalConstants.get(PiedPiperConstants.GRAPH_VERSION_TABLE_NAME).asText();
		String region = globalConstants.get(PiedPiperConstants.REGION).asText();

		String hashKey = String.format("%s_%s", input.getProjectName(), input.getGraphName());

		String rangeKey = String.format(DynamoDBGraphStorageImpl.GRAPH_BRANCH_VERSION_RANGE_KEY_PATTERN,
				input.getBranchName(), input.getVersion());

		ObjectNode inputNodeJsonNode = JsonUtils.mapper.createObjectNode();
		inputNodeJsonNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		inputNodeJsonNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));

		inputNodeJsonNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(),
				ParameterUtils.createParamValueNode(tableName));

		inputNodeJsonNode.set(AWSNode.REGION.getParameterName(), ParameterUtils.createParamValueNode(region));

		inputNodeJsonNode.set(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_HASH_KEY_NAME,
				ParameterUtils.createParamValueNode(hashKey));
		inputNodeJsonNode.set(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME,
				ParameterUtils.createParamValueNode(rangeKey));
		inputNodeJsonNode.set(PiedPiperConstants.GRAPH, ParameterUtils.createParamValueNode(input.getGraphJson()));

		if (isNotValidAlias(input.getVersion())) {
			inputNodeJsonNode.set(DynamoDBWriterNode.CONDITION_EXPRESSION.getParameterName(),
					ParameterUtils.createParamValueNode(PUT_IF_ABSENT_CONDITION_EXPRESSION));
		}

		if (MapUtils.isNotEmpty(input.getAttributes())) {
			for (Entry<String, Object> attribute : input.getAttributes().entrySet()) {
				inputNodeJsonNode.set(attribute.getKey(), ParameterUtils.createParamValueNode(attribute.getValue()));
			}
		}

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(inputNodeJsonNode);
		return nodeInput;
	}

	private boolean isNotValidAlias(String aliasType) {
		try {
			AliasType.valueOf(aliasType);
			return false;
		} catch (Exception e) {
			return true;
		}
	}

}
