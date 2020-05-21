package com.github.piedpiper.node.stepfunctions;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.github.piedpiper.aws.AccessKeySecretKeyCredentialProvider;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;

public abstract class StepFunctionsBaseHandler extends StepFunctionsBaseNode {
	protected AWSStepFunctions stepFunctionsClient;
	private AWSStepFunctions getStepFunctionsClient(NodeInput input) throws Exception{
		return AWSStepFunctionsClientBuilder.standard()
					.build();
		
	}
	
	@Override
	public NodeOutput apply(NodeInput input){
		try{
			this.stepFunctionsClient = getStepFunctionsClient(input);
			NodeOutput output = new NodeOutput();
			output = stepFunctionsRequestHandler(input);
			return output;
		}catch(IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract NodeOutput stepFunctionsRequestHandler(NodeInput input) throws Exception;

}
