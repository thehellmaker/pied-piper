package com.github.piedpiper.storage;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.amazonaws.regions.Regions;
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
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DynamoDBStorage implements IGraphStorage{
	
	private Injector injector;
	private ILogger logger;
	
	@Override
	public void setInjector(Injector injector) {
		this.injector = injector;
	}
	
	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}
	
	
	@Override
	public NodeOutput getGraphs() throws InterruptedException, ExecutionException {
		DynamoDBReaderNode readerNode = injector.getInstance(DynamoDBReaderNode.class);
		readerNode.setILogger(this.logger);
		readerNode.setInjector(this.injector);
		NodeOutput dynamoRecordOutput = (NodeOutput) readerNode.apply(getSearchNodeInput());
		return dynamoRecordOutput;
	}
	
	
	private NodeInput getSearchNodeInput() throws InterruptedException, ExecutionException{
		
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
