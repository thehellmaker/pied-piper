package com.github.piedpiper.node.aws.dynamo;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.collect.Maps;

public class DynamoDBWriterNode extends DynamoDBBaseNode {

	public static final ParameterMetadata CONDITION_EXPRESSION = new ParameterMetadata("conditionExpression", false);

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			JsonNode jsonInput = input.getInput();
			final AmazonDynamoDB ddb = getAmazonDynamoDBClient(jsonInput);
			Map<String, AttributeValue> itemValues = getItemMapFromJsonNode(jsonInput);
			PutItemRequest putItemRequest = new PutItemRequest(getTableName(jsonInput), itemValues);

			String conditionExpression = Optional
					.ofNullable(ParameterUtils.getParameterData(jsonInput, CONDITION_EXPRESSION))
					.map(conditionExprNode -> conditionExprNode.getValueString())
					.orElse(null);
			
			if (StringUtils.isNotBlank(conditionExpression)) {
				putItemRequest.setConditionExpression(conditionExpression);
			}

			PutItemResult result = ddb.putItem(putItemRequest);
			NodeOutput output = new NodeOutput();
			output.setNodeSpecification(input.getNodeSpecification());
			output.setOutput(JsonUtils.mapper.valueToTree(result));
			output.setNodeName(this.getClass().getName());
			output.setNodeSpecification(input.getNodeSpecification());
			return output;
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private Map<String, AttributeValue> getItemMapFromJsonNode(JsonNode jsonInput) throws Exception {
		Map<String, AttributeValue> itemValues = Maps.newHashMap();
		Iterator<String> inputIterator = jsonInput.fieldNames();
		while (inputIterator.hasNext()) {
			String eachFieldName = inputIterator.next();
			if (ACCESS_KEY.getParameterName().equals(eachFieldName)
					|| SECRET_KEY.getParameterName().equals(eachFieldName)
					|| CONDITION_EXPRESSION.getParameterName().equals(eachFieldName)
					|| TABLE_NAME.getParameterName().equals(eachFieldName)
					|| REGION.getParameterName().equals(eachFieldName))
				continue;

			ParameterData nodeData = ParameterUtils.getParameterData(jsonInput, new ParameterMetadata(eachFieldName));
			itemValues.put(eachFieldName, new AttributeValue(nodeData.getValueString()));
		}
		return itemValues;
	}
}