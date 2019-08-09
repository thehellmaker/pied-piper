package com.github.piedpiper.lambda;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBWriterNode;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class SaveGraphFunctionTest {

	private static Object input;

	@BeforeClass
	public static void createInput() throws IOException {
		input = new ObjectMapper().readTree(new FileInputStream(getFileName("successSaveGraphRequest.json")));
	}

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("Your Function Name");
		return ctx;
	}

	@Test
	public void testSaveGraphFunction() throws IOException {
		Context ctx = createContext();

		SaveGraphLambdaFunction handler = new SaveGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				NodeOutput output = new NodeOutput();
				output.setNodeName("testNode");
				DynamoDBWriterNode writerNode = Mockito.mock(DynamoDBWriterNode.class);
				Mockito.doReturn(output).when(writerNode).apply(Mockito.any());
				bind(DynamoDBWriterNode.class).toInstance(writerNode);
			}

			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmClient = Mockito.mock(AWSSimpleSystemsManagement.class);
				Mockito.doReturn(new GetParameterResult().withParameter(new Parameter().withValue("dummyValue")))
						.when(ssmClient).getParameter(Mockito.any(GetParameterRequest.class));
				return ssmClient;
			}
			@Provides
			@Singleton
			@Named(PiedPiperConstants.AWS_SSM_CACHE)
			public LoadingCache<String, String> getInMemoryCache(AWSSimpleSystemsManagement ssmClient) {
				return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
					public String load(String parameterName) {
						return ParameterUtils.resolveAWSSSMParameter(ssmClient, parameterName);
					}
				});
			}
		}));
		String output = handler.handleRequest(input, ctx);
		JsonNode outputNode = JsonUtils.mapper.readTree(output);
		Assert.assertEquals("testNode", outputNode.get("nodeName").asText());
	}

	@Test
	public void testSaveGraphFunctionException() throws IOException {
		Context ctx = createContext();

		SaveGraphLambdaFunction handler = new SaveGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmClient = Mockito.mock(AWSSimpleSystemsManagement.class);
				Mockito.doThrow(new RuntimeException("testException"))
						.when(ssmClient).getParameter(Mockito.any(GetParameterRequest.class));
				return ssmClient;
			}
			@Provides
			@Singleton
			@Named(PiedPiperConstants.AWS_SSM_CACHE)
			public LoadingCache<String, String> getInMemoryCache(AWSSimpleSystemsManagement ssmClient) {
				return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
					public String load(String parameterName) {
						return ParameterUtils.resolveAWSSSMParameter(ssmClient, parameterName);
					}
				});
			}
		}));
		String output = handler.handleRequest(input, ctx);
		Assert.assertTrue(output.contains("testException"));
	}

	private static String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/lambda/resources/" + fileName;
	}

}
