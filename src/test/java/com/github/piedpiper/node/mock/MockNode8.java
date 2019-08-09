package com.github.piedpiper.node.mock;

import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;

public class MockNode8 extends BaseNode {

	@Override
	public NodeOutput apply(NodeInput t) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeOutput output = new NodeOutput();
		output.setOutput(t.getInput());
		return output;
	}

}
