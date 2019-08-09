package com.github.piedpiper.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.piedpiper.graph.api.types.AuditInfo;
import com.github.piedpiper.graph.api.types.NodeSpecification;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NodeOutput {

	private String nodeName;
	
	private NodeSpecification nodeSpecification;
	
	private JsonNode output;
	
	private String stackTrace;
	
	private AuditInfo auditInfo;

	public JsonNode getOutput() {
		return output;
	}

	public void setOutput(JsonNode output) {
		this.output = output;
	}

	public NodeSpecification getNodeSpecification() {
		return nodeSpecification;
	}

	public void setNodeSpecification(NodeSpecification nodeSpecification) {
		this.nodeSpecification = nodeSpecification;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	public void setAuditInfo(AuditInfo auditInfo) {
		this.auditInfo = auditInfo;
	}

}
