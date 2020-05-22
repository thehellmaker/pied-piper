package com.github.piedpiper.node.aws;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;

public class AWSStepFunctionsFactory implements IStepFunctionsFactory {

	private AWSStepFunctions AWSStepFunctionsClient;

	public AWSStepFunctionsFactory() {
		AWSStepFunctionsClient = AWSStepFunctionsClientBuilder.standard().build();
	}

	@Override
	public AWSStepFunctions getStepFunctionsClient() {
		return this.AWSStepFunctionsClient;
	}

}
