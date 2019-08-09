package com.github.piedpiper.node.aws;

import com.amazonaws.services.lambda.AWSLambda;

public interface ILambdaFactory {

	public AWSLambda createLambdaClient(String region);
	
	public AWSLambda createLambdaClient(String accessKey, String secretKey, String region);
	
}
