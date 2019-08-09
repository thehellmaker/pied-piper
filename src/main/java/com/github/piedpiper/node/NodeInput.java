package com.github.piedpiper.node;

import com.github.piedpiper.graph.api.types.NodeSpecification;
import com.fasterxml.jackson.databind.JsonNode;

public class NodeInput {

	private NodeSpecification nodeSpecification;
	
	private JsonNode input;

	public JsonNode getInput() {
		return input;
	}

	public void setInput(JsonNode input) {
		this.input = input;
	}

	public NodeSpecification getNodeSpecification() {
		return nodeSpecification;
	}

	public void setNodeSpecification(NodeSpecification nodeSpecification) {
		this.nodeSpecification = nodeSpecification;
	}
	
}
