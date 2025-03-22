package edu.restaurant.app.dao.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderEntityTest {

    @Test
    public void testGetActualStatus() {
        Order order = new Order("ORD-TEST-001");
        order.setId(1L);
        order.setStatusHistory(new ArrayList<>());
        
        assertEquals(OrderStatus.CREATED, order.getActualStatus(), 
                    "Une commande sans historique de statut devrait retourner CREATED");
        
        OrderStatusEntry statusEntry = new OrderStatusEntry(order.getId(), OrderStatus.CONFIRMED);
        statusEntry.setStatusDatetime(Instant.now());
        order.getStatusHistory().add(statusEntry);
        
        assertEquals(OrderStatus.CONFIRMED, order.getActualStatus(), 
                    "Devrait retourner le seul statut présent");
        
        OrderStatusEntry olderStatus = new OrderStatusEntry(order.getId(), OrderStatus.IN_PREPARATION);
        olderStatus.setStatusDatetime(Instant.now().minusSeconds(60));
        order.getStatusHistory().add(olderStatus);
        
        OrderStatusEntry recentStatus = new OrderStatusEntry(order.getId(), OrderStatus.FINISHED);
        recentStatus.setStatusDatetime(Instant.now());
        order.getStatusHistory().add(recentStatus);
        
        assertEquals(OrderStatus.FINISHED, order.getActualStatus(), 
                    "Devrait retourner le statut le plus récent (FINISHED)");
    }
    
    @Test
    public void testGetDishOrders() {
        // Création d'une commande avec plats
        Order order = new Order("ORD-TEST-002");
        order.setId(1L);
        
        // Sans plats
        assertTrue(order.getDishOrders().isEmpty(), "Une nouvelle commande devrait avoir une liste de plats vide");
        
        // Avec des plats
        Dish dish1 = createDish(1L, "Pizza", 12.0);
        Dish dish2 = createDish(2L, "Salade", 8.0);
        
        DishOrder dishOrder1 = new DishOrder(order.getId(), dish1, 1);
        dishOrder1.setId(1L);
        
        DishOrder dishOrder2 = new DishOrder(order.getId(), dish2, 2);
        dishOrder2.setId(2L);
        
        order.addDishOrder(dishOrder1);
        order.addDishOrder(dishOrder2);
        
        List<DishOrder> dishOrders = order.getDishOrders();
        
        assertEquals(2, dishOrders.size(), "La commande devrait contenir 2 plats");
        assertEquals("Pizza", dishOrders.get(0).getDish().getName(), "Le premier plat devrait être une pizza");
        assertEquals("Salade", dishOrders.get(1).getDish().getName(), "Le deuxième plat devrait être une salade");
        assertEquals(2, dishOrders.get(1).getQuantity(), "La quantité de salade devrait être 2");
    }
    
    @Test
    public void testGetTotalAmount() {
        // Création d'une commande avec plats
        Order order = new Order("ORD-TEST-003");
        order.setId(1L);
        
        // Commande vide = 0
        assertEquals(0.0, order.getTotalAmount(), 0.01, "Une commande vide devrait avoir un montant total de 0");
        
        // Commande avec plats
        Dish dish1 = createDish(1L, "Pizza", 12.0);
        Dish dish2 = createDish(2L, "Salade", 8.0);
        
        DishOrder dishOrder1 = new DishOrder(order.getId(), dish1, 1); // 1 × 12€ = 12€
        dishOrder1.setId(1L);
        
        DishOrder dishOrder2 = new DishOrder(order.getId(), dish2, 2); // 2 × 8€ = 16€
        dishOrder2.setId(2L);
        
        order.addDishOrder(dishOrder1);
        order.addDishOrder(dishOrder2);
        
        double expectedTotal = 12.0 + (2 * 8.0); // 12€ + 16€ = 28€
        
        assertEquals(expectedTotal, order.getTotalAmount(), 0.01, 
                    "Le montant total devrait être la somme des prix des plats multipliés par leur quantité");
    }
    
    @Test
    public void testAddStatus() {
        Order order = new Order("ORD-TEST-004");
        order.setId(1L);
        
        // Le statut initial est CREATED
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
        
        // Transition valide vers CONFIRMED
        order.addStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getActualStatus());
        
        // Transition valide vers IN_PREPARATION
        order.addStatus(OrderStatus.IN_PREPARATION);
        assertEquals(OrderStatus.IN_PREPARATION, order.getActualStatus());
        
        // Transition valide vers FINISHED
        order.addStatus(OrderStatus.FINISHED);
        assertEquals(OrderStatus.FINISHED, order.getActualStatus());
        
        // Transition valide vers SERVED
        order.addStatus(OrderStatus.SERVED);
        assertEquals(OrderStatus.SERVED, order.getActualStatus());
        
        // Vérifier le nombre d'entrées dans l'historique
        assertEquals(5, order.getStatusHistory().size()); // CREATED + 4 transitions
    }
    
    @Test
    public void testInvalidStatusTransition() {
        Order order = new Order("ORD-TEST-005");
        order.setId(1L);
        
        // Le statut initial est CREATED
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
        
        // Tentative de transition invalide directement de CREATED à FINISHED
        assertThrows(IllegalStateException.class, () -> {
            order.addStatus(OrderStatus.FINISHED);
        });
        
        // Le statut ne devrait pas avoir changé
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
    }
    
    private Dish createDish(Long id, String name, Double price) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(price);
        dish.setDishIngredients(new ArrayList<>());
        return dish;
    }
} 