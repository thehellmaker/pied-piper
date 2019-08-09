package com.github.piedpiper.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.graph.api.types.ParameterDefinition;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;

public class CleanupOutputTransformerTest {

	@Test
	public void testReplaceSSMParameterLogic() throws IOException {
		JsonNode jsonInput = new ObjectMapper().readTree("{\"akash\": { \"value\": \"secret\" }}");

		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(jsonInput);

		ParameterDefinition paramDef = new ParameterDefinition();
		paramDef.setParameterName("akash");

		new CleanupOutputTransformer().clearAWSSSMParameter(nodeInput, paramDef);
		Assert.assertEquals(CleanupOutputTransformer.MASKED_VALUE,
				nodeInput.getInput().get(paramDef.getParameterName()).get("value").asText());
	}

	@Test
	public void testMaskingSuccess()
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {
		GraphDefinition graphDefinition = new ObjectMapper().readValue(
				new FileInputStream(getFileName("graphDefinitionWithRecursiveAWSSSMParams.json")),
				GraphDefinition.class);
		GraphDefinition convertedGraphDefinition = new CleanupOutputTransformer().apply(graphDefinition);
		JsonNode graphDefinitionSubGraphNode = convertedGraphDefinition.getNodeMap()
				.get("CreateAWSSignatureForListThingsInThingGroup").getNodeOutputList().get(0).getOutput().get("body");
		GraphDefinition subgraphDefinition = JsonUtils.mapper.treeToValue(graphDefinitionSubGraphNode,
				GraphDefinition.class);
		JsonNode maskedInput = subgraphDefinition.getNodeMap().get("DateHeaderGenerator").getNodeInputList().get(0)
				.getInput();
		String secretKey = maskedInput.get("secretKey").get("value").asText();
		String accessKey = maskedInput.get("accessKey").get("value").asText();
		Assert.assertEquals(CleanupOutputTransformer.MASKED_VALUE, secretKey);
		Assert.assertEquals(CleanupOutputTransformer.MASKED_VALUE, accessKey);

		NodeDefinition awsSSMNode = convertedGraphDefinition.getNodeMap().get("AWSSSMNode");
		Assert.assertNotNull(awsSSMNode);
		JsonNode secureValueNode = awsSSMNode.getNodeOutputList().get(0)
				.getOutput().get("secureValue");
		Assert.assertEquals(CleanupOutputTransformer.MASKED_VALUE, secureValueNode.asText());

		JsonNode referencedSecretValueNode = convertedGraphDefinition.getNodeMap().get("ListThingsInThingGroupRESTNode")
				.getNodeInputList().get(0).getInput().get("accessKey").get("value");
		Assert.assertEquals(CleanupOutputTransformer.MASKED_VALUE, referencedSecretValueNode.asText());

		NodeDefinition excludedInOutputNode = convertedGraphDefinition.getNodeMap()
				.get("Authorize");
		Assert.assertNull(excludedInOutputNode);
		

		NodeDefinition subGraphNodeExcludedNode = convertedGraphDefinition.getNodeMap()
				.get("GetThingShadowForThings");
		Assert.assertNotNull(subGraphNodeExcludedNode);

		NodeOutput excludeSubgraphOutput = subGraphNodeExcludedNode.getNodeOutputList().get(0);

		ObjectNode outputNode = (ObjectNode) excludeSubgraphOutput.getOutput();
		GraphDefinition subGraphOutput = JsonUtils.mapper.treeToValue(outputNode.get("body"),
				GraphDefinition.class);
		Assert.assertNull(subGraphOutput.getNodeMap().get("GetDeviceShadow"));

	}

	@Test
	public void testMaskingException()
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {
		GraphDefinition graphDefinition = new GraphDefinition();
		GraphDefinition maskedGraphDefinition = new CleanupOutputTransformer().apply(graphDefinition);
		Assert.assertEquals(JsonUtils.mapper.writeValueAsString(graphDefinition),
				JsonUtils.mapper.writeValueAsString(maskedGraphDefinition));
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/transformer/resources/" + fileName;
	}

}
