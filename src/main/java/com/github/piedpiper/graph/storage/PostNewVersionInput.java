package com.github.piedpiper.graph.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PostNewVersionInput {

	private String projectName;
	private String graphName;
	private String graphJson;
	private String previousProjectName;
	private String previousGraphName;
	private Long previousVersion;
	private String versionDescription;
	private String previousBranchName;
	private String branchName;

	public PostNewVersionInput(String projectName, String graphName, String graphJson, String branchName,
			String versionDescription, String previousProjectName, String previousGraphName, String previousBranchName,
			Long previousVersion) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.graphJson = graphJson;
		this.branchName = branchName;
		this.versionDescription = versionDescription;
		this.previousProjectName = previousProjectName;
		this.previousGraphName = previousGraphName;
		this.previousBranchName = previousBranchName;
		this.previousVersion = previousVersion;
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

	public String getPreviousProjectName() {
		return previousProjectName;
	}

	public String getPreviousGraphName() {
		return previousGraphName;
	}

	public Long getPreviousVersion() {
		return previousVersion;
	}

	public String getVersionDescription() {
		return versionDescription;
	}

	public String getPreviousBranchName() {
		return previousBranchName;
	}

	public String getBranchName() {
		return branchName;
	}

}
