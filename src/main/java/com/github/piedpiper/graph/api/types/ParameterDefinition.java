package com.github.piedpiper.graph.api.types;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterDefinition {

	/**
	 * This is the name of the parameter which is defined in the graph json
	 */
	private String parameterName;

	/**
	 * This defines the type of parameter if its a constant or runtime binding or if
	 * it has to be resolved from another node
	 */
	private ParameterType parameterType;
	
	private ParameterFormat parameterFormat;

	/**
	 * The values of {@link ParameterDefinition#parameterValue},
	 * {@link ParameterDefinition#referenceNodeName} and
	 * {@link ParameterDefinition#resolvedParameterValue} vary for different
	 * {@link ParameterType}.
	 * <p>
	 * <li>
	 * <ul>
	 * {@link ParameterType#CONSTANT} parameterValue = Constant referenceNodeName =
	 * null
	 * </ul>
	 * <ul>
	 * {@link ParameterType#REFERENCE_FROM_ANOTHER_NODE} parameterValue = JsonPath
	 * to be applied on referenceNode output to get resolvedParameterValue
	 * referenceNodeName = name of the reference node on which json path stored in
	 * parameterValue is to be applied.
	 * </ul>
	 * </li>
	 * </p>
	 */
	private String parameterValue;

	private String referenceNodeName;

	private Map<String, String> attributeMap;

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public String getReferenceNodeName() {
		return referenceNodeName;
	}

	public void setReferenceNodeName(String referenceNodeName) {
		this.referenceNodeName = referenceNodeName;
	}

	public ParameterFormat getParameterFormat() {
		return parameterFormat;
	}

	public void setParameterFormat(ParameterFormat parameterFormat) {
		this.parameterFormat = parameterFormat;
	}

}
