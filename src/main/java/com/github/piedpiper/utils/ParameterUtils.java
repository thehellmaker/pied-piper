package com.github.piedpiper.utils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.ParameterDefinition;
import com.github.piedpiper.graph.api.types.ParameterType;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.google.common.collect.Lists;

public class ParameterUtils {

	public static ParameterData getParameterData(JsonNode input, ParameterMetadata metadata) throws Exception {
		Optional<ParameterData> stringOptional = Optional.ofNullable(input)
				.map(node -> node.get(metadata.getParameterName())).map(fieldNode -> fieldNode.toString())
				.filter(fieldNodeStr -> StringUtils.isNotBlank(fieldNodeStr))
				.map(fieldNodeStr -> JsonUtils.readValueSilent(fieldNodeStr, ParameterData.class))
				.filter(fieldValueNode -> fieldValueNode.getValue() != null);

		if (stringOptional.isPresent()) {
			return stringOptional.get();
		} else if (metadata.isRequired()) {
			throw new IllegalArgumentException(
					String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, metadata.getParameterName()));
		}
		return null;
	}

	public static ParameterData getNodeData(String nodeStr) {
		return JsonUtils.readValueSilent(nodeStr, ParameterData.class);
	}
	
	public static String resolveAWSSSMParameter(AWSSimpleSystemsManagement ssmClient, String paramName) {
		GetParameterResult result = ssmClient
				.getParameter(new GetParameterRequest().withName(paramName).withWithDecryption(true));
		return result.getParameter().getValue();
	}
	
	public static String getDynamoParamPlaceHolderName(String paramName) {
		return ":" + paramName.replaceAll("[\\W]|_", "");
	}
	
	public static boolean isConstant(ParameterDefinition paramDef) {
		return ParameterType.CONSTANT.equals(paramDef.getParameterType());
	}
	
	public static boolean isNotConstant(ParameterDefinition paramDef) {
		return !isConstant(paramDef);
	}

	public static String bytesToStringWithStrippedQuotesIfPresent(ByteBuffer payload) throws Exception {
		String payloadStr = new String(payload.array(), "UTF-8").trim();
		if (StringUtils.startsWith(payloadStr, "\"") && StringUtils.endsWith(payloadStr, "\""))
			return payloadStr.substring(1, payloadStr.length() - 1);
		else
			return payloadStr;
	}

	public static JsonNode createParamValueNode(Object paramValue) {
		ObjectNode paramValueNode = JsonUtils.mapper.createObjectNode();
		paramValueNode.set("value", JsonUtils.mapper.valueToTree(paramValue));
		return paramValueNode;
	}
	
	public static NodeInput createNodeInput(JsonNode inputJson) {
		List<String> fieldNameList = Lists.newArrayList(inputJson.fieldNames());
		ObjectNode nodeInputJson = JsonUtils.mapper.createObjectNode();
		for(String key: fieldNameList) {
			JsonNode eachValue = inputJson.get(key);
			nodeInputJson.set(key, createParamValueNode(eachValue));
		}
		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(nodeInputJson);
		return nodeInput;
	}
	
	
}
