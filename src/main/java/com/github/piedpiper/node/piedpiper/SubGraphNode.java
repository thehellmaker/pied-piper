package com.github.piedpiper.node.piedpiper;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.transformer.ExecuteGraphFunction;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class SubGraphNode extends BaseNode {

	public static final ParameterMetadata PROJECT_NAME = new ParameterMetadata(PiedPiperConstants.PROJECT_NAME,
			ParameterMetadata.MANDATORY);

	public static final ParameterMetadata GRAPH_NAME = new ParameterMetadata(PiedPiperConstants.GRAPH_NAME,
			ParameterMetadata.MANDATORY);

	public static final ParameterMetadata TABLE_NAME = new ParameterMetadata(PiedPiperConstants.TABLE_NAME_PARAMETER,
			ParameterMetadata.OPTIONAL);

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			// TODO: Change to RESTNode so that this moves to a rest call.. In non lambda
			// deployment scenario
			// it will cleanly abstract interfaces through the REST Interface
			String projectName = ParameterUtils.getParameterData(input.getInput(), PROJECT_NAME).getValueString();
			String graphName = ParameterUtils.getParameterData(input.getInput(), GRAPH_NAME).getValueString();
			String tableName = Optional.ofNullable(ParameterUtils.getParameterData(input.getInput(), TABLE_NAME))
					.map(parameterData -> parameterData.getValueString())
					.orElse(PiedPiperConstants.ALMIGHTY_TABLE_NAME);
			ObjectNode inputFieldNode = JsonUtils.mapper.createObjectNode();
			JsonNode inputJsonNode = input.getInput();
			for (String fieldName : Lists.newArrayList(inputJsonNode.fieldNames())) {
				JsonNode value = ParameterUtils.getParameterData(inputJsonNode, new ParameterMetadata(fieldName))
						.getValue();
				inputFieldNode.set(fieldName, value);
			}

			ObjectNode executeGraphPayload = JsonUtils.mapper.createObjectNode();
			executeGraphPayload.put(DynamoDBBaseNode.TABLE_NAME.getParameterName(), tableName);
			executeGraphPayload.put(PiedPiperConstants.PROJECT_NAME, projectName);
			executeGraphPayload.put(PiedPiperConstants.GRAPH_NAME, graphName);
			executeGraphPayload.set(PiedPiperConstants.INPUT, inputFieldNode);

			ExecuteGraphFunction graphFunction = getExecuteGraphFunction(logger, injector);
			GraphDefinition graphDefinition = graphFunction.apply(executeGraphPayload);
			NodeOutput output = new NodeOutput();
			ObjectNode outputJson = JsonUtils.mapper.createObjectNode();
			outputJson.set("body", JsonUtils.mapper.valueToTree(graphDefinition));
			output.setOutput(outputJson);
			return output;
		} catch (Exception e) {
			logger.log(ExceptionUtils.getMessage(e));
			throw new RuntimeException(e);
		}
	}

	protected ExecuteGraphFunction getExecuteGraphFunction(ILogger logger, Injector injector) {
		return new ExecuteGraphFunction(logger, injector);
	}

}
