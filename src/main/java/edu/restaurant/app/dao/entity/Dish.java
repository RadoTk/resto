package edu.restaurant.app.dao.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Long id;
    private String name;
    private List<DishIngredient> dishIngredients;
    private Double price;

    // Constructeur par défaut
    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    // Constructeur avec tous les paramètres
    public Dish(Long id, String name, List<DishIngredient> dishIngredients, Double price) {
        this.id = id;
        this.name = name;
        this.dishIngredients = dishIngredients;
        this.price = price;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Méthodes de calcul
    public Double getGrossMargin() {
        return getPrice() - getTotalIngredientsCost();
    }

    public Double getGrossMarginAt(LocalDate dateValue) {
        return getPrice() - getTotalIngredientsCostAt(dateValue);
    }

    public Double getTotalIngredientsCost() {
        return dishIngredients.stream()
                .map(dishIngredient -> {
                    Double actualPrice = dishIngredient.getIngredient().getActualPrice();
                    Double requiredQuantity = dishIngredient.getRequiredQuantity();
                    return actualPrice * requiredQuantity;
                })
                .reduce(0.0, Double::sum);
    }

    public Double getTotalIngredientsCostAt(LocalDate dateValue) {
        double cost = 0.0;
        for (DishIngredient dishIngredient : dishIngredients) {
            cost += dishIngredient.getIngredient().getPriceAt(dateValue);
        }
        return cost;
    }

    public Double getAvailableQuantity() {
        List<Double> allQuantitiesPossible = new ArrayList<>();
        for (DishIngredient dishIngredient : dishIngredients) {
            Ingredient ingredient = dishIngredient.getIngredient();
            double quantityPossibleForThatIngredient = ingredient.getAvailableQuantity() / dishIngredient.getRequiredQuantity();
            double roundedQuantityPossible = Math.ceil(quantityPossibleForThatIngredient); // ceil for smallest
            allQuantitiesPossible.add(roundedQuantityPossible);
        }
        return allQuantitiesPossible.stream().min(Double::compare).orElse(0.0);
    }

    public Double getAvailableQuantityAt(Instant datetime) {
        List<Double> allQuantitiesPossible = new ArrayList<>();
        for (DishIngredient dishIngredient : dishIngredients) {
            Ingredient ingredient = dishIngredient.getIngredient();
            double quantityPossibleForThatIngredient = ingredient.getAvailableQuantityAt(datetime) / dishIngredient.getRequiredQuantity();
            double roundedQuantityPossible = Math.ceil(quantityPossibleForThatIngredient); // ceil for smallest
            allQuantitiesPossible.add(roundedQuantityPossible);
        }
        return allQuantitiesPossible.stream().min(Double::compare).orElse(0.0);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishIngredients=" + dishIngredients +
                ", price=" + price +
                '}';
    }
}
