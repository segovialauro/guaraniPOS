package com.guarani.pos.shared.exception;

public class ForbiddenOperationException extends RuntimeException {
   
	private static final long serialVersionUID = -2501548096423188610L;

	public ForbiddenOperationException(String message) {
        super(message);
    }
}
