package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import org.junit.jupiter.api.*;

import edu.restaurant.app.dao.entity.Dish;
import edu.restaurant.app.dao.entity.DishOrder;
import edu.restaurant.app.dao.entity.Order;
import edu.restaurant.app.dao.entity.OrderDishStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DishOrderCrudOperationsTest {
    private static DataSource dataSource;
    private static DishOrderCrudOperations dishOrderCrudOperations;
    private static DishCrudOperations dishCrudOperations;
    private static OrderCrudOperations orderCrudOperations;
    private static Long testDishOrderId;
    private static Long testOrderId;

    @BeforeAll
    public static void setUp() {
        // Initialize data source and crud operations
        dataSource = new DataSource();
        dishCrudOperations = new DishCrudOperations();
        dishOrderCrudOperations = new DishOrderCrudOperations(dataSource, dishCrudOperations);
        orderCrudOperations = new OrderCrudOperations(dataSource, dishOrderCrudOperations);
        
        // Create a test order for dish order tests
        createTestOrder();
    }
    
    private static void createTestOrder() {
        // Create a test order to use for dish order tests
        Order order = new Order("TEST-ORDER-FOR-DISH-" + System.currentTimeMillis());
        Order savedOrder = orderCrudOperations.create(order);
        testOrderId = savedOrder.getId();
    }
    
    @AfterAll
    public static void tearDown() {
        // Clean up test data if necessary
        if (testOrderId != null) {
            orderCrudOperations.delete(testOrderId);
        }
    }
    
    @Test
    @org.junit.jupiter.api.Order(1)
    public void testCreateDishOrder() {
        // Get a dish to add to the order
        Dish dish = dishCrudOperations.findById(1L);
        assertNotNull(dish, "Test dish should exist");
        
        // Create a new dish order
        DishOrder dishOrder = new DishOrder(testOrderId, dish, 3);
        
        // Save the dish order
        DishOrder savedDishOrder = dishOrderCrudOperations.create(dishOrder);
        
        // Store the ID for later tests
        testDishOrderId = savedDishOrder.getId();
        
        // Verify the dish order was saved properly
        assertNotNull(savedDishOrder.getId(), "Dish order ID should not be null");
        assertEquals(testOrderId, savedDishOrder.getOrderId(), "Order ID should match");
        assertEquals(dish.getId(), savedDishOrder.getDish().getId(), "Dish ID should match");
        assertEquals(3, savedDishOrder.getQuantity(), "Quantity should match");
        assertNotNull(savedDishOrder.getStatusHistory(), "Status history should not be null");
        assertFalse(savedDishOrder.getStatusHistory().isEmpty(), "Status history should not be empty");
        assertEquals(OrderDishStatus.CREATED, savedDishOrder.getActualStatus(), "Initial status should be CREATED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(2)
    public void testFindDishOrderById() {
        // Find the dish order created in the previous test
        Optional<DishOrder> foundDishOrder = dishOrderCrudOperations.findById(testDishOrderId);
        
        // Verify the dish order was found
        assertTrue(foundDishOrder.isPresent(), "Dish order should be found by ID");
        assertEquals(testDishOrderId, foundDishOrder.get().getId(), "Dish order ID should match");
        assertEquals(OrderDishStatus.CREATED, foundDishOrder.get().getActualStatus(), "Status should be CREATED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(3)
    public void testFindDishOrdersByOrderId() {
        // Find all dish orders for the test order
        List<DishOrder> dishOrders = dishOrderCrudOperations.findByOrderId(testOrderId);
        
        // Verify that the dish orders were found
        assertFalse(dishOrders.isEmpty(), "There should be at least one dish order for the test order");
        assertTrue(dishOrders.stream().anyMatch(dishOrder -> dishOrder.getId().equals(testDishOrderId)), 
                  "Test dish order should be in the list of dish orders for the test order");
    }
    
    @Test
    @org.junit.jupiter.api.Order(4)
    public void testUpdateDishOrderStatus() {
        // Update the status of the dish order
        dishOrderCrudOperations.updateDishOrderStatus(testDishOrderId, OrderDishStatus.CONFIRMED);
        
        // Verify the status was updated
        Optional<DishOrder> updatedDishOrder = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(updatedDishOrder.isPresent(), "Dish order should be found after status update");
        assertEquals(OrderDishStatus.CONFIRMED, updatedDishOrder.get().getActualStatus(), 
                    "Dish order status should be CONFIRMED");
    }
    
    @Test
    @org.junit.jupiter.api.Order(5)
    public void testSaveDishOrder() {
        // Find existing dish order
        Optional<DishOrder> existingDishOrder = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(existingDishOrder.isPresent(), "Dish order should exist before save test");
        
        // Update the quantity
        DishOrder dishOrderToUpdate = existingDishOrder.get();
        int newQuantity = 5;
        dishOrderToUpdate.setQuantity(newQuantity);
        
        // Save the updated dish order
        DishOrder savedDishOrder = dishOrderCrudOperations.save(dishOrderToUpdate);
        
        // Verify the update
        assertEquals(newQuantity, savedDishOrder.getQuantity(), "Quantity should be updated");
        
        // Verify the update in database
        Optional<DishOrder> refreshedDishOrder = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(refreshedDishOrder.isPresent(), "Dish order should be found after save");
        assertEquals(newQuantity, refreshedDishOrder.get().getQuantity(), "Quantity should be updated in database");
    }
    
    @Test
    @org.junit.jupiter.api.Order(6)
    public void testTransitionThroughStatuses() {
        // Transition through the status lifecycle
        dishOrderCrudOperations.updateDishOrderStatus(testDishOrderId, OrderDishStatus.IN_PREPARATION);
        
        Optional<DishOrder> dishOrder1 = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(dishOrder1.isPresent(), "Dish order should be found");
        assertEquals(OrderDishStatus.IN_PREPARATION, dishOrder1.get().getActualStatus(), 
                    "Status should be IN_PREPARATION");
        
        dishOrderCrudOperations.updateDishOrderStatus(testDishOrderId, OrderDishStatus.FINISHED);
        
        Optional<DishOrder> dishOrder2 = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(dishOrder2.isPresent(), "Dish order should be found");
        assertEquals(OrderDishStatus.FINISHED, dishOrder2.get().getActualStatus(), 
                    "Status should be FINISHED");
        
        dishOrderCrudOperations.updateDishOrderStatus(testDishOrderId, OrderDishStatus.SERVED);
        
        Optional<DishOrder> dishOrder3 = dishOrderCrudOperations.findById(testDishOrderId);
        assertTrue(dishOrder3.isPresent(), "Dish order should be found");
        assertEquals(OrderDishStatus.SERVED, dishOrder3.get().getActualStatus(), 
                    "Status should be SERVED");
    }
} 