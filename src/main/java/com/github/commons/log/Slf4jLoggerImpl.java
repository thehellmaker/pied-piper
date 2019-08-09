package com.github.commons.log;

public class Slf4jLoggerImpl implements ILogger {

	@Override
	public void log(String msg) {
		System.out.println(msg);		
	}

}
