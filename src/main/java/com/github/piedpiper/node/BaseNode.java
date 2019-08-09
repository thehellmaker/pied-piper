package com.github.piedpiper.node;

import com.github.commons.log.ILogger;
import com.github.piedpiper.graph.api.ApiNodeActor;
import com.google.inject.Inject;
import com.google.inject.Injector;

public abstract class BaseNode implements INode {

	protected ILogger logger;

	protected Injector injector;
	
	/**
	 * TODO: This is introduced as I don't know how to assisted constructor inject
	 * for different classes with different {@link Inject} annotations but same
	 * {@link Assisted} annotation fields.
	 * 
	 * For Example: If there are 2 classes with constructor signature A(B, C,
	 * Assisted1) and B(D, Assisted1), I do not know how to use guice to constructor
	 * inject this.
	 * 
	 * The approach I am using here is to set logger to each {@link INode}
	 * implemetation during instantiation in {@link ApiNodeActor} class.
	 * 
	 * @param logger
	 */
	public void setILogger(ILogger logger) {
		this.logger = logger;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

}
