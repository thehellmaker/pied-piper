package com.github.piedpiper.node.rest;

import java.util.Optional;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;

public abstract class BaseBodyRestHandler extends BaseRestHandler {

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			String body = getBody(input);
			String outputType = getOutputType(input);
			HttpRequestWithBody requestWithBody = getRequestWithBody(input);
			
			JsonNode jsonHeader = getHeaders(input);
			requestWithBody = (HttpRequestWithBody) populateHeaders(requestWithBody, jsonHeader);

			RequestBodyEntity requestBodyEntity = requestWithBody.body(body);
			
			String outputMethod = Optional.ofNullable(outputType).orElse("asString");
			HttpResponse<?> response = (HttpResponse<?>) MethodUtils.invokeExactMethod(requestBodyEntity, outputMethod);
			ObjectNode outputNode = (ObjectNode) JsonUtils.mapper.valueToTree(response);
			if(outputMethod.equals(PiedPiperConstants.AS_JSON)) {
				JsonNode bodyNode = JsonUtils.mapper.readTree(response.getBody().toString());
				outputNode.set(RESTServiceNode.BODY.getParameterName(), bodyNode);
			}
			NodeOutput output = new NodeOutput();
			output.setOutput(outputNode);
			return output;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract HttpRequestWithBody getRequestWithBody(NodeInput input) throws Exception;

}
