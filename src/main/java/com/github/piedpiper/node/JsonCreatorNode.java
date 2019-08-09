package com.github.piedpiper.node;

public class JsonCreatorNode extends BaseNode {

	@Override
	public NodeOutput apply(NodeInput input) {
		NodeOutput output = new NodeOutput();
		output.setOutput(input.getInput());
		return output;
	}

}
