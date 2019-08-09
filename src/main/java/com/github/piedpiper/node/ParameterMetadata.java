package com.github.piedpiper.node;

import java.util.ArrayList;
import java.util.List;


public class ParameterMetadata {

	public static boolean MANDATORY = Boolean.TRUE;

	public static boolean OPTIONAL = Boolean.FALSE;
	
	private String parameterName;

	private boolean isRequired;
	
	private List<String> allowedValues;

	public ParameterMetadata(String parameterName) {
		this(parameterName, OPTIONAL);
	}
	
	public ParameterMetadata(String parameterName, boolean isRequired) {
		this(parameterName, isRequired, new ArrayList<String>());
	}
	
	public ParameterMetadata(String parameterName, boolean isRequired, List<String> allowedValues) {
		this.parameterName = parameterName;
		this.isRequired = isRequired;
		this.allowedValues = allowedValues;
	}

	public String getParameterName() {
		return parameterName;
	}
	
	public boolean isRequired() {
		return isRequired;
	}
	
	public List<String> getAllowedValues() {
		return allowedValues;
	}

}
