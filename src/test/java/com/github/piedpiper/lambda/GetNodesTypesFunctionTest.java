package com.github.piedpiper.lambda;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeListQueryHandler;
import com.github.piedpiper.node.NodeMetadata;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class GetNodesTypesFunctionTest {

	private static Object input;

	@BeforeClass
	public static void createInput() throws IOException {
		// TODO: set up your sample input object here.
		input = null;
	}

	private Context createContext() {
		TestContext ctx = new TestContext();

		// TODO: customize your context here if needed.
		ctx.setFunctionName("Your Function Name");

		return ctx;
	}

	@Test
	public void testGetNodesTypesFunction() throws IOException {
		Context ctx = createContext();

		GetNodesTypesLambdaFunction handler = new GetNodesTypesLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Function.class).annotatedWith(Names.named(NodeListQueryHandler.class.getName()))
						.toInstance(new Function<Void, List<NodeMetadata>>() {
							@Override
							public List<NodeMetadata> apply(Void input) {
								return Lists.newArrayList(new NodeMetadata(), new NodeMetadata());
							}
						});
			}
		}));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		handler.handleRequest(new StringInputStream(""), output, ctx);
		ArrayNode array = (ArrayNode) JsonUtils.mapper.readTree(new String(output.toByteArray()));
		Assert.assertEquals(2, array.size());
	}

	@Test
	public void testGetNodesTypesFunctionException() throws IOException {
		Context ctx = createContext();

		GetNodesTypesLambdaFunction handler = new GetNodesTypesLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Function.class).annotatedWith(Names.named(NodeListQueryHandler.class.getName()))
						.toInstance(new Function<Void, List<NodeMetadata>>() {
							@Override
							public List<NodeMetadata> apply(Void input) {
								throw new RuntimeException();
							}
						});
			}
		}));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		handler.handleRequest(new StringInputStream(""), output, ctx);
		Assert.assertTrue(new String(output.toByteArray()).contains("RuntimeException"));
	}

}
