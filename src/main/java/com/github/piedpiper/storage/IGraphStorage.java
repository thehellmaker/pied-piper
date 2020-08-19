package com.github.piedpiper.storage;

import java.util.concurrent.ExecutionException;

import com.github.commons.log.ILogger;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Injector;

public interface IGraphStorage {
	
	public void setInjector(Injector injector);
	public NodeOutput getGraphs() throws InterruptedException, ExecutionException;
	public void setLogger(ILogger logger);
}
