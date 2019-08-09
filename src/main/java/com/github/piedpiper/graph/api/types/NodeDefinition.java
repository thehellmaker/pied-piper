package com.github.piedpiper.graph.api.types;

import java.util.List;
import java.util.Map;

import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.common.collect.Lists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NodeDefinition {

	private String nodeName;

	private String nodeClass;
	
	private NodeSpecification nodeSpecification;
	
	private Map<String, ParameterDefinition> parameterMap;
	
	private NodeStatus nodeStatus = NodeStatus.NOT_STARTED;
	
	private List<NodeInput> nodeInputList = Lists.newArrayList();

	private List<NodeExecutor> nodeExecutorList = Lists.newArrayList();
	
	private List<NodeOutput> nodeOutputList = Lists.newArrayList();
	
	private String stackTrace;

	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeClass() {
		return nodeClass;
	}

	public void setNodeClass(String nodeClass) {
		this.nodeClass = nodeClass;
	}

	public Map<String, ParameterDefinition> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, ParameterDefinition> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public NodeSpecification getNodeSpecification() {
		return nodeSpecification;
	}

	public void setNodeSpecification(NodeSpecification nodeSpecification) {
		this.nodeSpecification = nodeSpecification;
	}

	public List<NodeOutput> getNodeOutputList() {
		return nodeOutputList;
	}

	public void setNodeOutputList(List<NodeOutput> nodeOutputList) {
		this.nodeOutputList = nodeOutputList;
	}
	
	public void appendNodeOutput(NodeOutput nodeOutput) {
		this.nodeOutputList.add(nodeOutput);
	}

	public List<NodeInput> getNodeInputList() {
		return nodeInputList;
	}

	public void setNodeInputList(List<NodeInput> nodeInputList) {
		this.nodeInputList = nodeInputList;
	}

	public List<NodeExecutor> getNodeExecutorList() {
		return nodeExecutorList;
	}

	public void setNodeExecutorList(List<NodeExecutor> nodeExecutorList) {
		this.nodeExecutorList = nodeExecutorList;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public NodeStatus getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(NodeStatus nodeStatus) {
		this.nodeStatus = nodeStatus;
	}

}
