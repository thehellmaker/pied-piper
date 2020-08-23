package com.github.piedpiper.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.LambdaLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.transformer.SearchGraphFunction;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SearchGraphLambdaFunction implements RequestStreamHandler {

	private Injector injector;

	public SearchGraphLambdaFunction() {
		this(Lists.newArrayList(new PiedPiperModule()));
	}

	public SearchGraphLambdaFunction(List<AbstractModule> guiceModules) {
		injector = Guice.createInjector(guiceModules);
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		ILogger logger = new LambdaLoggerImpl(context.getLogger());
		try {
			JsonNode inputJson = JsonUtils.mapper.readTree(inputStream);
			logger.log("Input: " + inputJson);
			JsonNode searchResponse = new SearchGraphFunction(logger, injector).apply(inputJson);
			String graphJsonStr = Jackson.toJsonString(searchResponse);
			outputStream.write(graphJsonStr.getBytes());
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			outputStream.write(ExceptionUtils.getStackTrace(e).getBytes());
		}
	}

}
