package com.github.piedpiper.guice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeListQueryHandler;
import com.github.piedpiper.node.aws.AWSLambdaFactory;
import com.github.piedpiper.node.aws.AWSLambdaNode;
import com.github.piedpiper.node.aws.ILambdaFactory;
import com.github.piedpiper.storage.DynamoDBStorage;
import com.github.piedpiper.storage.IGraphStorage;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import akka.actor.ActorSystem;

public class PiedPiperModule extends AbstractModule {

	private static final String PROD_GOOGLE_CREDENTIAL_PATH = "com/github/piedpiper/node/firebase/atom8-157617-firebase-adminsdk-mbg44-7eb9af3f7b.json";

	private static final String NODEJS_TEMPLATE_PATH = "pied-piper-script.js";

	public PiedPiperModule() {
	}

	@Override
	protected void configure() {

		bind(Function.class).annotatedWith(Names.named(NodeListQueryHandler.class.getName()))
				.to(NodeListQueryHandler.class);
		bind(ILambdaFactory.class).to(AWSLambdaFactory.class);
		bind(IGraphStorage.class).to(DynamoDBStorage.class);
		bind(AWSLambdaNode.class).annotatedWith(Names.named(PiedPiperConstants.SEARCH_GRAPH_LAMBDA_NODE_NAME))
				.toInstance(new AWSLambdaNode());
		bind(String.class).annotatedWith(Names.named(PiedPiperConstants.PROD_SEARCH_ENDPOINT))
				.toInstance("https://ms9uc1ppsa.execute-api.us-east-1.amazonaws.com/prod/graph/search");
		bind(String.class).annotatedWith(Names.named(PiedPiperConstants.PROD_EXECUTE_GRAPH_ENDPOINT))
				.toInstance("https://ms9uc1ppsa.execute-api.us-east-1.amazonaws.com/prod/graph/run");

	}

	@Provides
	@Singleton
	@Named(PiedPiperConstants.GRAPH_ACTOR)
	public ActorSystem getActorSystem() {
		return ActorSystem.create(UUID.randomUUID().toString());
	}

	@Provides
	@Singleton
	public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
		return AWSSimpleSystemsManagementClientBuilder.standard().withRegion("us-east-1").build();
	}

	@Provides
	@Singleton
	public AWSStepFunctions getAWSStepFunctionsClient() {
		return AWSStepFunctionsClientBuilder.standard().build();
	}

	@Provides
	@Singleton
	public ExecutorService getExecutorService() {
		return Executors.newCachedThreadPool();
	}

	@Provides
	@Singleton
	@Named(PiedPiperConstants.AWS_SSM_CACHE)
	public LoadingCache<String, String> getInMemoryAWSSSMCache(AWSSimpleSystemsManagement ssmClient) {
		return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
			public String load(String parameterName) {
				return ParameterUtils.resolveAWSSSMParameter(ssmClient, parameterName);
			}
		});
	}

	@Provides
	@Singleton
	@Named(PiedPiperConstants.GRAPH_CACHE)
	public Map<String, JsonNode> getGraphCache() {
		return Maps.newHashMap();
	}

	@Provides
	@Singleton
	@Named(PiedPiperConstants.NODEJS_SCRIPT_TEMPLATE)
	public String getScriptTemplate() throws IOException, URISyntaxException {
		try {
			return readPath(NODEJS_TEMPLATE_PATH);
		} catch (Exception e) {
			// This is the path used if its run as a part of lambda
			return readPath("com/github/piedpiper/guice/" + NODEJS_TEMPLATE_PATH);
		}
	}

	private String readPath(String path) throws IOException {
		InputStream nodeJsTemplateFile = getClass().getClassLoader().getResourceAsStream(path);
		return IOUtils.toString(nodeJsTemplateFile, Charset.forName("UTF-8"));
	}

}
