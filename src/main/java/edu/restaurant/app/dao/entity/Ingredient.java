package edu.restaurant.app.dao.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static edu.restaurant.app.dao.entity.StockMovementType.IN;
import static edu.restaurant.app.dao.entity.StockMovementType.OUT;

public class Ingredient {
    private Long id;
    private String name;
    private List<Price> prices;
    private List<StockMovement> stockMovements;

    // Constructeur par défaut
    public Ingredient() {
    }

    // Constructeur avec tous les paramètres
    public Ingredient(Long id, String name, List<Price> prices, List<StockMovement> stockMovements) {
        this.id = id;
        this.name = name;
        this.prices = prices;
        this.stockMovements = stockMovements;
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

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }

    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }

    // Méthodes de gestion des mouvements de stock
    public List<StockMovement> addStockMovements(List<StockMovement> stockMovements) {
        stockMovements.forEach(stockMovement -> stockMovement.setIngredient(this));
        if (getStockMovements() == null || getStockMovements().isEmpty()) {
            return stockMovements;
        }
        getStockMovements().addAll(stockMovements);
        return getStockMovements();
    }

    public List<Price> addPrices(List<Price> prices) {
        if (getPrices() == null || getPrices().isEmpty()) {
            return prices;
        }
        prices.forEach(price -> price.setIngredient(this));
        getPrices().addAll(prices);
        return getPrices();
    }

    // Méthodes de calcul des prix et quantités
    public Double getActualPrice() {
        return findActualPrice().orElse(new Price(0.0)).getAmount();
    }

    public Double getAvailableQuantity() {
        return getAvailableQuantityAt(Instant.now());
    }

    public Double getPriceAt(LocalDate dateValue) {
        return findPriceAt(dateValue).orElse(new Price(0.0)).getAmount();
    }

    public Double getAvailableQuantityAt(Instant datetime) {
        List<StockMovement> stockMovementsBeforeToday = stockMovements.stream()
                .filter(stockMovement ->
                        stockMovement.getCreationDatetime().isBefore(datetime)
                                || stockMovement.getCreationDatetime().equals(datetime))
                .toList();
        double quantity = 0;
        for (StockMovement stockMovement : stockMovementsBeforeToday) {
            if (IN.equals(stockMovement.getMovementType())) {
                quantity += stockMovement.getQuantity();
            } else if (OUT.equals(stockMovement.getMovementType())) {
                quantity -= stockMovement.getQuantity();
            }
        }
        return quantity;
    }

    private Optional<Price> findPriceAt(LocalDate dateValue) {
        return prices.stream()
                .filter(price -> price.getDateValue().equals(dateValue))
                .findFirst();
    }

    private Optional<Price> findActualPrice() {
        return prices.stream().max(Comparator.comparing(Price::getDateValue));
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prices=" + prices +
                ", stockMovements=" + stockMovements +
                '}';
    }
}
