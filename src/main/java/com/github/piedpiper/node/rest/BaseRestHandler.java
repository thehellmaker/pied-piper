package com.github.piedpiper.node.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.utils.ParameterUtils;
import com.mashape.unirest.request.HttpRequest;

import io.netty.util.internal.StringUtil;

public abstract class BaseRestHandler implements Function<NodeInput, NodeOutput> {

	protected String getUrl(NodeInput input) throws Exception {
		return ParameterUtils.getParameterData(input.getInput(), RESTServiceNode.URL).getValueString();
	}

	protected JsonNode getHeaders(NodeInput input) throws Exception {
		return Optional.ofNullable(ParameterUtils.getParameterData(input.getInput(), RESTServiceNode.HEADERS))
				.map(headerParamData -> headerParamData.getValue()).orElse(JsonUtils.mapper.readTree("{}"));

	}

	protected String getBody(NodeInput input) throws Exception {
		return Optional.ofNullable(ParameterUtils.getParameterData(input.getInput(), RESTServiceNode.BODY))
				.map(headerParamData -> headerParamData.getValueString()).orElse(StringUtil.EMPTY_STRING);
	}

	protected void parseAndUpdateBodyAsJson(ObjectNode node) throws IOException {
		String bodyStr = node.get(PiedPiperConstants.BODY).asText();
		JsonNode bodyJackson = JsonUtils.mapper.readTree(bodyStr);
		node.set(PiedPiperConstants.BODY, bodyJackson);
	}

	protected HttpRequest populateHeaders(HttpRequest request, JsonNode jsonHeader) {
		Iterator<Entry<String, JsonNode>> jsonHeaderIterator = jsonHeader.fields();
		while (jsonHeaderIterator.hasNext()) {
			Entry<String, JsonNode> header = jsonHeaderIterator.next();
			request = request.header(header.getKey(), header.getValue().asText());
		}
		return request;
	}

	protected String getOutputType(NodeInput input) throws Exception {
		return Optional.ofNullable(ParameterUtils.getParameterData(input.getInput(), RESTServiceNode.OUTPUT_TYPE))
				.map(parameterData -> parameterData.getValueString())
				.filter(outputTypeStr -> StringUtils.isNotBlank(outputTypeStr)).orElse(PiedPiperConstants.AS_STRING);
	}

}
