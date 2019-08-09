package com.github.piedpiper.node.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.github.piedpiper.aws.AccessKeySecretKeyCredentialProvider;

public class AWSLambdaFactory implements ILambdaFactory {

	@Override
	public AWSLambda createLambdaClient(String accessKey, String secretKey, String region) {
		return AWSLambdaClientBuilder.standard()
				.withCredentials(new AccessKeySecretKeyCredentialProvider(accessKey, secretKey)).withRegion(region)
				.build();
	}

	@Override
	public AWSLambda createLambdaClient(String region) {
		return AWSLambdaClientBuilder.standard()
				.withRegion(region)
				.build();
	}

}
