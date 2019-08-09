package com.github.piedpiper.node;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.commons.utils.JsonUtils;

public class ParameterData {

	private JsonNode value;

	private String dataTypeName;

	public ParameterData() {
		this.value = new ObjectMapper().createObjectNode().textNode(StringUtils.EMPTY);
	}

	public JsonNode getValue() {
		return value;
	}

	public String getValueString() {
		if(value instanceof TextNode) {
			return value.asText();
		} else {
			try {
				return JsonUtils.mapper.writeValueAsString(value);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return StringUtils.EMPTY;
			}
		}
		
	}

	public void setValue(JsonNode value) {
		this.value = value;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

}
