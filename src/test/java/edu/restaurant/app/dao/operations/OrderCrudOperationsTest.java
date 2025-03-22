package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.*;
import edu.restaurant.app.dao.entity.Order;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderCrudOperationsTest {
    private static DataSource dataSource;
    private static OrderCrudOperations orderCrudOperations;
    private static DishOrderCrudOperations dishOrderCrudOperations;
    private static DishCrudOperations dishCrudOperations;
    private static Long testOrderId;
    private static List<Long> testBatchOrderIds = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        dataSource = new DataSource();
        dishCrudOperations = new DishCrudOperations();
        dishOrderCrudOperations = new DishOrderCrudOperations(dataSource, dishCrudOperations);
        orderCrudOperations = new OrderCrudOperations(dataSource, dishOrderCrudOperations);
    }
    
    @AfterAll
    public static void tearDown() {
        // Nettoyer les commandes créées par le test saveAll
        for (Long id : testBatchOrderIds) {
            try {
                orderCrudOperations.delete(id);
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression de la commande " + id + ": " + e.getMessage());
            }
        }
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
        Optional<Order> foundOrder = orderCrudOperations.findOptionalById(testOrderId);
        
        assertTrue(foundOrder.isPresent(), "Order should be found by ID");
        assertEquals(testOrderId, foundOrder.get().getId(), "Order ID should match");
        assertEquals(OrderStatus.CREATED, foundOrder.get().getActualStatus(), "Status should be CREATED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(3)
    public void testFindOrderByReference() {
        Optional<Order> order = orderCrudOperations.findOptionalById(testOrderId);
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
        
        Optional<Order> updatedOrder = orderCrudOperations.findOptionalById(testOrderId);
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
        Optional<Order> existingOrder = orderCrudOperations.findOptionalById(testOrderId);
        assertTrue(existingOrder.isPresent(), "Order should exist before save test");
        
        Order orderToUpdate = existingOrder.get();
        String newReference = "UPDATED-" + System.currentTimeMillis();
        orderToUpdate.setReference(newReference);
        
        Order savedOrder = orderCrudOperations.save(orderToUpdate);
        
        assertEquals(newReference, savedOrder.getReference(), "Reference should be updated");
        
        // Verify the update in database
        Optional<Order> refreshedOrder = orderCrudOperations.findOptionalById(testOrderId);
        assertTrue(refreshedOrder.isPresent(), "Order should be found after save");
        assertEquals(newReference, refreshedOrder.get().getReference(), "Reference should be updated in database");
    }
    
    @Test
    @org.junit.jupiter.api.Order(7)
    public void testSaveAllOrders() {
        // Créer plusieurs commandes avec leurs plats
        List<Order> orders = new ArrayList<>();
        
        // Première commande avec deux plats
        Order order1 = new Order("BATCH-ORDER-1-" + System.currentTimeMillis());
        Dish dish1 = dishCrudOperations.findById(1L);
        Dish dish2 = dishCrudOperations.findById(1L);
        order1.addDishOrder(new DishOrder(null, dish1, 1));
        order1.addDishOrder(new DishOrder(null, dish2, 2));
        orders.add(order1);
        
        // Deuxième commande avec un plat
        Order order2 = new Order("BATCH-ORDER-2-" + System.currentTimeMillis());
        order2.addDishOrder(new DishOrder(null, dish1, 3));
        orders.add(order2);
        
        // Sauvegarder toutes les commandes
        List<Order> savedOrders = orderCrudOperations.saveAll(orders);
        
        // Vérifier que toutes les commandes ont été sauvegardées
        assertEquals(2, savedOrders.size(), "All orders should be saved");
        
        // Vérifier que les ID ont été générés
        assertNotNull(savedOrders.get(0).getId(), "Order 1 ID should not be null");
        assertNotNull(savedOrders.get(1).getId(), "Order 2 ID should not be null");
        
        // Stocker les IDs pour le nettoyage
        testBatchOrderIds.add(savedOrders.get(0).getId());
        testBatchOrderIds.add(savedOrders.get(1).getId());
        
        // Vérifier que les plats ont été sauvegardés
        Order savedOrder1 = orderCrudOperations.findById(savedOrders.get(0).getId());
        Order savedOrder2 = orderCrudOperations.findById(savedOrders.get(1).getId());
        
        assertEquals(2, savedOrder1.getDishOrders().size(), "Order 1 should have 2 dish orders");
        assertEquals(1, savedOrder2.getDishOrders().size(), "Order 2 should have 1 dish order");
        
        // Vérifier que les statuts ont été initialisés correctement
        assertEquals(OrderStatus.CREATED, savedOrder1.getActualStatus(), "Order 1 status should be CREATED");
        assertEquals(OrderStatus.CREATED, savedOrder2.getActualStatus(), "Order 2 status should be CREATED");
        
        // Vérifier que les plats ont le bon statut
        assertTrue(savedOrder1.getDishOrders().stream()
                .allMatch(d -> OrderDishStatus.CREATED.equals(d.getActualStatus())),
                "All dish orders in order 1 should have CREATED status");
        assertTrue(savedOrder2.getDishOrders().stream()
                .allMatch(d -> OrderDishStatus.CREATED.equals(d.getActualStatus())),
                "All dish orders in order 2 should have CREATED status");
    }
    
    @Test
    @org.junit.jupiter.api.Order(9) 
    public void testDeleteOrder() {
        orderCrudOperations.delete(testOrderId);
        
        Optional<Order> deletedOrder = orderCrudOperations.findOptionalById(testOrderId);
        assertFalse(deletedOrder.isPresent(), "Order should be deleted");
    }
} 