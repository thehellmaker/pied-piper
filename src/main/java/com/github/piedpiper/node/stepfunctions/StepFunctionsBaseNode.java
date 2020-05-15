package com.github.piedpiper.node.stepfunctions;
import java.util.function.Function;

import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.utils.ParameterUtils;

public abstract class StepFunctionsBaseNode implements Function<NodeInput,NodeOutput> {
	protected String getAccessKey(NodeInput input) throws Exception {
		return ParameterUtils.getParameterData(input.getInput(), StepFunctionsServiceNode.ACCESSKEY).getValueString();
	}
	
	protected String getSecretKey(NodeInput input) throws Exception {
		return ParameterUtils.getParameterData(input.getInput(), StepFunctionsServiceNode.SECRETKEY).getValueString();
	}
	
	protected String getRegion(NodeInput input) throws Exception {
		return ParameterUtils.getParameterData(input.getInput(), StepFunctionsServiceNode.REGION).getValueString();
	}
	
}
