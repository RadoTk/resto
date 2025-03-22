package edu.restaurant.app.dao.entity;

public class InsufficientIngredientsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InsufficientIngredientsException(String message) {
        super(message);
    }
    
    public InsufficientIngredientsException(String message, Throwable cause) {
        super(message, cause);
    }
} 