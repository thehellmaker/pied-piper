package com.github.piedpiper.node.stepfunctions;

import com.github.piedpiper.node.NodeOutput;


import java.util.Optional;


import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;


public class StepFunctionsExecute extends StepFunctionsBaseHandler {
	private static ParameterMetadata NAME = new ParameterMetadata("name",ParameterMetadata.MANDATORY);
	private static ParameterMetadata ARN = new ParameterMetadata("arn",ParameterMetadata.MANDATORY);
	private static ParameterMetadata INPUT = new ParameterMetadata("input");

	@Override
	protected NodeOutput stepFunctionsRequestHandler(NodeInput input) throws Exception {
		
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
	}
	
	protected StartExecutionRequest getStartExecutionRequest(){
		return new StartExecutionRequest();
	}
	
	private String getStateMachineInput(NodeInput input){
		String stateMachineInput = Optional.ofNullable(input).map(node -> node.getInput())
									.map(inputJsonNode -> inputJsonNode.get(INPUT.getParameterName()))
									.map(value -> value.get(PiedPiperConstants.VALUE))
									.map(value -> value.asText())
									.orElse("{}");
		//input has to be in JSON string format
		if(stateMachineInput.isEmpty()){
			stateMachineInput = "{}";
		}
		return stateMachineInput;
	}
		
}
