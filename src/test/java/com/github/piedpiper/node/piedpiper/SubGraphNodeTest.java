package com.github.piedpiper.node.piedpiper;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.piedpiper.SubGraphNode;
import com.github.piedpiper.transformer.ExecuteGraphFunction;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class SubGraphNodeTest {

	private static final String TEST_NODE_OUTPUT_STRING = "testNodeOuput";
	private static final Object THINGS_GROUP_NAME = "Akash_Home";
	private static final String THINGS_GROUP_NAME_KEY = "thingGroupName";

	@Test
	public void testSuccess() throws Exception {
		ILogger logger = new Slf4jLoggerImpl();
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
			}

			@Provides
			@Singleton
			@Named(PiedPiperConstants.GRAPH_CACHE)
			public Map<String, JsonNode> getGraphCache() {
				return Maps.newHashMap();
			}

		});

		ObjectNode inputJson = JsonUtils.mapper.createObjectNode();
		inputJson.set(DynamoDBBaseNode.TABLE_NAME.getParameterName(),
				ParameterUtils.createParamValueNode(PiedPiperConstants.ALMIGHTY_TABLE_NAME));
		inputJson.set("projectName", ParameterUtils.createParamValueNode("Atom8"));
		inputJson.set("graphName", ParameterUtils.createParamValueNode("ListThingsInThingGroup"));
		inputJson.set(THINGS_GROUP_NAME_KEY, ParameterUtils.createParamValueNode(THINGS_GROUP_NAME));
		NodeInput input = new NodeInput();
		input.setInput(inputJson);

		SubGraphNode node = new SubGraphNode();
		SubGraphNode spyNode = Mockito.spy(node);
		ExecuteGraphFunction executeGraphFunction = Mockito.mock(ExecuteGraphFunction.class);
		GraphDefinition graphDefinition = new GraphDefinition();
		graphDefinition.setGraphName(TEST_NODE_OUTPUT_STRING);
		Mockito.when(executeGraphFunction.apply(Mockito.any())).thenReturn(graphDefinition);
		Mockito.doReturn(executeGraphFunction).when(spyNode).getExecuteGraphFunction(Mockito.any(), Mockito.any());
		
		spyNode.setILogger(logger);
		spyNode.setInjector(injector);
		NodeOutput output = spyNode.apply(input);
		Assert.assertEquals(TEST_NODE_OUTPUT_STRING, output.getOutput().get("body").get("graphName").asText());
	}

}
