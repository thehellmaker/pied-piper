package com.github.piedpiper.node.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class RESTServiceNodeTest {

	@Test
	public void testGetMethod() throws FileNotFoundException, IOException {
		RESTServiceNode restNode = new RESTServiceNode();
		restNode.setILogger(new Slf4jLoggerImpl());
		restNode.setInjector(Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			@Singleton
			public RESTGetHandler providesRestHandler() throws IOException {
				RESTGetHandler handler = Mockito.mock(RESTGetHandler.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\": \"GET\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		})));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("GETSuccessRequest.json"))));
		NodeOutput output = restNode.apply(input);
		Assert.assertEquals("GET", output.getOutput().get("method").asText());
	}

	@Test
	public void testPostMethod() throws FileNotFoundException, IOException {
		RESTServiceNode restNode = new RESTServiceNode();
		restNode.setILogger(new Slf4jLoggerImpl());
		restNode.setInjector(Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			@Singleton
			public RESTPostHandler providesRestHandler() throws IOException {
				RESTPostHandler handler = Mockito.mock(RESTPostHandler.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\": \"POST\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		})));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("POSTSuccessRequest.json"))));
		NodeOutput output = restNode.apply(input);
		Assert.assertEquals("POST", output.getOutput().get("method").asText());
	}

	@Test
	public void testPutMethod() throws FileNotFoundException, IOException {
		RESTServiceNode restNode = new RESTServiceNode();
		restNode.setILogger(new Slf4jLoggerImpl());
		restNode.setInjector(Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			@Singleton
			public RESTPutHandler providesRestHandler() throws IOException {
				RESTPutHandler handler = Mockito.mock(RESTPutHandler.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\": \"PUT\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		})));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("PUTSuccessRequest.json"))));
		NodeOutput output = restNode.apply(input);
		Assert.assertEquals("PUT", output.getOutput().get("method").asText());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownMethod() throws FileNotFoundException, IOException {
		RESTServiceNode restNode = new RESTServiceNode();
		restNode.setILogger(new Slf4jLoggerImpl());
		restNode.setInjector(Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

		})));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("UnknownRequest.json"))));
		restNode.apply(input);
	}

	@Test
	public void testUnsupportedMethod() throws FileNotFoundException, IOException {
		RESTServiceNode restNode = new RESTServiceNode();
		restNode.setILogger(new Slf4jLoggerImpl());
		restNode.setInjector(Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
			}

		})));
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("UnsupportedHttpMethod.json"))));
		try {
			restNode.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Unsupported Http Method"));
		}
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/rest/resources/" + fileName;
	}

}
