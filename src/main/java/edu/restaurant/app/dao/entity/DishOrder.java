package edu.restaurant.app.dao.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DishOrder {
    private Long id;
    private Long orderId;
    private Dish dish;
    private Integer quantity;
    private List<OrderDishStatusEntry> statusHistory;

    public DishOrder() {
        this.statusHistory = new ArrayList<>();
    }

    public DishOrder(Long id, Long orderId, Dish dish, Integer quantity, List<OrderDishStatusEntry> statusHistory) {
        this.id = id;
        this.orderId = orderId;
        this.dish = dish;
        this.quantity = quantity;
        this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>();
    }

    public DishOrder(Long orderId, Dish dish, Integer quantity) {
        this.orderId = orderId;
        this.dish = dish;
        this.quantity = quantity;
        this.statusHistory = new ArrayList<>();
        
        OrderDishStatusEntry initialStatus = new OrderDishStatusEntry(null, OrderDishStatus.CREATED);
        initialStatus.setStatusDatetime(Instant.now());
        this.statusHistory.add(initialStatus);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<OrderDishStatusEntry> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<OrderDishStatusEntry> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public OrderDishStatus getActualStatus() {
        return statusHistory.stream()
                .max(Comparator.comparing(OrderDishStatusEntry::getStatusDatetime))
                .map(OrderDishStatusEntry::getStatus)
                .orElse(OrderDishStatus.CREATED);
    }

    public void addStatus(OrderDishStatus status) {
        OrderDishStatus currentStatus = getActualStatus();
        
        boolean validTransition = isValidStatusTransition(currentStatus, status);
        if (!validTransition) {
            throw new IllegalStateException("Cannot transition from " + currentStatus + " to " + status);
        }
        
        OrderDishStatusEntry statusEntry = new OrderDishStatusEntry(this.id, status);
        this.statusHistory.add(statusEntry);
    }

    private boolean isValidStatusTransition(OrderDishStatus currentStatus, OrderDishStatus newStatus) {
        if (currentStatus == null) {
            return newStatus == OrderDishStatus.CREATED;
        }
        
        switch (currentStatus) {
            case CREATED:
                return newStatus == OrderDishStatus.CONFIRMED;
            case CONFIRMED:
                return newStatus == OrderDishStatus.IN_PREPARATION;
            case IN_PREPARATION:
                return newStatus == OrderDishStatus.FINISHED;
            case FINISHED:
                return newStatus == OrderDishStatus.SERVED;
            default:
                return false;
        }
    }

    public Double getTotalPrice() {
        return dish.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return "DishOrder{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", dish=" + dish +
                ", quantity=" + quantity +
                ", status=" + getActualStatus() +
                '}';
    }
} 