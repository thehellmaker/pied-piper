package com.github.piedpiper.node.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.ResultCaptor;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Unirest.class)
public class RESTPostHandlerTest {

	@Before
	public void setupClass() {
		JsonUtils.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

			private static final long serialVersionUID = -185165746100799851L;

			@Override
			public boolean hasIgnoreMarker(final AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || StringUtils.containsIgnoreCase(m.getName(), ("Mock"));
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEmptyBodyInput() throws FileNotFoundException, IOException, UnirestException {
		PowerMockito.mockStatic(Unirest.class);
		HttpRequestWithBody requestWithBodyMock = Mockito.mock(HttpRequestWithBody.class);
		Mockito.when(Unirest.post(Mockito.anyString())).thenReturn(requestWithBodyMock);
		RequestBodyEntity bodyEntityMock = Mockito.mock(RequestBodyEntity.class);
		Mockito.when(requestWithBodyMock.body(Mockito.anyString())).thenReturn(bodyEntityMock);
		@SuppressWarnings("rawtypes")
		HttpResponse  response = Mockito.mock(HttpResponse.class);
		Mockito.when(bodyEntityMock.asString()).thenReturn(response);
		RESTPostHandler handler = new RESTPostHandler();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("POSTNoBody.json"))));
		NodeOutput nodeOutput = handler.apply(input);
		Assert.assertNotNull(nodeOutput);
	}

	@Test(expected = RuntimeException.class)
	public void testException() throws FileNotFoundException, IOException {
		PowerMockito.mockStatic(Unirest.class);
		RESTPostHandler handler = new RESTPostHandler();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("POSTSuccessRequest.json"))));
		handler.apply(input);
	}

	@Test
	public void testSuccess() throws Exception {
		PowerMockito.mockStatic(Unirest.class);
		HttpRequestWithBody requestWithBodyMock = Mockito.mock(HttpRequestWithBody.class);
		Mockito.when(Unirest.post(Mockito.anyString())).thenReturn(requestWithBodyMock);
		Mockito.when(requestWithBodyMock.header(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(requestWithBodyMock);
		RequestBodyEntity requestBodyEntity = Mockito.mock(RequestBodyEntity.class);
		Mockito.when(requestWithBodyMock.body(Mockito.anyString())).thenReturn(requestBodyEntity);
		@SuppressWarnings("unchecked")
		HttpResponse<JsonNode> response = Mockito.mock(HttpResponse.class);
		Mockito.when(requestBodyEntity.asJson()).thenReturn(response);
		Mockito.when(response.getBody()).thenReturn(new JsonNode("{}"));
		
		RESTPostHandler handler = new RESTPostHandler();
		
		RESTPostHandler spyHandler = Mockito.spy(handler);
		ResultCaptor<String> resultCaptor = new ResultCaptor<>();
		Mockito.doAnswer(resultCaptor).when(spyHandler).getBody(Mockito.any());
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("POSTSuccessRequest.json"))));
		spyHandler.apply(input);
		Assert.assertEquals("{\"tony\":\"stark\"}", resultCaptor.getResult());
		Mockito.verify(requestWithBodyMock, VerificationModeFactory.times(2)).header(Mockito.anyString(),
				Mockito.anyString());
		Mockito.verify(requestWithBodyMock, VerificationModeFactory.times(1)).body(Mockito.any(String.class));
		Mockito.verify(requestBodyEntity, VerificationModeFactory.times(1)).asJson();
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/rest/resources/" + fileName;
	}

}
