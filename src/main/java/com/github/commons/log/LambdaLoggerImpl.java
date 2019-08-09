package com.github.commons.log;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Created by aashok on 11/28/2017.
 */
public class LambdaLoggerImpl implements ILogger {


    private LambdaLogger logger;

    public LambdaLoggerImpl(LambdaLogger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String msg) {
        if(logger != null) logger.log("\n"+msg);
    }
}
