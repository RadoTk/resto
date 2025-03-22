package edu.restaurant.app.dao.entity;

import java.time.LocalDate;

public class Price {
    private Long id;
    private Ingredient ingredient;
    private Double amount;
    private LocalDate dateValue;

    // Constructeur par défaut
    public Price() {
    }

    // Constructeur avec tous les paramètres
    public Price(Long id, Ingredient ingredient, Double amount, LocalDate dateValue) {
        this.id = id;
        this.ingredient = ingredient;
        this.amount = amount;
        this.dateValue = dateValue;
    }

    // Constructeur avec amount seulement
    public Price(Double amount) {
        this.amount = amount;
        this.dateValue = LocalDate.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getDateValue() {
        return dateValue;
    }

    public void setDateValue(LocalDate dateValue) {
        this.dateValue = dateValue;
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", ingredient=" + ingredient +
                ", amount=" + amount +
                ", dateValue=" + dateValue +
                '}';
    }
}
