package com.github.piedpiper.graph.storage;

import org.apache.commons.lang3.StringUtils;

public class QueryGraphInput {

	private String projectName;
	private String graphName;
	private Long version;
	private VersionType versionType;
	private String aliasName;
	private SortType sortType;

	public QueryGraphInput(String projectName, String graphName, VersionType verstionType, Long version,
			SortType sortType) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.versionType = verstionType;
		this.version = version;
		this.sortType = sortType;
	}

	public QueryGraphInput(String projectName, String graphName, String aliasName, Long version, SortType sortType) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.sortType = sortType;
		this.versionType = VersionType.Alias;
		this.aliasName = aliasName;
		this.version = version;
	}

	public QueryGraphInput(String projectName, String graphName, VersionType versionType, String alias, Long version,
			SortType sortType) {
		this.projectName = projectName;
		this.graphName = graphName;
		this.versionType = versionType;
		this.aliasName = alias;
		this.version = version;
		this.sortType = sortType;
		if(VersionType.Alias.equals(this.versionType) && StringUtils.isBlank(this.aliasName)) {
			throw new RuntimeException("Alias cannot be blank when versionType is Alias");
		}
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

	public SortType getSortType() {
		return sortType;
	}

	public static enum SortType {
		Ascending, Descending
	}

}
