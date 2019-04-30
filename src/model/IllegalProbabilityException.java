package model;

import java.lang.Error;

/**
 * This error is thrown when a probability for the CAF is illegal (not between 0 and 1) 
 */
public class IllegalProbabilityException extends Error{
	
	public IllegalProbabilityException(String message) {
		super(message);
	}
	
}
