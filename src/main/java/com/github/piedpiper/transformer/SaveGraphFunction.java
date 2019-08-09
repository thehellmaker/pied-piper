package com.github.piedpiper.transformer;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.GraphUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBWriterNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

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
			DynamoDBWriterNode writerNode = injector.getInstance(DynamoDBWriterNode.class);
			writerNode.setILogger(logger);
			return JsonUtils.mapper.valueToTree(writerNode.apply(getNodeInput(inputJson)));
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private NodeInput getNodeInput(JsonNode inputJson) throws JsonProcessingException, ExecutionException {
		LoadingCache<String, String> cacheLoader = injector.getInstance(
				Key.get(new TypeLiteral<LoadingCache<String, String>> () {}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		String accessKey = cacheLoader.get("AccessKey");
		String secretKey = cacheLoader.get("SecretKey");
		String tableName = inputJson.get(DynamoDBBaseNode.TABLE_NAME.getParameterName()).asText();
		String hashKey = PiedPiperConstants.GRAPH;
		String rangeKey = GraphUtils.getRangeKeyEquals(
				inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.PROJECT_NAME).asText(),
				inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.GRAPH_NAME).asText());

		ObjectNode inputNodeJsonNode = JsonUtils.mapper.createObjectNode();
		inputNodeJsonNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		inputNodeJsonNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		inputNodeJsonNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		inputNodeJsonNode.set(AWSNode.REGION.getParameterName(),
				ParameterUtils.createParamValueNode(Regions.US_EAST_1.getName()));
		inputNodeJsonNode.set(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY, ParameterUtils.createParamValueNode(hashKey));
		inputNodeJsonNode.set(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY, ParameterUtils.createParamValueNode(rangeKey));
		inputNodeJsonNode.set(PiedPiperConstants.GRAPH,
				ParameterUtils.createParamValueNode(inputJson.get(PiedPiperConstants.GRAPH).toString()));
		inputNodeJsonNode.set(PiedPiperConstants.GRAPH_INDEX,
				ParameterUtils.createParamValueNode(inputJson.get(PiedPiperConstants.GRAPH).toString().toLowerCase()));
		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(inputNodeJsonNode);
		return nodeInput;
	}

}
