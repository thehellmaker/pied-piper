package com.github.piedpiper.transformer;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
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
			String method = Optional.ofNullable(inputJson).map(methodInput->methodInput.get("method")).map(value->value.asText()).orElse("Save");
			switch(method) {
			case("PublishVersion"):
				return JsonUtils.mapper.valueToTree(writerNode.apply(getPublishVersionNodeInput(inputJson)));
			case("PublishVersionToAlias"):
				return JsonUtils.mapper.valueToTree(writerNode.apply(getPublishVersionToAliasNodeInput(inputJson)));
			case("Save"):
				return JsonUtils.mapper.valueToTree(writerNode.apply(getSaveNodeInput(inputJson)));
			default:
				throw new IllegalArgumentException(String.format("Unsupported Method = %s", method));
			}
			
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}
	
	//no completed yet
	private NodeInput getPublishVersionNodeInput(JsonNode inputJson) throws ExecutionException {
		LoadingCache<String, String> cacheLoader = injector.getInstance(
				Key.get(new TypeLiteral<LoadingCache<String, String>> () {}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		String accessKey = cacheLoader.get("AccessKey");
		String secretKey = cacheLoader.get("SecretKey");
		String hashKey = GraphUtils.getRangeKeyEquals(
				inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.PROJECT_NAME).asText(),
				inputJson.get(PiedPiperConstants.GRAPH).get(PiedPiperConstants.GRAPH_NAME).asText());
		String tableName = inputJson.get(DynamoDBBaseNode.TABLE_NAME.getParameterName()).asText();
		
		String keyQuery = PiedPiperConstants.VERSION_QUERY;
		
		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(),
				ParameterUtils.createParamValueNode(Regions.US_EAST_1.getName()));
		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));
		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY),
				ParameterUtils.createParamValueNode(hashKey));
		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY),
				ParameterUtils.createParamValueNode(0));

		DynamoDBReaderNode readerNode = (DynamoDBReaderNode) injector.getInstance(DynamoDBReaderNode.class);
		readerNode.setILogger(logger);
		readerNode.setInjector(injector);
		NodeOutput dynamoRecordOutput;
		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);

		dynamoRecordOutput = (NodeOutput) readerNode.apply(nodeInput);

		ArrayNode dynamoRecord = (ArrayNode) dynamoRecordOutput.getOutput();
		NodeInput Input = new NodeInput();
		return Input;

	}
	private NodeInput getPublishVersionToAliasNodeInput(JsonNode inputJson) throws ExecutionException {
		LoadingCache<String, String> cacheLoader = injector.getInstance(
				Key.get(new TypeLiteral<LoadingCache<String, String>> () {}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		String accessKey = cacheLoader.get("AccessKey");
		String secretKey = cacheLoader.get("SecretKey");

		String tableName = inputJson.get(DynamoDBBaseNode.TABLE_NAME.getParameterName()).asText();
		String rangeKey = inputJson.get("alias").asText();
		String hashKey = inputJson.get("graphName").asText();
		int version = inputJson.get(PiedPiperConstants.VERSION).asInt();
		
		ObjectNode inputNodeJsonNode = JsonUtils.mapper.createObjectNode();
		inputNodeJsonNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		inputNodeJsonNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		inputNodeJsonNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		inputNodeJsonNode.set(AWSNode.REGION.getParameterName(),
				ParameterUtils.createParamValueNode(Regions.US_EAST_1.getName()));
		inputNodeJsonNode.set(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY, ParameterUtils.createParamValueNode(hashKey));
		inputNodeJsonNode.set(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY, ParameterUtils.createParamValueNode(rangeKey));
		inputNodeJsonNode.set(PiedPiperConstants.VERSION, ParameterUtils.createParamValueNode(version));
		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(inputNodeJsonNode);
		return nodeInput;

		
	}
	
	
	
	private NodeInput getSaveNodeInput(JsonNode inputJson) throws JsonProcessingException, ExecutionException {
		LoadingCache<String, String> cacheLoader = injector.getInstance(
				Key.get(new TypeLiteral<LoadingCache<String, String>> () {}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		String accessKey = cacheLoader.get("AccessKey");
		String secretKey = cacheLoader.get("SecretKey");
		String tableName = inputJson.get(DynamoDBBaseNode.TABLE_NAME.getParameterName()).asText();
		int rangeKey = 0;
		String hashKey = GraphUtils.getRangeKeyEquals(
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
