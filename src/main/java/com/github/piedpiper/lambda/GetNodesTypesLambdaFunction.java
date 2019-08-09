package com.github.piedpiper.lambda;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.commons.log.ILogger;
import com.github.commons.log.LambdaLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.transformer.GetNodesTypesFunction;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GetNodesTypesLambdaFunction implements RequestHandler<Object, String> {

	private Injector injector;

	public GetNodesTypesLambdaFunction() {
		this(Lists.newArrayList(new PiedPiperModule()));
	}

	public GetNodesTypesLambdaFunction(List<AbstractModule> guiceModules) {
		injector = Guice.createInjector(guiceModules);
	}

	@Override
	public String handleRequest(Object input, Context context) {
		ILogger logger = new LambdaLoggerImpl(context.getLogger());
		logger.log("Input: " + input);
		try {
			return JsonUtils.writeValueAsStringSilent(new GetNodesTypesFunction(logger, injector).apply(null));
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			return ExceptionUtils.getStackTrace(e);
		}
	}

}
