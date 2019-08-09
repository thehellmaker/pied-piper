package com.github.piedpiper.node.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Inject;

public class MockNode5 extends BaseNode {

	private ObjectMapper mapper;

	@Inject
	public MockNode5(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	
	@Override
	public NodeOutput apply(NodeInput arg0) {
		NodeOutput nodeOutput = new NodeOutput();
		ObjectNode outputJson = mapper.createObjectNode();
		outputJson.put("referenceValue5", "sexyValue5");
		nodeOutput.setOutput(outputJson);
		return nodeOutput;
	}

}
