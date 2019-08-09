package com.github.piedpiper.graph.api.types;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown=true)
public class GraphDefinition {

	private String projectName;

	private String graphName;
	
	private Map<String, NodeDefinition> nodeMap;
	
	private AuditInfo auditInfo;
	
	private String exceptionTrace;
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	public Map<String, NodeDefinition> getNodeMap() {
		return nodeMap;
	}

	public void setNodeMap(Map<String, NodeDefinition> nodeList) {
		this.nodeMap = nodeList;
	}

	public String getExceptionTrace() {
		return exceptionTrace;
	}

	public void setExceptionTrace(String exceptionTrace) {
		this.exceptionTrace = exceptionTrace;
	}

	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	public void setAuditInfo(AuditInfo auditInfo) {
		this.auditInfo = auditInfo;
	}

}
