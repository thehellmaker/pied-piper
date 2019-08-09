package com.github.piedpiper.utils;

import java.util.regex.Pattern;

public class RegExUtils {

	public static boolean isSubstitutorString(String input) {
		return Pattern.matches("\\$\\{(.*?)}", input); 
	}
	
	public static boolean isNotSubstitutorString(String input) {
		return !isSubstitutorString(input);
	}

}
