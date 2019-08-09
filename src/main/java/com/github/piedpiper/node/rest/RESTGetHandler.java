 package com.github.piedpiper.node.rest;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;

public class RESTGetHandler extends BaseRestHandler {
	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			String outputType = getOutputType(input);

			GetRequest getRequest = Unirest.get(getUrl(input));
			
			JsonNode jsonHeader = getHeaders(input);
			getRequest = (GetRequest) populateHeaders(getRequest, jsonHeader);
			
			HttpResponse<?> response = (HttpResponse<?>) MethodUtils.invokeExactMethod(getRequest, "asString");
			ObjectNode node = (ObjectNode) JsonUtils.mapper.valueToTree(response);
			if (outputType.equals(PiedPiperConstants.AS_JSON))
				parseAndUpdateBodyAsJson(node);
			
			NodeOutput output = new NodeOutput();
			output.setOutput(node);
			return output;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
