package edu.restaurant.app.dao.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

public class Order {
    private Long id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;
    private List<OrderStatusEntry> statusHistory;

    public Order() {
        this.dishOrders = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
        this.creationDatetime = Instant.now();
    }

    public Order(Long id, String reference, Instant creationDatetime, List<DishOrder> dishOrders, List<OrderStatusEntry> statusHistory) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime != null ? creationDatetime : Instant.now();
        this.dishOrders = dishOrders != null ? dishOrders : new ArrayList<>();
        this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>();
    }

    public Order(String reference) {
        this.reference = reference;
        this.creationDatetime = Instant.now();
        this.dishOrders = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
        
        // Initialisation directe du statut CREATED sans vérification de transition
        OrderStatusEntry initialStatus = new OrderStatusEntry(null, OrderStatus.CREATED);
        initialStatus.setStatusDatetime(this.creationDatetime);
        this.statusHistory.add(initialStatus);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public List<OrderStatusEntry> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<OrderStatusEntry> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public OrderStatus getActualStatus() {
        System.out.println("Getting actual status, history size: " + (statusHistory != null ? statusHistory.size() : 0));
        if (statusHistory == null || statusHistory.isEmpty()) {
            return OrderStatus.CREATED;
        }

        for (OrderStatusEntry entry : statusHistory) {
            System.out.println("Status entry: " + entry.getStatus() + " at " + entry.getStatusDatetime());
        }
        
        OrderStatusEntry latestEntry = statusHistory.stream()
                .max(Comparator.comparing(OrderStatusEntry::getStatusDatetime))
                .orElse(null);
        
        if (latestEntry == null) {
            return OrderStatus.CREATED;
        }
        
        System.out.println("Latest status found: " + latestEntry.getStatus() + " at " + latestEntry.getStatusDatetime());
        return latestEntry.getStatus();
    }

    public void addStatus(OrderStatus status) {
        System.out.println("Adding status: " + status + " to order " + getId());
        OrderStatus actualStatus = getActualStatus();
        System.out.println("Current status: " + actualStatus);
        
        if (!isValidStatusTransition(actualStatus, status)) {
            System.err.println("Invalid status transition from " + actualStatus + " to " + status);
            throw new IllegalStateException("Invalid status transition from " + actualStatus + " to " + status);
        }

        // Create a new OrderStatusEntry with the appropriate constructor
        OrderStatusEntry statusEntry = new OrderStatusEntry(getId(), status);
        
        // Add the status entry
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        statusHistory.add(statusEntry);
        
        System.out.println("Status successfully updated from " + actualStatus + " to " + status);
        System.out.println("Current status entries: " + statusHistory.size());
        
        // Update dish statuses accordingly (but not for FINISHED or SERVED status)
        if (status != OrderStatus.FINISHED && status != OrderStatus.SERVED) {
            updateDishStatuses(status);
        } else {
            System.out.println("Skipping dish status update for " + status + " status");
        }
    }
    
    private void updateDishStatuses(OrderStatus orderStatus) {
        // Only for specific status transitions 
        if (orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.IN_PREPARATION) {
            OrderDishStatus dishStatus = OrderDishStatus.valueOf(orderStatus.name());
            for (DishOrder dishOrder : dishOrders) {
                try {
                    dishOrder.addStatus(dishStatus);
                } catch (IllegalStateException e) {
                    // Log but don't fail the main status transition if dish status update fails
                    System.err.println("Failed to update dish status: " + e.getMessage());
                }
            }
        }
        
        // Debug output
        System.out.println("After updateDishStatuses, order status is: " + getActualStatus());
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null) {
            return newStatus == OrderStatus.CREATED;
        }
        
        switch (currentStatus) {
            case CREATED:
                return newStatus == OrderStatus.CONFIRMED;
            case CONFIRMED:
                return newStatus == OrderStatus.IN_PREPARATION;
            case IN_PREPARATION:
                return newStatus == OrderStatus.FINISHED;
            case FINISHED:
                return newStatus == OrderStatus.SERVED;
            default:
                return false;
        }
    }
    
    public void addDishOrder(DishOrder dishOrder) {
        OrderStatus currentStatus = getActualStatus();
        if (currentStatus != OrderStatus.CREATED) {
            throw new IllegalStateException("Impossible de modifier les plats après confirmation de la commande (statut actuel: " + currentStatus + ")");
        }
        
        if (dishOrder != null) {
            if (dishOrders == null) {
                dishOrders = new ArrayList<>();
            }
            // Associer le plat à cette commande
            dishOrder.setOrderId(getId());
            dishOrders.add(dishOrder);
        }
    }

    public Double getTotalAmount() {
        return dishOrders.stream()
                .mapToDouble(DishOrder::getTotalPrice)
                .sum();
    }
    
    
    public void confirm() {
        // Vérifier la disponibilité des ingrédients
        Map<Ingredient, Double> requiredIngredients = new HashMap<>();
        
        // Calculer la quantité totale requise pour chaque ingrédient
        for (DishOrder dishOrder : dishOrders) {
            Dish dish = dishOrder.getDish();
            int quantity = dishOrder.getQuantity();
            
            for (DishIngredient dishIngredient : dish.getDishIngredients()) {
                Ingredient ingredient = dishIngredient.getIngredient();
                double requiredQuantity = dishIngredient.getRequiredQuantity() * quantity;
                
                requiredIngredients.merge(ingredient, requiredQuantity, Double::sum);
            }
        }
        
        // Vérifier la disponibilité pour chaque ingrédient
        StringBuilder missingIngredientsMessage = new StringBuilder();
        for (Map.Entry<Ingredient, Double> entry : requiredIngredients.entrySet()) {
            Ingredient ingredient = entry.getKey();
            double requiredQuantity = entry.getValue();
            double availableQuantity = getAvailableStock(ingredient);
            
            if (availableQuantity < requiredQuantity) {
                double missing = requiredQuantity - availableQuantity;
                
                for (DishOrder dishOrder : dishOrders) {
                    Dish dish = dishOrder.getDish();
                    for (DishIngredient dishIngredient : dish.getDishIngredients()) {
                        if (dishIngredient.getIngredient().equals(ingredient)) {
                            if (missingIngredientsMessage.length() > 0) {
                                missingIngredientsMessage.append(", ");
                            }
                            missingIngredientsMessage.append(
                                String.format("%.0f %s est nécessaire pour fabriquer %.0f %s supplémentaire", 
                                missing, 
                                ingredient.getName(), 
                                Math.ceil(missing / dishIngredient.getRequiredQuantity()), 
                                dish.getName()));
                            break;
                        }
                    }
                    if (missingIngredientsMessage.length() > 0) {
                        break;
                    }
                }
            }
        }
        
        if (missingIngredientsMessage.length() > 0) {
            throw new InsufficientIngredientsException("Ingrédients insuffisants: " + missingIngredientsMessage);
        }
        addStatus(OrderStatus.CONFIRMED);
    }
    
    /**
     * Calcule la quantité disponible d'un ingrédient.
     */
    private double getAvailableStock(Ingredient ingredient) {
        if (ingredient == null || ingredient.getStockMovements() == null) {
            return 0.0;
        }
        
        double totalIn = ingredient.getStockMovements().stream()
            .filter(m -> m.getMovementType() == StockMovementType.IN)
            .mapToDouble(StockMovement::getQuantity)
            .sum();
        
        double totalOut = ingredient.getStockMovements().stream()
            .filter(m -> m.getMovementType() == StockMovementType.OUT)
            .mapToDouble(StockMovement::getQuantity)
            .sum();
        
        return totalIn - totalOut;
    }
    
    public void updateStatusBasedOnDishes() {
        // Get current statuses of all dishes
        List<OrderDishStatus> dishStatuses = dishOrders.stream()
                .map(DishOrder::getActualStatus)
                .collect(Collectors.toList());
        
        System.out.println("Updating order status based on dishes. Current dish statuses: " + dishStatuses);
        OrderStatus currentStatus = getActualStatus();
        System.out.println("Current order status: " + currentStatus);
        
        // Check if all dishes are in a particular status
        boolean allFinished = dishStatuses.stream().allMatch(s -> s == OrderDishStatus.FINISHED);
        boolean allServed = dishStatuses.stream().allMatch(s -> s == OrderDishStatus.SERVED);
        boolean anyInPreparation = dishStatuses.stream().anyMatch(s -> s == OrderDishStatus.IN_PREPARATION);
        
        System.out.println("All finished: " + allFinished + ", All served: " + allServed + ", Any in preparation: " + anyInPreparation);
        
        // All dishes are SERVED -> Order is SERVED
        if (allServed && currentStatus == OrderStatus.FINISHED) {
            System.out.println("All dishes are SERVED, transitioning order to SERVED");
            addStatus(OrderStatus.SERVED);
        }
        // All dishes are FINISHED -> Order is FINISHED
        else if (allFinished && currentStatus == OrderStatus.IN_PREPARATION) {
            System.out.println("All dishes are FINISHED, transitioning order to FINISHED");
            addStatus(OrderStatus.FINISHED);
        }
        // Some dishes are still in preparation -> Order is IN_PREPARATION
        // But only if we're not in FINISHED or SERVED status already
        else if (anyInPreparation && 
                 currentStatus != OrderStatus.FINISHED && 
                 currentStatus != OrderStatus.SERVED) {
            System.out.println("Some dishes are IN_PREPARATION, setting order as IN_PREPARATION");
            if (currentStatus != OrderStatus.IN_PREPARATION) {
                addStatus(OrderStatus.IN_PREPARATION);
            }
        } else {
            System.out.println("No status change needed based on dish statuses");
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", dishOrders=" + dishOrders +
                ", status=" + getActualStatus() +
                '}';
    }
} 