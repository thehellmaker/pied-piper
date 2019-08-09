package com.github.piedpiper.lambda;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.common.GraphUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.github.piedpiper.transformer.SearchGraphFunction;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class SearchGraphFunctionTest {

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("Your Function Name");
		return ctx;
	}

	@Test
	public void testSearchGraphWithProjectNameAndGraphNameSuccess() throws Exception {
		InputStream inputStream = new FileInputStream(getFileName("successSearchGraphWithProjectNameAndGraphName.json"));
		ArgumentCaptor<NodeInput> captor = ArgumentCaptor.forClass(NodeInput.class);
		DynamoDBReaderNode node = Mockito.mock(DynamoDBReaderNode.class);
		SearchGraphLambdaFunction handler = new SearchGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			public DynamoDBReaderNode providesDynamoDBReaderNode() {
				return node;
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
		Context ctx = createContext();

		handler.handleRequest(inputStream, new ByteArrayOutputStream(),  ctx);
		Mockito.verify(node, VerificationModeFactory.times(1)).apply(captor.capture());
		NodeInput nodeInput = captor.getValue();
		JsonNode searchJson = nodeInput.getInput();
		String keyExpression = searchJson.get(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName())
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(SearchGraphFunction.EXACT_RANGE_QUERY, keyExpression);
		String hashKey = searchJson
				.get(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(PiedPiperConstants.GRAPH, hashKey);
		String rangeKey = searchJson
				.get(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(GraphUtils.getRangeKeyEquals("PROJECT NAME", "GRAPH NAME"), rangeKey);
	}

	@Test
	public void testSearchGraphWithOnlyProjectNameSuccess() throws Exception {
		InputStream inputStream = new FileInputStream(getFileName("successSearchGraphWithOnlyProjectName.json"));
		ArgumentCaptor<NodeInput> captor = ArgumentCaptor.forClass(NodeInput.class);
		DynamoDBReaderNode node = Mockito.mock(DynamoDBReaderNode.class);
		SearchGraphLambdaFunction handler = new SearchGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			public DynamoDBReaderNode providesDynamoDBReaderNode() {
				return node;
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
		Context ctx = createContext();

		handler.handleRequest(inputStream, new ByteArrayOutputStream(), ctx);
		Mockito.verify(node, VerificationModeFactory.times(1)).apply(captor.capture());
		NodeInput nodeInput = captor.getValue();
		JsonNode searchJson = nodeInput.getInput();
		String keyExpression = searchJson.get(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName())
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(SearchGraphFunction.PREFIX_RANGE_QUERY, keyExpression);
		String hashKey = searchJson
				.get(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(PiedPiperConstants.GRAPH, hashKey);
		String rangeKey = searchJson
				.get(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_RANGE_KEY))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals("PROJECT NAME", rangeKey);
	}

	@Test
	public void testSearchGraphWithSearchTermsSuccess() throws Exception {
		InputStream inputStream = new FileInputStream(getFileName("successSearchGraphWithSearchTerms.json"));
		ArgumentCaptor<NodeInput> captor = ArgumentCaptor.forClass(NodeInput.class);
		DynamoDBReaderNode node = Mockito.mock(DynamoDBReaderNode.class);
		SearchGraphLambdaFunction handler = new SearchGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			public DynamoDBReaderNode providesDynamoDBReaderNode() {
				return node;
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
		Context ctx = createContext();

		handler.handleRequest(inputStream, new ByteArrayOutputStream(), ctx);
		Mockito.verify(node, VerificationModeFactory.times(1)).apply(captor.capture());
		NodeInput nodeInput = captor.getValue();
		JsonNode searchJson = nodeInput.getInput();
		String keyExpression = searchJson.get(DynamoDBReaderNode.KEY_QUERY_EXPRESSION.getParameterName())
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(PiedPiperConstants.HASH_KEY_QUERY, keyExpression);
		String hashKey = searchJson
				.get(ParameterUtils.getDynamoParamPlaceHolderName(PiedPiperConstants.ALMIGHTY_TABLE_HASH_KEY))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals(PiedPiperConstants.GRAPH, hashKey);
		String filterQueryExpression = searchJson.get(DynamoDBReaderNode.FILTER_QUERY_EXPRESSION.getParameterName())
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals("contains(graphIndex, :akash) or contains(graphIndex, :ashok)", filterQueryExpression);
		String termAkash = searchJson.get(ParameterUtils.getDynamoParamPlaceHolderName("akash"))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals("akash", termAkash);
		String termAshok = searchJson.get(ParameterUtils.getDynamoParamPlaceHolderName("ashok"))
				.get(PiedPiperConstants.VALUE).asText();
		Assert.assertEquals("ashok", termAshok);
	}
	
	@Test
	public void testSearchGraphWithNoProjectNameGraphNameOrSearchTerms() throws Exception {
		InputStream inputStream = new FileInputStream(getFileName("successSearchGraphWithNoProjectNameGraphNameOrSearchTerm.json"));
		DynamoDBReaderNode node = Mockito.mock(DynamoDBReaderNode.class);
		SearchGraphLambdaFunction handler = new SearchGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			public DynamoDBReaderNode providesDynamoDBReaderNode() {
				return node;
			}

			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmClient = Mockito.mock(AWSSimpleSystemsManagement.class);
				Mockito.doReturn(new GetParameterResult().withParameter(new Parameter().withValue("dummyValue")))
						.when(ssmClient).getParameter(Mockito.any(GetParameterRequest.class));
				return ssmClient;
			}
		}));
		Context ctx = createContext();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		handler.handleRequest(inputStream, outputStream, ctx);
		String output = new String(outputStream.toByteArray());
		System.out.println(output.contains("Unknown search type"));
	}
	
	@Test
	public void testSearchGraphException() throws Exception {
		InputStream inputStream = new FileInputStream((getFileName("successSearchGraphWithSearchTerms.json")));
		DynamoDBReaderNode node = Mockito.mock(DynamoDBReaderNode.class);
		Mockito.when(node.apply(Mockito.any())).thenThrow(new RuntimeException("test"));
		SearchGraphLambdaFunction handler = new SearchGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			public DynamoDBReaderNode providesDynamoDBReaderNode() {
				return node;
			}

			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmClient = Mockito.mock(AWSSimpleSystemsManagement.class);
				Mockito.doReturn(new GetParameterResult().withParameter(new Parameter().withValue("dummyValue")))
						.when(ssmClient).getParameter(Mockito.any(GetParameterRequest.class));
				return ssmClient;
			}
		}));
		Context ctx = createContext();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		handler.handleRequest(inputStream, outputStream, ctx);
		String output = new String(outputStream.toByteArray());
		Assert.assertTrue(output.contains("test"));
	}
	
	private static String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/lambda/resources/" + fileName;
	}

}
