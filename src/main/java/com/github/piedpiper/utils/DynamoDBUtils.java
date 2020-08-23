package com.github.piedpiper.utils;

public class DynamoDBUtils {

	public static String formatWithLongMaxLength(long number) {
		long longMaxLength = String.valueOf(Long.MAX_VALUE).length();
		return String.format("%0"+longMaxLength+"d", number);
	}
	
}
