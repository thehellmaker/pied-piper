package com.github.piedpiper.node;

import java.util.List;

public class NodeMetadata {

	private Class<?> nodeClass;
	
	private List<ParameterMetadata> parameterMetadataList;

	public Class<?> getNodeClass() {
		return nodeClass;
	}

	public void setNodeClass(Class<?> nodeClass) {
		this.nodeClass = nodeClass;
	}

	public List<ParameterMetadata> getParameterMetadataList() {
		return parameterMetadataList;
	}

	public void setParameterMetadataList(List<ParameterMetadata> parameterMetadataList) {
		this.parameterMetadataList = parameterMetadataList;
	}
	
}
