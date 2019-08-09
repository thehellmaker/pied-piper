package com.github.piedpiper.node.aws.dynamo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.util.internal.StringUtil;

public class DynamoDBReaderNode extends DynamoDBBaseNode {

	public static final ParameterMetadata KEY_QUERY_EXPRESSION = new ParameterMetadata("keyQueryExpression");

	public static final ParameterMetadata FILTER_QUERY_EXPRESSION = new ParameterMetadata("filterQueryExpression");
	
	/**
	 * @param input
	 */
	public NodeOutput apply(NodeInput input) {
		try {
			JsonNode jsonInput = input.getInput();
			DynamoDB dynamoDB = getDynamoDB(getAmazonDynamoDBClient(jsonInput));
			Table table = dynamoDB.getTable(getTableName(jsonInput));
			String keyQueryExpression = Optional.ofNullable(ParameterUtils.getParameterData(jsonInput, KEY_QUERY_EXPRESSION))
					.map(parameterData -> parameterData.getValueString())
					.orElse(StringUtil.EMPTY_STRING);
			String filterExpression = Optional.ofNullable(ParameterUtils.getParameterData(jsonInput, FILTER_QUERY_EXPRESSION))
					.map(parameterData -> parameterData.getValueString())
					.orElse(StringUtil.EMPTY_STRING);

			List<Item> itemList;
			if (StringUtils.isNotBlank(keyQueryExpression)) {
				// Query
				QuerySpec spec = new QuerySpec().withKeyConditionExpression(keyQueryExpression)
						.withValueMap(getValueMap(jsonInput));
				if (StringUtils.isNotBlank(filterExpression)) {
					spec.withFilterExpression(filterExpression);
				}
				ItemCollection<QueryOutcome> itemCollection = table.query(spec);
				itemList = Lists.newArrayList(itemCollection.iterator());
			} else {
				// Scan
				ScanSpec spec = new ScanSpec();
				if (StringUtils.isNotBlank(filterExpression)) {
					spec.withFilterExpression(filterExpression).withValueMap(getValueMap(jsonInput));
				}

				ItemCollection<ScanOutcome> itemCollection = table.scan(spec);
				itemList = Lists.newArrayList(itemCollection.iterator());
			}
			NodeOutput output = new NodeOutput();
			output.setNodeSpecification(input.getNodeSpecification());
			output.setOutput(
					JsonUtils.mapper.valueToTree(itemList.stream().map(item -> item.asMap()).collect(Collectors.toList())));
			return output;
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> getValueMap(JsonNode jsonInput) throws JsonProcessingException {
		Map<String, Object> stringMap = Maps.newHashMap();
		Iterator<Map.Entry<String, JsonNode>> jsonIterator = jsonInput.fields();
		while (jsonIterator.hasNext()) {
			Map.Entry<String, JsonNode> eachNode = jsonIterator.next();
			if (eachNode.getKey().startsWith(":")) {
				ParameterData data = ParameterUtils.getNodeData(eachNode.getValue().toString());
				if (Long.class.getName().equals(data.getDataTypeName()))
					stringMap.put(eachNode.getKey(), data.getValue().asLong());
				else
					stringMap.put(eachNode.getKey(), data.getValueString());
			}
		}
		return stringMap;
	}

}
