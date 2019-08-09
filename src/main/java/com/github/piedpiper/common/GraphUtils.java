package com.github.piedpiper.common;

public class GraphUtils {

	public static String getRangeKeyEquals(String projectName, String graphName) {
		return String.format("%s/%s", projectName, graphName);
	}

}
