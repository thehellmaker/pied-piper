package com.github.piedpiper.node.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.github.piedpiper.aws.AccessKeySecretKeyCredentialProvider;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class DynamoDBBaseNode extends AWSNode {

	public static final ParameterMetadata TABLE_NAME = new ParameterMetadata("tableName", ParameterMetadata.MANDATORY);

	protected AmazonDynamoDB getAmazonDynamoDBClient(JsonNode jsonInput) throws Exception {
		String accessKey = ParameterUtils.getParameterData(jsonInput, ACCESS_KEY).getValueString();
		String secretKey = ParameterUtils.getParameterData(jsonInput, SECRET_KEY).getValueString();
		String region = ParameterUtils.getParameterData(jsonInput, REGION).getValueString();
		return AmazonDynamoDBClientBuilder.standard()
				.withCredentials(new AccessKeySecretKeyCredentialProvider(accessKey, secretKey)).withRegion(region)
				.build();

	}
	
	protected String getTableName(JsonNode jsonInput) throws Exception {
		return ParameterUtils.getParameterData(jsonInput, TABLE_NAME).getValueString();
	}
	
	protected DynamoDB getDynamoDB(AmazonDynamoDB amazonDynamoDBClient) {
		return new DynamoDB(amazonDynamoDBClient);
	}

}
