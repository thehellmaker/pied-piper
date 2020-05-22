package com.github.piedpiper.node.stepfunctions;

import java.util.Optional;
import java.util.function.Function;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.inject.Inject;

public class StepFunctionsExecute implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata ARN = new ParameterMetadata("arn");
	private static final ParameterMetadata NAME = new ParameterMetadata("name");
	private static final ParameterMetadata INPUT = new ParameterMetadata("input");

	private AWSStepFunctions stepFunctionsClient;

	@Inject
	public StepFunctionsExecute(AWSStepFunctions stepFunctionsClient) {
		this.stepFunctionsClient = stepFunctionsClient;
	}

	public NodeOutput apply(NodeInput input) {

		try {

			String stateMachineName = ParameterUtils.getParameterData(input.getInput(), NAME).getValueString();
			String stateMachineARN = ParameterUtils.getParameterData(input.getInput(), ARN).getValueString();
			String stateMachineInput = getStateMachineInput(input);

			StartExecutionRequest startExecutionRequest = getStartExecutionRequest();
			startExecutionRequest.setStateMachineArn(stateMachineARN);
			startExecutionRequest.setName(stateMachineName);
			startExecutionRequest.setInput(stateMachineInput);

			StartExecutionResult startExecutionResult = this.stepFunctionsClient.startExecution(startExecutionRequest);

			NodeOutput output = new NodeOutput();
			ObjectNode outputNode = (ObjectNode) JsonUtils.mapper.valueToTree(startExecutionResult);
			output.setOutput(outputNode);
			return output;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected StartExecutionRequest getStartExecutionRequest() {
		return new StartExecutionRequest();
	}

	private String getStateMachineInput(NodeInput input) {
		return Optional.ofNullable(input).map(node -> node.getInput())
				.map(inputJsonNode -> inputJsonNode.get(INPUT.getParameterName()))
				.map(value -> value.get(PiedPiperConstants.VALUE)).map(value -> value.asText()).orElse("{}");

	}

}
