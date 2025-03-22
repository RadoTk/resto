package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.*;
import edu.restaurant.app.dao.entity.Order;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderCrudOperationsTest {
    private static DataSource dataSource;
    private static OrderCrudOperations orderCrudOperations;
    private static DishOrderCrudOperations dishOrderCrudOperations;
    private static DishCrudOperations dishCrudOperations;
    private static Long testOrderId;

    @BeforeAll
    public static void setUp() {
        dataSource = new DataSource();
        dishCrudOperations = new DishCrudOperations();
        dishOrderCrudOperations = new DishOrderCrudOperations(dataSource, dishCrudOperations);
        orderCrudOperations = new OrderCrudOperations(dataSource, dishOrderCrudOperations);
    }
    
    @AfterAll
    public static void tearDown() {
    }
    
    @Test
    @org.junit.jupiter.api.Order(1)
    public void testCreateOrder() {
        
        Order order = new Order("TEST-ORDER-" + System.currentTimeMillis());
        
        Dish dish = dishCrudOperations.findById(1L);
        assertNotNull(dish, "Test dish should exist");
        
        DishOrder dishOrder = new DishOrder(null, dish, 2);
        order.addDishOrder(dishOrder);
        
        Order savedOrder = orderCrudOperations.create(order);
        
        testOrderId = savedOrder.getId();
        
        assertNotNull(savedOrder.getId(), "Order ID should not be null");
        assertEquals(order.getReference(), savedOrder.getReference(), "Order reference should match");
        assertNotNull(savedOrder.getStatusHistory(), "Status history should not be null");
        assertFalse(savedOrder.getStatusHistory().isEmpty(), "Status history should not be empty");
        assertEquals(OrderStatus.CREATED, savedOrder.getActualStatus(), "Initial status should be CREATED");
        assertEquals(1, savedOrder.getDishOrders().size(), "Order should have one dish order");
    }
    
    @Test
    @org.junit.jupiter.api.Order(2)
    public void testFindOrderById() {
        Optional<Order> foundOrder = orderCrudOperations.findById(testOrderId);
        
        assertTrue(foundOrder.isPresent(), "Order should be found by ID");
        assertEquals(testOrderId, foundOrder.get().getId(), "Order ID should match");
        assertEquals(OrderStatus.CREATED, foundOrder.get().getActualStatus(), "Status should be CREATED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(3)
    public void testFindOrderByReference() {
        Optional<Order> order = orderCrudOperations.findById(testOrderId);
        assertTrue(order.isPresent(), "Order should be found by ID");
        String reference = order.get().getReference();
        
        Optional<Order> foundOrder = orderCrudOperations.findByReference(reference);
        
        assertTrue(foundOrder.isPresent(), "Order should be found by reference");
        assertEquals(reference, foundOrder.get().getReference(), "Order reference should match");
    }
    
    @Test
    @org.junit.jupiter.api.Order(4)
    public void testUpdateOrderStatus() {
        orderCrudOperations.updateOrderStatus(testOrderId, OrderStatus.CONFIRMED);
        
        Optional<Order> updatedOrder = orderCrudOperations.findById(testOrderId);
        assertTrue(updatedOrder.isPresent(), "Order should be found after status update");
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.get().getActualStatus(), "Order status should be CONFIRMED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(5)
    public void testFindAllOrders() {
        // Get all orders
        List<Order> orders = orderCrudOperations.findAll();
        
        assertFalse(orders.isEmpty(), "There should be at least one order");
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(testOrderId)), 
                  "Test order should be in the list of all orders");
    }
    
    @Test
    @org.junit.jupiter.api.Order(6)
    public void testSaveOrder() {
        Optional<Order> existingOrder = orderCrudOperations.findById(testOrderId);
        assertTrue(existingOrder.isPresent(), "Order should exist before save test");
        
        Order orderToUpdate = existingOrder.get();
        String newReference = "UPDATED-" + System.currentTimeMillis();
        orderToUpdate.setReference(newReference);
        
        Order savedOrder = orderCrudOperations.save(orderToUpdate);
        
        assertEquals(newReference, savedOrder.getReference(), "Reference should be updated");
        
        // Verify the update in database
        Optional<Order> refreshedOrder = orderCrudOperations.findById(testOrderId);
        assertTrue(refreshedOrder.isPresent(), "Order should be found after save");
        assertEquals(newReference, refreshedOrder.get().getReference(), "Reference should be updated in database");
    }
    
    @Test
    @org.junit.jupiter.api.Order(9) 
    public void testDeleteOrder() {
        orderCrudOperations.delete(testOrderId);
        
        Optional<Order> deletedOrder = orderCrudOperations.findById(testOrderId);
        assertFalse(deletedOrder.isPresent(), "Order should be deleted");
    }
} 