
package com.github.piedpiper.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.LambdaLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.transformer.ExecuteGraphFunction;
import com.github.piedpiper.transformer.WarmupHandler;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ExecuteGraphLambdaFunction implements RequestStreamHandler {

	private Injector injector;


	public ExecuteGraphLambdaFunction() {
		this(Lists.newArrayList(new PiedPiperModule()));
	}

	public ExecuteGraphLambdaFunction(List<AbstractModule> guiceModules) {
		injector = Guice.createInjector(guiceModules);
	}

	public ExecuteGraphLambdaFunction(Module module) {
		injector = Guice.createInjector(module);
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
			throws JsonProcessingException, IOException {
		ILogger logger = new LambdaLoggerImpl(context.getLogger());
		try {
			
			JsonNode inputJson = JsonUtils.mapper.readTree(inputStream);
			validateNotLambdaWarmUp(inputJson, logger);
			logger.log("Input Thundra: " + inputJson);
			GraphDefinition response = new ExecuteGraphFunction(logger, injector).apply(inputJson);
			logger.log("Response = " + response);
			outputStream.write(JsonUtils.mapper.writeValueAsBytes(response));
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			GraphDefinition response = new GraphDefinition();
			response.setExceptionTrace(ExceptionUtils.getStackTrace(e));
			outputStream.write(JsonUtils.mapper.writeValueAsBytes(response));
		}
	}
	
	private void validateNotLambdaWarmUp(JsonNode inputJson, ILogger logger) throws ExecutionException, InterruptedException {
		Boolean isWarmUp = Optional.ofNullable(inputJson.get(PiedPiperConstants.WARM_UP))
				.map(warmUpNode -> warmUpNode.asBoolean()).orElse(false);
		if(isWarmUp) {
			new WarmupHandler(injector, logger, false).apply(null);
			throw new RuntimeException("Warm Up function. Not Executing");
		}
	}

}
