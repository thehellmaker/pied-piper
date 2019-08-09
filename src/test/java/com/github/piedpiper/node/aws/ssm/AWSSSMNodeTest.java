package com.github.piedpiper.node.aws.ssm;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.AWSLambdaNode;
import com.github.piedpiper.node.aws.ILambdaFactory;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class AWSSSMNodeTest {

	@Test
	public void testSuccess() {
		String EXPECTED_VALUE = "DummyValue";
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
			}
			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmMock = Mockito.mock(AWSSimpleSystemsManagement.class);
				GetParameterResult result = new GetParameterResult();
				Parameter parameter = new Parameter();
				parameter.setValue(EXPECTED_VALUE);
				result.setParameter(parameter);
				Mockito.when(ssmMock.getParameter(Mockito.any())).thenReturn(result);
				return ssmMock;
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
		});

		
		
		ObjectNode inputValueNode = JsonUtils.mapper.createObjectNode();
		inputValueNode.put("value", "test");
		
		ObjectNode inputNode = JsonUtils.mapper.createObjectNode();
		inputNode.set(AWSSSMNode.KEY_NAME_PARAMETER.getParameterName(), inputValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		
		AWSSSMNode node = injector.getInstance(AWSSSMNode.class);
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(injector);
		
		NodeOutput output = node.apply(input);
		Assert.assertEquals(EXPECTED_VALUE, output.getOutput().get(AWSSSMNode.SECURE_VALUE).asText());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testKeyNameParameterNotPresent() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
			}
			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmMock = Mockito.mock(AWSSimpleSystemsManagement.class);
				GetParameterResult result = new GetParameterResult();
				Parameter parameter = new Parameter();
				parameter.setValue("");
				result.setParameter(parameter);
				Mockito.when(ssmMock.getParameter(Mockito.any())).thenReturn(result);
				return ssmMock;
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
		});
		
		NodeInput input = new NodeInput();
		AWSSSMNode node = injector.getInstance(AWSSSMNode.class);
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(injector);
		
		node.apply(input);
	}
	
	@Test(expected=RuntimeException.class)
	public void testCheckedException() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
			}
			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmMock = Mockito.mock(AWSSimpleSystemsManagement.class);
				GetParameterResult result = new GetParameterResult();
				Parameter parameter = new Parameter();
				parameter.setValue("");
				result.setParameter(parameter);
				Mockito.when(ssmMock.getParameter(Mockito.any())).thenReturn(result);
				return ssmMock;
			}

			@Provides
			@Singleton
			@Named(PiedPiperConstants.AWS_SSM_CACHE)
			public LoadingCache<String, String> getInMemoryAWSSSMCache(AWSSimpleSystemsManagement ssmClient) throws ExecutionException {
				return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
					public String load(String parameterName) throws Exception {
						throw new Exception("");
					}
				});
			}
		});
		
		ObjectNode inputValueNode = JsonUtils.mapper.createObjectNode();
		inputValueNode.put("value", "test");
		
		ObjectNode inputNode = JsonUtils.mapper.createObjectNode();
		inputNode.set(AWSSSMNode.KEY_NAME_PARAMETER.getParameterName(), inputValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		
		AWSSSMNode node = injector.getInstance(AWSSSMNode.class);
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(injector);
		
		node.apply(input);
	}
	
}
