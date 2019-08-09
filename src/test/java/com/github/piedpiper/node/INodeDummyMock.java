package com.github.piedpiper.node;

public class INodeDummyMock implements INode {

	@Override
	public NodeOutput apply(NodeInput arg0) {
		return new NodeOutput();
	}

}
