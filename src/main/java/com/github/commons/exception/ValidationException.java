package com.github.commons.exception;

/**
 * Created by aashok on 11/28/2017.
 */
public class ValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 8062755351357444301L;

	public ValidationException() {
		
	}
	
    public ValidationException(String s) {
        super(s);
    }

	public ValidationException(String s, Exception e) {
		super(s, e);
	}
}
