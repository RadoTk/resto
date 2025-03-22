package edu.restaurant.app.dao.entity;

import java.time.Instant;

public class OrderStatusEntry {
    private Long id;
    private Long orderId;
    private OrderStatus status;
    private Instant statusDatetime;

    public OrderStatusEntry() {
    }

    public OrderStatusEntry(Long id, Long orderId, OrderStatus status, Instant statusDatetime) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.statusDatetime = statusDatetime;
    }

    public OrderStatusEntry(Long orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
        this.statusDatetime = Instant.now();
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getStatusDatetime() {
        return statusDatetime;
    }

    public void setStatusDatetime(Instant statusDatetime) {
        this.statusDatetime = statusDatetime;
    }

    @Override
    public String toString() {
        return "OrderStatusEntry{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", status=" + status +
                ", statusDatetime=" + statusDatetime +
                '}';
    }
} 