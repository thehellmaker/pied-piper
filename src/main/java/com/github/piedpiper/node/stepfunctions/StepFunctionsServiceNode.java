package com.github.piedpiper.node.stepfunctions;

import java.util.Optional;
import java.util.function.Function;

import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.node.aws.AWSNode;
import com.google.common.collect.Lists;

public class StepFunctionsServiceNode extends AWSNode {

	public static final ParameterMetadata METHOD = new ParameterMetadata("method", ParameterMetadata.MANDATORY,
			Lists.newArrayList("EXECUTE", "DESCRIBE"));

	@Override
	public NodeOutput apply(NodeInput nodeInput) {
		return getStepFunctionsMethodHandler(nodeInput).apply(nodeInput);
	}

	private Function<NodeInput, NodeOutput> getStepFunctionsMethodHandler(NodeInput nodeInput) {
		try {
			String method = getStepFunctionsMethod(nodeInput);
			switch (method) {
			case "EXECUTE":
				return injector.getInstance(StepFunctionsExecute.class);
			case "DESCRIBE":
				return injector.getInstance(StepFunctionsDescribe.class);
			default:
				throw new IllegalArgumentException(String.format("Unsupported StepFunctions Method = %s", method));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private String getStepFunctionsMethod(NodeInput nodeInput) {
		return Optional.ofNullable(nodeInput).map(node -> node.getInput())
				.map(inputJsonNode -> inputJsonNode.get(METHOD.getParameterName()))
				.map(methodNode -> methodNode.get(PiedPiperConstants.VALUE)).map(valueNode -> valueNode.asText())
				.orElse(null);
	}
}
