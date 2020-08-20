package com.github.piedpiper.graph.storage;

public class GetGraphVersionInput {

	private String projectName;
	private String graphName;
	private Long version;
	private VersionType versionType;
	private String aliasName;

	public GetGraphVersionInput(String projectName, String graphName, VersionType verstionType, Long version) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.versionType = verstionType;
		this.version = version;
	}
	
	public GetGraphVersionInput(String projectName, String graphName, String aliasName, Long version) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.versionType = VersionType.Alias;
		this.aliasName = aliasName;
		this.version = version;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public String getGraphName() {
		return graphName;
	}

	public Long getVersion() {
		return version;
	}
	
	public VersionType getVersionType() {
		return versionType;
	}

	public String getAliasName() {
		return aliasName;
	}
	
	
	
}
