package com.github.piedpiper.graph.storage;

import java.util.Map;

public final class PutGraphVersionInput {

	private String projectName;
	private String graphName;
	private String graphJson;
	private long version;
	private VersionType versionType;
	private String alias;
	private Map<String, Object> attributes;

	public PutGraphVersionInput(String projectName, String graphName, String graphJson, VersionType versionType,
			Long version, Map<String, Object> attributes) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.graphJson = graphJson;
		this.versionType = versionType;
		this.version = version;
		this.attributes = attributes;
	}
	
	public PutGraphVersionInput(String projectName, String graphName, String graphJson, String alias,
			Long version, Map<String, Object> attributes) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.graphJson = graphJson;
		this.attributes = attributes;
		this.versionType = VersionType.Alias;
		this.alias = alias;
		this.version = version;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getGraphName() {
		return graphName;
	}

	public String getGraphJson() {
		return graphJson;
	}

	public long getVersion() {
		return version;
	}
	
	public VersionType getVersionType() {
		return versionType;
	}

	public String getAlias() {
		return alias;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

}
