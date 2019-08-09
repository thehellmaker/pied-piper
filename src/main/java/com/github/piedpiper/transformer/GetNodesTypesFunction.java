package com.github.piedpiper.transformer;

import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeListQueryHandler;
import com.github.piedpiper.node.NodeMetadata;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class GetNodesTypesFunction implements Function<Void, JsonNode> {
	private Injector injector;
	private ILogger logger;


	public GetNodesTypesFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
	}

	@Override
	public JsonNode apply(Void aVoid) {

		@SuppressWarnings("unchecked")
		Function<Class<?>, NodeMetadata> nodeMetadataQueryFunction = injector
				.getInstance(Key.get(Function.class, Names.named(NodeListQueryHandler.class.getName())));
		try {
			return JsonUtils.mapper.valueToTree(nodeMetadataQueryFunction.apply(null));
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

}
