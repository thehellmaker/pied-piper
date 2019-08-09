package com.github.piedpiper.guice;

import com.github.commons.log.ILogger;
import com.github.piedpiper.transformer.ExecuteGraphFunction;
import com.google.inject.Injector;

public interface PiedPiperFactory {

	ExecuteGraphFunction create(ILogger logger, Injector injector);
	
}
