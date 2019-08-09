package com.github.piedpiper.graph.api;

public class DependentNodeException extends RuntimeException {


	public DependentNodeException(String msg) {
		super(msg);
	}
	
	public DependentNodeException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7124524702148454364L;

}
