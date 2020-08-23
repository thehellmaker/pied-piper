package com.github.piedpiper.graph.storage;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class PutGraphVersionInput {

	private String projectName;
	private String graphName;
	
	private String graphJson;
	private String version;
	private String alias;
	private Map<String, Object> attributes;
	private String branchName;

	public PutGraphVersionInput(String projectName, String graphName, String graphJson, String branchName,
			String version, Map<String, Object> attributes) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.graphJson = graphJson;
		this.branchName = branchName;
		this.version = version;
		this.attributes = attributes;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public String getGraphName() {
		return graphName;
	}
	@JsonIgnore
	public String getGraphJson() {
		return graphJson;
	}

	public String getVersion() {
		return version;
	}
	
	public String getAlias() {
		return alias;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public String getBranchName() {
		return branchName;
	}

}
