package com.github.piedpiper.node.stepfunctions;

import java.util.function.Function;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.DescribeExecutionRequest;
import com.amazonaws.services.stepfunctions.model.DescribeExecutionResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.inject.Inject;

public class StepFunctionsDescribeExecution implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata ARN = new ParameterMetadata("arn", ParameterMetadata.MANDATORY);

	private AWSStepFunctions stepFunctionsClient;

	@Inject
	public StepFunctionsDescribeExecution(AWSStepFunctions stepFunctionsClient) {
		this.stepFunctionsClient = stepFunctionsClient;
	}

	public NodeOutput apply(NodeInput input) {

		try {
			String stateMachineARN = ParameterUtils.getParameterData(input.getInput(), ARN).getValueString();

			DescribeExecutionRequest describeExecutionRequest = getDescribeExecutionRequest();
			describeExecutionRequest.setExecutionArn(stateMachineARN);

			DescribeExecutionResult describeExecutionResult = this.stepFunctionsClient
					.describeExecution(describeExecutionRequest);

			NodeOutput output = new NodeOutput();
			ObjectNode outputNode = (ObjectNode) JsonUtils.mapper.valueToTree(describeExecutionResult);
			output.setOutput(outputNode);
			return output;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected DescribeExecutionRequest getDescribeExecutionRequest() {
		return new DescribeExecutionRequest();
	}

}
