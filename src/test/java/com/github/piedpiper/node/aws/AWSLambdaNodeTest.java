package com.github.piedpiper.node.aws;

import java.io.FileInputStream;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AWSLambdaClientBuilder.class)
public class AWSLambdaNodeTest {

	private static final String AWS_SIGNATURE = "\"AWS4-HMAC-SHA256 Credential=/20180614/us-east-1/execute-api/aws4_request, SignedHeaders=x-amz-date, Signature=bb22466f47a33b8e3c53f3cd239109dbebab430d0044ce22c4350773e7b8a444\"";

	@Test
	public void testLambdaNodeCall() throws Exception {
		JsonUtils.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

			private static final long serialVersionUID = -926454488702714941L;

			@Override
			public boolean hasIgnoreMarker(final AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || StringUtils.containsIgnoreCase(m.getName(), ("Mock"));
			}
		});
		AWSLambda client = Mockito.mock(AWSLambda.class);
		AWSLambdaNode node = new AWSLambdaNode();
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ILambdaFactory.class).toInstance(new ILambdaFactory() {

					@Override
					public AWSLambda createLambdaClient(String accessKey, String secretKey, String region) {
						return client;
					}

					@Override
					public AWSLambda createLambdaClient(String region) {
						return client;
					}
				});
			}
			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmMock = Mockito.mock(AWSSimpleSystemsManagement.class);
				GetParameterResult result = new GetParameterResult();
				Parameter parameter = new Parameter();
				parameter.setValue("DummyValue");
				result.setParameter(parameter);
				Mockito.when(ssmMock.getParameter(Mockito.any())).thenReturn(result);
				return ssmMock;
			}

		}));
		InvokeResult result = Mockito.mock(InvokeResult.class);
		Mockito.when(result.getPayload()).thenReturn(ByteBuffer.wrap(AWS_SIGNATURE.getBytes()));
		Mockito.when(client.invoke(Mockito.any(InvokeRequest.class))).thenReturn(result);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("successSigner.json"))));
		Assert.assertEquals(
				ParameterUtils.bytesToStringWithStrippedQuotesIfPresent(ByteBuffer.wrap(AWS_SIGNATURE.getBytes())),
				node.apply(input).getOutput().get(PiedPiperConstants.STRING_PAYLOAD).asText());
	}

	@Test
	public void testLambdaNodeCallException() throws Exception {
		JsonUtils.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

			private static final long serialVersionUID = -926454488702714941L;

			@Override
			public boolean hasIgnoreMarker(final AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || StringUtils.containsIgnoreCase(m.getName(), ("Mock"));
			}
		});
		AWSLambda client = Mockito.mock(AWSLambda.class);
		AWSLambdaNode node = new AWSLambdaNode();
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ILambdaFactory.class).toInstance(new ILambdaFactory() {

					@Override
					public AWSLambda createLambdaClient(String accessKey, String secretKey, String region) {
						return client;
					}

					@Override
					public AWSLambda createLambdaClient(String region) {
						return client;
					}
				});
			}
		}));
		Mockito.when(client.invoke(Mockito.any(InvokeRequest.class))).thenThrow(new RuntimeException("test"));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("successSigner.json"))));
		try {
			node.apply(input);
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains("test"));
		}
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/aws/resources/" + fileName;
	}

}
