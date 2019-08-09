package com.github.piedpiper.graph.api.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NodeSpecification {

	private boolean includeOutput = true;
	
	private boolean failOnAnyError = false;

	public boolean isIncludeOutput() {
		return includeOutput;
	}

	public void setIncludeOutput(boolean includeOutput) {
		this.includeOutput = includeOutput;
	}

	public boolean isFailOnAnyError() {
		return failOnAnyError;
	}

	public void setFailOnAnyError(boolean failOnAnyError) {
		this.failOnAnyError = failOnAnyError;
	}
	
}
