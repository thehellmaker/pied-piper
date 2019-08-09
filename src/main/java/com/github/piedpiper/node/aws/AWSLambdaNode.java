package com.github.piedpiper.node.aws;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;

public class AWSLambdaNode extends AWSNode {

	public static final ParameterMetadata PAYLOAD = new ParameterMetadata("payload", ParameterMetadata.OPTIONAL);

	public static final ParameterMetadata FUNCTION_NAME = new ParameterMetadata("functionName",
			ParameterMetadata.MANDATORY);
	
	public static final ParameterMetadata REGION = new ParameterMetadata("region", ParameterMetadata.MANDATORY);

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			String region = ParameterUtils.getParameterData(input.getInput(), AWSNode.REGION).getValueString();
			AWSLambda client = null;
			try {
				String accessKey = ParameterUtils.getParameterData(input.getInput(), ACCESS_KEY).getValueString();
				String secretKey = ParameterUtils.getParameterData(input.getInput(), SECRET_KEY).getValueString();
				client = injector.getInstance(ILambdaFactory.class).createLambdaClient(accessKey, secretKey, region);
			} catch(IllegalArgumentException e) {
				logger.log(String.format("Exception getting access or secretKey: %s", ExceptionUtils.getStackTrace(e)));
				client = injector.getInstance(ILambdaFactory.class).createLambdaClient(region);
			}
			
			String functionName = ParameterUtils.getParameterData(input.getInput(), FUNCTION_NAME).getValueString();
			JsonNode payload = ParameterUtils.getParameterData(input.getInput(), PAYLOAD).getValue();
			InvokeRequest request = new InvokeRequest().withFunctionName(functionName)
					.withPayload(JsonUtils.writeValueAsStringSilent(payload));
			InvokeResult result = client.invoke(request);
			String payloadStr = ParameterUtils.bytesToStringWithStrippedQuotesIfPresent(result.getPayload());
			ObjectNode resultJson = JsonUtils.mapper.valueToTree(result);
			try {
				resultJson.set(PiedPiperConstants.JSON_PAYLOAD, JsonUtils.mapper.readTree(payloadStr));
			} catch(Exception e) {
				logger.log(String.format("Unable to parse payload as Json, Ignoring payload: %s", payloadStr));
			}
			
			resultJson.put(PiedPiperConstants.STRING_PAYLOAD, StringEscapeUtils.unescapeJson(payloadStr));
			NodeOutput output = new NodeOutput();
			output.setNodeSpecification(input.getNodeSpecification());
			output.setOutput(resultJson);
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
