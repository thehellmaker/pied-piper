package com.github.piedpiper.graph.api.types;

import com.github.piedpiper.node.NodeInput;

public class NodeExecutor {

	private NodeInput nodeInput;
	
	private NodeDefinition nodeDefinition;
	
	public NodeInput getNodeInput() {
		return nodeInput;
	}

	public void setNodeInput(NodeInput nodeInput) {
		this.nodeInput = nodeInput;
	}

	public NodeDefinition getNodeDefinition() {
		return nodeDefinition;
	}

	public void setNodeDefinition(NodeDefinition nodeDefinition) {
		this.nodeDefinition = nodeDefinition;
	}

}
