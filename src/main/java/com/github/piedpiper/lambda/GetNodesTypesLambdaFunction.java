package com.github.piedpiper.lambda;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.commons.log.ILogger;
import com.github.commons.log.LambdaLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.transformer.GetNodesTypesFunction;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.github.piedpiper.graph.api.types.GraphDefinition;

public class GetNodesTypesLambdaFunction implements RequestStreamHandler {

	private Injector injector;

	public GetNodesTypesLambdaFunction() {
		this(Lists.newArrayList(new PiedPiperModule()));
	}

	public GetNodesTypesLambdaFunction(List<AbstractModule> guiceModules) {
		injector = Guice.createInjector(guiceModules);
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws JsonProcessingException, IOException {
		ILogger logger = new LambdaLoggerImpl(context.getLogger());
		try {
			outputStream.write(JsonUtils.mapper.writeValueAsBytes(new GetNodesTypesFunction(logger, injector).apply(null)));
		} catch (Exception e) {
			logger.log(String.format("Error: %s", ExceptionUtils.getStackTrace(e)));
			GraphDefinition response = new GraphDefinition();
			response.setExceptionTrace(ExceptionUtils.getStackTrace(e));
			outputStream.write(JsonUtils.mapper.writeValueAsBytes(response));
		}
	}

}