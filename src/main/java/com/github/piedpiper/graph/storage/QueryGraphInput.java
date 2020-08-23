package com.github.piedpiper.graph.storage;

import com.github.piedpiper.graph.AliasType;

public class QueryGraphInput {

	private String projectName;
	private String graphName;
	private Long version;
	private SortType sortType;
	private String branchName;
	private AliasType alias;

	public QueryGraphInput(String projectName, String graphName, String branchName, Long version,
			SortType sortType) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.branchName = branchName;
		this.version = version;
		this.sortType = sortType;
	}
	
	public QueryGraphInput(String projectName, String graphName, String branchName, AliasType alias,
			SortType sortType) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.branchName = branchName;
		this.alias = alias;
		this.sortType = sortType;
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

	public SortType getSortType() {
		return sortType;
	}

	public String getBranchName() {
		return branchName;
	}

	public AliasType getAlias() {
		return alias;
	}

	public static enum SortType {
		Ascending, Descending
	}

}
