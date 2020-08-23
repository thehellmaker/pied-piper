package com.github.piedpiper.graph.storage.dynamo;

import com.github.piedpiper.graph.AliasType;

public class PutNewAliasInput {

	private String projectName;
	private String graphName;
	private String versionDescription;
	private String branchName;
	private AliasType aliasType;
	private Long version;

	public PutNewAliasInput(String projectName, String graphName, String branchName, Long version, AliasType aliasType,
			String versionDescription) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.branchName = branchName;
		this.version = version;
		this.aliasType = aliasType;
		this.versionDescription = versionDescription;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getGraphName() {
		return graphName;
	}

	public String getVersionDescription() {
		return versionDescription;
	}

	public String getBranchName() {
		return branchName;
	}

	public AliasType getAliasType() {
		return aliasType;
	}

	public Long getVersion() {
		return version;
	}

}
