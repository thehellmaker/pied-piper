package com.github.piedpiper.node.stepfunctions;

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
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class StepFunctionsServiceNodeTest {
	@Test
	public void testExecuteMethod() throws FileNotFoundException, IOException {
		StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
		sfNode.setILogger(new Slf4jLoggerImpl());
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			@Singleton
			public StepFunctionsExecute providesHandler() throws IOException {
				StepFunctionsExecute handler = Mockito.mock(StepFunctionsExecute.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\":\"EXECUTE\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		}));
		sfNode.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFExecuteSuccessGraph.json"))));
		NodeOutput output = sfNode.apply(input);
		Assert.assertEquals(output.getOutput().get("method").asText(), "EXECUTE");

	}

	@Test
	public void testDescribeMethod() throws FileNotFoundException, IOException {
		StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
		sfNode.setILogger(new Slf4jLoggerImpl());
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			@Singleton
			public StepFunctionsDescribe providesHandler() throws IOException {
				StepFunctionsDescribe handler = Mockito.mock(StepFunctionsDescribe.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\":\"DESCRIBE\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		}));
		sfNode.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFDescribeSuccessGraph.json"))));
		NodeOutput output = sfNode.apply(input);
		Assert.assertEquals(output.getOutput().get("method").asText(), "DESCRIBE");

	}

	@Test
	public void testDescribeExecutionMethod() throws FileNotFoundException, IOException {
		StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
		sfNode.setILogger(new Slf4jLoggerImpl());
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			@Singleton
			public StepFunctionsDescribeExecution providesHandler() throws IOException {
				StepFunctionsDescribeExecution handler = Mockito.mock(StepFunctionsDescribeExecution.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\":\"DESCRIBE_EXECUTION\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		}));
		sfNode.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(
				JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFDescribeExecutionSuccessGraph.json"))));
		NodeOutput output = sfNode.apply(input);
		Assert.assertEquals(output.getOutput().get("method").asText(), "DESCRIBE_EXECUTION");

	}

	@Test
	public void testSendTaskSuccessMethod() throws FileNotFoundException, IOException {
		StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
		sfNode.setILogger(new Slf4jLoggerImpl());
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			@Singleton
			public StepFunctionsSendTaskSuccess providesHandler() throws IOException {
				StepFunctionsSendTaskSuccess handler = Mockito.mock(StepFunctionsSendTaskSuccess.class);
				NodeOutput output = new NodeOutput();
				output.setOutput(JsonUtils.mapper.readTree("{\"method\":\"SEND_TASK_SUCCESS\"}"));
				Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
				return handler;
			}
		}));
		sfNode.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFSendTaskSuccessGraph.json"))));
		NodeOutput output = sfNode.apply(input);
		Assert.assertEquals(output.getOutput().get("method").asText(), "SEND_TASK_SUCCESS");

	}

	@Test
	public void testIllegalArgumentException() throws FileNotFoundException, IOException {
		StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
		sfNode.setILogger(new Slf4jLoggerImpl());
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {

			}
		}));
		sfNode.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("UnsupportedSFMethod.json"))));
		try {
			sfNode.apply(input);
			Assert.fail("sfNode didn't throw the IllegalArgumentException in testIllegalArgumentException");
		} catch (RuntimeException ex) {
			Assert.assertEquals("java.lang.IllegalArgumentException: Unsupported StepFunctions Method = UNKNOWN",
					ex.getMessage());
		}

	}

	private String getFileName(String fileName) {
		return "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}
}
