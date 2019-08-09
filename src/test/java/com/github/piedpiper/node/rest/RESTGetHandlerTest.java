package com.github.piedpiper.node.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Unirest.class)
public class RESTGetHandlerTest {

	@Test(expected = IllegalArgumentException.class)
	public void testBadInput() {
		PowerMockito.mockStatic(Unirest.class);
		RESTGetHandler handler = new RESTGetHandler();
		PowerMockito.when(Unirest.get(Mockito.anyString())).thenReturn(null);
		handler.apply(new NodeInput());
	}

	@Test
	public void testUnirestException() throws FileNotFoundException, IOException, UnirestException {
		PowerMockito.mockStatic(Unirest.class);
		JsonUtils.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

			private static final long serialVersionUID = -926454488702714941L;

			@Override
			public boolean hasIgnoreMarker(final AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || StringUtils.containsIgnoreCase(m.getName(), ("Mock"));
			}
		});

		RESTGetHandler handler = new RESTGetHandler();
		GetRequest request = Mockito.mock(GetRequest.class);
		@SuppressWarnings("unchecked")
		HttpResponse<String> response = Mockito.mock(HttpResponse.class);
		Mockito.when(response.getBody()).thenReturn("test");
		Mockito.when(request.asString()).thenThrow(new UnirestException("test"));
		PowerMockito.when(Unirest.get(Mockito.anyString())).thenReturn(request);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("GETSuccessRequest.json"))));
		try {
			handler.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains("UnirestException"));
		}
	}

	@Test
	public void testSuccess() throws FileNotFoundException, IOException, UnirestException {
		PowerMockito.mockStatic(Unirest.class);
		JsonUtils.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

			private static final long serialVersionUID = -185165746100799851L;

			@Override
			public boolean hasIgnoreMarker(final AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || StringUtils.containsIgnoreCase(m.getName(), ("Mock"));
			}
		});

		RESTGetHandler handler = new RESTGetHandler();
		GetRequest request = Mockito.mock(GetRequest.class);
		@SuppressWarnings("unchecked")
		HttpResponse<String> response = Mockito.mock(HttpResponse.class);
		Mockito.when(response.getBody()).thenReturn("{ \"value\": \"test\"}");
		Mockito.when(request.asString()).thenReturn(response);
		PowerMockito.when(Unirest.get("http://www.google.com")).thenReturn(request);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("GETSuccessRequest.json"))));
		Assert.assertEquals("test", handler.apply(input).getOutput().get("body").get("value").asText());
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/rest/resources/" + fileName;
	}

}
