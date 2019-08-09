package com.github.piedpiper.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.piedpiper.exception.IncorrectParameterTypeException;
import com.github.piedpiper.graph.api.DependentNodeException;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.graph.api.types.NodeStatus;
import com.github.piedpiper.graph.api.types.ParameterDefinition;
import com.github.piedpiper.graph.api.types.ParameterType;
import com.github.piedpiper.node.NodeOutput;
import com.google.common.collect.Lists;

public class GraphDefinitionUtils {

	private static List<String> getDependentNodeExecutionError(NodeDefinition nodeDefinition) {
		List<NodeOutput> nodeOutputList = nodeDefinition.getNodeOutputList();

		if (CollectionUtils.isEmpty(nodeOutputList))
			return Lists.newArrayList();

		List<String> stackTraceList = Lists.newArrayList();

		for (NodeOutput output : nodeOutputList) {
			if (StringUtils.isNotBlank(output.getStackTrace())) {
				stackTraceList.add(output.getStackTrace());
			}
		}

		if (StringUtils.isNotBlank(nodeDefinition.getStackTrace())) {
			stackTraceList.add(nodeDefinition.getStackTrace());
		}

		if (CollectionUtils.isEmpty(stackTraceList)) {
			return Lists.newArrayList();
		} else {
			return stackTraceList;
		}
	}

	public static boolean isNodeExecutionError(NodeDefinition nodeDef) {
		return nodeDef.getNodeStatus() == NodeStatus.COMPLETE
				&& CollectionUtils.isNotEmpty(getDependentNodeExecutionError(nodeDef));
	}

	public static boolean isNodeExecutionSuccess(NodeDefinition nodeDef) {
		return nodeDef.getNodeStatus() == NodeStatus.COMPLETE && !isNodeExecutionError(nodeDef);
	}

	public static boolean isGraphExecutionComplete(GraphDefinition graphDefinition) throws JsonProcessingException {
		Map<String, NodeDefinition> nodeMap = graphDefinition.getNodeMap();
		for (Entry<String, NodeDefinition> nodeEntry : nodeMap.entrySet()) {
			if (nodeEntry.getValue().getNodeStatus() != NodeStatus.COMPLETE) {
				return false;
			}
		}
		return true;
	}

	public static boolean isDependentParameterResolved(GraphDefinition graphDefinition, ParameterDefinition paramDef) {
		if (!ParameterType.REFERENCE_FROM_ANOTHER_NODE.equals(paramDef.getParameterType()))
			throw new IncorrectParameterTypeException(String.format(
					"ParameterName = %s(ParameterType = %s) is not of type %s", paramDef.getParameterName(),
					paramDef.getParameterType(), ParameterType.REFERENCE_FROM_ANOTHER_NODE));
		String nodeName = paramDef.getReferenceNodeName();
		NodeDefinition nodeDef = graphDefinition.getNodeMap().get(nodeName);
		if(nodeDef.getNodeStatus() != NodeStatus.COMPLETE) return false;
		
		if (GraphDefinitionUtils.isNodeExecutionError(nodeDef)) {
			throw new DependentNodeException(String.format(
					"The node execution has failed because a dependentNode: %s errored out during execution",
					nodeName));
		}
		return true;
	}
	
	public static boolean isResolved(GraphDefinition graphDefinition, NodeDefinition nodeDef) throws JsonProcessingException {

		Map<String, ParameterDefinition> paramMap = nodeDef.getParameterMap();
		if (MapUtils.isEmpty(paramMap))
			return true;
		for (Entry<String, ParameterDefinition> paramEntry : paramMap.entrySet()) {
			if (!isParameterResolved(graphDefinition, paramEntry)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isParameterResolved(GraphDefinition graphDefinition, Entry<String, ParameterDefinition> paramEntry) {
		ParameterType type = paramEntry.getValue().getParameterType();
		return ParameterType.CONSTANT.equals(type) || ParameterType.AWS_SSM.equals(type)
				|| isDependentParameterResolved(graphDefinition, paramEntry.getValue());

	}

}
