package com.github.piedpiper.node.stepfunctions;

import java.util.Optional;
import java.util.function.Function;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.inject.Inject;

public class StepFunctionsSendTaskSuccess implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata TASKTOKEN = new ParameterMetadata("taskToken", ParameterMetadata.MANDATORY);
	private static final ParameterMetadata OUTPUT = new ParameterMetadata("output");

	private AWSStepFunctions stepFunctionsClient;

	@Inject
	public StepFunctionsSendTaskSuccess(AWSStepFunctions stepFunctionsClient) {
		this.stepFunctionsClient = stepFunctionsClient;
	}

	public NodeOutput apply(NodeInput input) {

		try {

			String taskToken = ParameterUtils.getParameterData(input.getInput(), TASKTOKEN).getValueString();
			String stateMachineOutput = getStateMachineOutput(input);

			SendTaskSuccessRequest sendTaskSuccessRequest = getSendTaskSuccessRequest();
			sendTaskSuccessRequest.setTaskToken(taskToken);
			sendTaskSuccessRequest.setOutput(stateMachineOutput);

			SendTaskSuccessResult sendTaskSuccessResult = this.stepFunctionsClient
					.sendTaskSuccess(sendTaskSuccessRequest);

			NodeOutput output = new NodeOutput();
			ObjectNode outputNode = (ObjectNode) JsonUtils.mapper.valueToTree(sendTaskSuccessResult);
			output.setOutput(outputNode);
			return output;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected SendTaskSuccessRequest getSendTaskSuccessRequest() {
		return new SendTaskSuccessRequest();
	}

	private String getStateMachineOutput(NodeInput input) {
		return Optional.ofNullable(input).map(node -> node.getInput())
				.map(inputJsonNode -> inputJsonNode.get(OUTPUT.getParameterName()))
				.map(value -> value.get(PiedPiperConstants.VALUE)).map(value -> value.asText()).orElse("{}");

	}

}
