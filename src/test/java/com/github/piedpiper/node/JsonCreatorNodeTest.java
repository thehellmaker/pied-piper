package com.github.piedpiper.node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.aws.dynamo.TestConstants;

public class JsonCreatorNodeTest {

	@Test
	public void testSuccess() throws FileNotFoundException, IOException {
		JsonNode node = JsonUtils.mapper.readTree(new FileInputStream(getFileName("successJsonCreator.json")));
		NodeInput input = new NodeInput();
		input.setInput(node);
		JsonNode output = new JsonCreatorNode().apply(input).getOutput();
		Assert.assertTrue(output.get("region").get("value") instanceof TextNode);
		Assert.assertTrue(output.get(":time").get("value") instanceof JsonNode);
	}
	
	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/resources/" + fileName;
	}
}
