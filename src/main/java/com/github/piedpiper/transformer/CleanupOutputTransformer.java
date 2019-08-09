package com.github.piedpiper.transformer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.graph.api.types.ParameterDefinition;
import com.github.piedpiper.graph.api.types.ParameterType;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.ssm.AWSSSMNode;
import com.google.common.collect.Lists;

public class CleanupOutputTransformer implements Function<GraphDefinition, GraphDefinition> {

	public static final String MASKED_VALUE = "******Masked For Security Reasons******";

	@Override
	public GraphDefinition apply(GraphDefinition graphDefinition) {
		try {
			GraphDefinition clonedGraphDefinition = JsonUtils.mapper
					.readValue(JsonUtils.mapper.writeValueAsString(graphDefinition), GraphDefinition.class);
			List<NodeDefinition> subGraphNodes = Lists.newArrayList();
			if (MapUtils.isNotEmpty(clonedGraphDefinition.getNodeMap())) {
				for (Entry<String, NodeDefinition> eachNode : clonedGraphDefinition.getNodeMap().entrySet()) {
					NodeDefinition nodeDefinition = eachNode.getValue();
					if (nodeDefinition.getNodeClass().equals("com.github.piedpiper.node.piedpiper.SubGraphNode")) {
						subGraphNodes.add(nodeDefinition);
					}
					cleanupAWSSSMParameters(graphDefinition, nodeDefinition);
				}
			}
			for (NodeDefinition eachSubGraphNode : subGraphNodes) {
				for (NodeOutput nodeOutput : eachSubGraphNode.getNodeOutputList()) {
					if(nodeOutput.getOutput() == null || nodeOutput.getOutput() instanceof NullNode) continue;
					ObjectNode outputNode = (ObjectNode) nodeOutput.getOutput();
					GraphDefinition subGraphOutput = JsonUtils.mapper.treeToValue(outputNode.get("body"),
							GraphDefinition.class);
					GraphDefinition maskedGraphDefinition = apply(subGraphOutput);
					outputNode.set("body", JsonUtils.mapper.valueToTree(maskedGraphDefinition));
				}
			}
			if (MapUtils.isNotEmpty(clonedGraphDefinition.getNodeMap())) {
				Iterator<Entry<String, NodeDefinition>> iterator = clonedGraphDefinition.getNodeMap().entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, NodeDefinition> eachNode = iterator.next();
					NodeDefinition nodeDefinition = eachNode.getValue();
					if (nodeDefinition.getNodeSpecification() != null && nodeDefinition.getNodeSpecification().isIncludeOutput() == false) {
						iterator.remove();
					}
				}
			}
			
			return clonedGraphDefinition;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void cleanupAWSSSMParameters(GraphDefinition graphDefinition, NodeDefinition nodeDefinition) throws JsonProcessingException {
		if(MapUtils.isEmpty(nodeDefinition.getParameterMap())) return;
		if(nodeDefinition.getNodeClass().equals(AWSSSMNode.class.getName())) {
			List<NodeOutput> nodeOutputList = nodeDefinition.getNodeOutputList();
			for(NodeOutput output: nodeOutputList) {
				((ObjectNode)output.getOutput()).put(AWSSSMNode.SECURE_VALUE, MASKED_VALUE);
			}
		} else {
			for (Entry<String, ParameterDefinition> eachParamDef : nodeDefinition.getParameterMap().entrySet()) {
				ParameterDefinition paramDef = eachParamDef.getValue();
				if (paramDef.getParameterType().equals(ParameterType.AWS_SSM) || isParameterReferencingAWSSSM(graphDefinition, paramDef))  {
					for (NodeInput eachInput : nodeDefinition.getNodeInputList()) {
						clearAWSSSMParameter(eachInput, paramDef);
					}
				}
			}
		}
	}

	private boolean isParameterReferencingAWSSSM(GraphDefinition graphDefinition, ParameterDefinition paramDef) {
		if(paramDef.getParameterType().equals(ParameterType.REFERENCE_FROM_ANOTHER_NODE)) {
			String referenceNodeName = paramDef.getReferenceNodeName();
			NodeDefinition nodeDef = graphDefinition.getNodeMap().get(referenceNodeName);
			// nodeDef can be null if the node is not included in the output and hence this check 
			// is absolutely required
			if(nodeDef != null && nodeDef.getNodeClass().equals(AWSSSMNode.class.getName())) {
				return true;
			}
		}
		return false;
	}

	protected void clearAWSSSMParameter(NodeInput nodeInput, ParameterDefinition paramDef)
			throws JsonProcessingException {
		JsonNode jsonInput = nodeInput.getInput();
		ObjectNode paramToBeMaskedNode = (ObjectNode) jsonInput.get(paramDef.getParameterName());
		paramToBeMaskedNode.put("value", MASKED_VALUE);
	}

}
