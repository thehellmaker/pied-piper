package com.github.commons.utils;

import java.io.IOException;

import com.github.commons.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {
	public static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
	public static String writeValueAsStringSilent(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new ValidationException(e.getMessage(), e);
		}
	}

	public static <T> T readValueSilent(String fieldNodeStr, Class<T> clazz) {
		try {
			return mapper.readValue(fieldNodeStr, clazz);
		} catch (IOException e) {
			throw new ValidationException(e.getMessage(), e);
		}
	}

}
