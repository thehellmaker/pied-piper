package com.github.piedpiper.transformer;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.github.piedpiper.utils.SearchGraphUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class WarmupHandler implements Function<Void, Void> {

	private ILogger logger;
	private Injector injector;
	private Boolean isClearCache;

	public WarmupHandler(Injector injector, ILogger logger, Boolean isClearCache) {
		this.injector = injector;
		this.logger = logger;
		this.isClearCache = isClearCache;
	}

	@Override
	public Void apply(Void t) {
		try {
			refreshGraphCache();
			refreshSSMCache();
		} catch (JsonProcessingException | ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private void refreshSSMCache() throws InterruptedException, ExecutionException {
		ExecutorService executor = injector.getInstance(ExecutorService.class);
		LoadingCache<String, String> cacheLoader = injector
				.getInstance(Key.get(new TypeLiteral<LoadingCache<String, String>>() {
				}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		java.util.concurrent.Future<String> futureAccessKey = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return cacheLoader.get(PiedPiperConstants.DEFAULT_ACCESS_KEY);
			}

		});
		java.util.concurrent.Future<String> futureSecretKey = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return cacheLoader.get(PiedPiperConstants.DEFAULT_SECRET_KEY);
			}

		});
		futureAccessKey.get();
		futureSecretKey.get();
	}

	private void refreshGraphCache() throws JsonProcessingException, ExecutionException, InterruptedException {
		Map<String, JsonNode> graphCache = injector.getInstance(Key.get(new TypeLiteral<Map<String, JsonNode>>() {
		}, Names.named(PiedPiperConstants.GRAPH_CACHE)));
		if (!graphCache.isEmpty() && !isClearCache) return;
			
		DynamoDBReaderNode readerNode = (DynamoDBReaderNode) injector.getInstance(DynamoDBReaderNode.class);
		readerNode.setILogger(logger);
		readerNode.setInjector(injector);
		NodeOutput dynamoRecordOutput;

		dynamoRecordOutput = (NodeOutput) readerNode.apply(getSearchNodeInput());

		ArrayNode dynamoRecord = (ArrayNode) dynamoRecordOutput.getOutput();
		// Dynamo stores the graphJson as a string and hence returns a list of strings.
		// The below loop iterates through the
		// string graph jsons and converts them info json objects.
		for (JsonNode eachDynamoRecord : dynamoRecord) {
			JsonNode graphJson;
			try {
				graphJson = JsonUtils.mapper.readTree(eachDynamoRecord.get(PiedPiperConstants.GRAPH).asText());
				String projectName = graphJson.get(PiedPiperConstants.PROJECT_NAME).asText();
				String graphName = graphJson.get(PiedPiperConstants.GRAPH_NAME).asText();

				graphCache.put(SearchGraphUtils.getGraphCacheKey(projectName, graphName), graphJson);
			} catch (Exception e) {
				logger.log(ExceptionUtils.getStackTrace(e));
			}
		}

	}

	private NodeInput getSearchNodeInput() throws JsonProcessingException, ExecutionException, InterruptedException {
		LoadingCache<String, String> cacheLoader = injector
				.getInstance(Key.get(new TypeLiteral<LoadingCache<String, String>>() {
				}, Names.named(PiedPiperConstants.AWS_SSM_CACHE)));
		ExecutorService executor = injector.getInstance(ExecutorService.class);
		java.util.concurrent.Future<String> futureAccessKey = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return cacheLoader.get(PiedPiperConstants.DEFAULT_ACCESS_KEY);
			}

		});
		java.util.concurrent.Future<String> futureSecretKey = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return cacheLoader.get(PiedPiperConstants.DEFAULT_SECRET_KEY);
			}

		});
		String accessKey = futureAccessKey.get();
		String secretKey = futureSecretKey.get();
		String tableName = "AlmightyTable";

		ObjectNode queryNode = JsonUtils.mapper.createObjectNode();
		queryNode.set(AWSNode.ACCESS_KEY.getParameterName(), ParameterUtils.createParamValueNode(accessKey));
		queryNode.set(AWSNode.SECRET_KEY.getParameterName(), ParameterUtils.createParamValueNode(secretKey));
		queryNode.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(), ParameterUtils.createParamValueNode(tableName));
		queryNode.set(AWSNode.REGION.getParameterName(),
				ParameterUtils.createParamValueNode(Regions.US_EAST_1.getName()));

		augmentQueryNodeWithFilterQuery(queryNode);

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(queryNode);
		return nodeInput;
	}

	private void augmentQueryNodeWithFilterQuery(ObjectNode queryNode) {
		String keyQuery = PiedPiperConstants.HASH_KEY_QUERY;
		queryNode.set(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName(),
				ParameterUtils.createParamValueNode(keyQuery));
		queryNode.set(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY),
				ParameterUtils.createParamValueNode(PiedPiperConstants.GRAPH));

	}

}
