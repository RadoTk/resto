package edu.restaurant.app.dao.entity;

public class InsufficientIngredientException extends Exception {
    public InsufficientIngredientException(String message) {
        super(message);
    }
} 