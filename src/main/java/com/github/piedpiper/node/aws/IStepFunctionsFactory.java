package com.github.piedpiper.node.aws;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;

public interface IStepFunctionsFactory {
	public AWSStepFunctions getStepFunctionsClient();
}
