package com.github.piedpiper.graph.storage;

public class GraphId {
	
	private String projectName;
	private String graphName;

	public GraphId(String projectName, String graphName) {
		this.projectName = projectName;
		this.graphName = graphName;
	}

	public String getGraphName() {
		return graphName;
	}

	public String getProjectName() {
		return projectName;
	}
	
}
