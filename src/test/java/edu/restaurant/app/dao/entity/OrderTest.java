package edu.restaurant.app.dao.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.util.stream.Collectors;

public class OrderTest {

    @Test
    public void testOrderCreation() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-001");
        assertNotNull(order);
        assertEquals("ORD-TEST-001", order.getReference());
        assertNotNull(order.getCreationDatetime());
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
    }
    
    @Test
    public void testAddDishesToOrder() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-002");
        
        // Création des ingrédients
        Ingredient pain = createIngredient(1L, "Pain", 1.0);
        Ingredient saucisse = createIngredient(2L, "Saucisse", 2.0);
        
        // Création d'un plat avec ingrédients
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(pain);
        di1.setRequiredQuantity(1.0);
        
        DishIngredient di2 = new DishIngredient();
        di2.setIngredient(saucisse);
        di2.setRequiredQuantity(1.0);
        
        List<DishIngredient> ingredients = new ArrayList<>();
        ingredients.add(di1);
        ingredients.add(di2);
        
        Dish hotDog = createDish(1L, "Hot Dog", 5000.0, ingredients);
        
        // Ajout de plats à la commande
        DishOrder dishOrder = new DishOrder(1L, hotDog, 2);
        order.addDishOrder(dishOrder);
        
        assertEquals(1, order.getDishOrders().size());
        assertEquals(2, order.getDishOrders().get(0).getQuantity());
        assertEquals(10000.0, order.getTotalAmount());
    }
    
    @Test
    public void testOrderStatusTransition() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-003");
        order.setId(1L);
        
        // Vérification du statut initial
        OrderStatus initialStatus = order.getActualStatus();
        System.out.println("Initial status: " + initialStatus);
        assertEquals(OrderStatus.CREATED, initialStatus);
        
        // Passage au statut CONFIRMED
        order.confirm();
        OrderStatus confirmedStatus = order.getActualStatus();
        System.out.println("After CONFIRMED transition: " + confirmedStatus);
        assertEquals(OrderStatus.CONFIRMED, confirmedStatus);
        
        // Passage au statut IN_PREPARATION
        order.addStatus(OrderStatus.IN_PREPARATION);
        OrderStatus inPrepStatus = order.getActualStatus();
        System.out.println("After IN_PREPARATION transition: " + inPrepStatus);
        assertEquals(OrderStatus.IN_PREPARATION, inPrepStatus);
        
        // Passage au statut FINISHED
        System.out.println("Attempting to transition to FINISHED");
        order.addStatus(OrderStatus.FINISHED);
        OrderStatus finishedStatus = order.getActualStatus();
        System.out.println("After FINISHED transition: " + finishedStatus);
        assertEquals(OrderStatus.FINISHED, finishedStatus);
        
        // Passage au statut SERVED
        order.addStatus(OrderStatus.SERVED);
        OrderStatus servedStatus = order.getActualStatus();
        System.out.println("After SERVED transition: " + servedStatus);
        assertEquals(OrderStatus.SERVED, servedStatus);
    }
    
    @Test
    public void testInvalidStatusTransition() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-004");
        
        // Tentative de passage de CREATED à FINISHED (non autorisé)
        assertThrows(IllegalStateException.class, () -> {
            order.addStatus(OrderStatus.FINISHED);
        });
        
        // Vérification que le statut est toujours CREATED
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
    }
    
    @Test
    public void testDishStatusUpdatesWithOrderStatus() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-005");
        order.setId(1L);
        
        // Création des ingrédients avec stock suffisant
        Ingredient pain = createIngredientWithStock(1L, "Pain", 1.0, 10.0);
        Ingredient saucisse = createIngredientWithStock(2L, "Saucisse", 2.0, 10.0);
        
        // Création d'un plat avec ingrédients
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(pain);
        di1.setRequiredQuantity(1.0);
        
        DishIngredient di2 = new DishIngredient();
        di2.setIngredient(saucisse);
        di2.setRequiredQuantity(1.0);
        
        List<DishIngredient> ingredients = new ArrayList<>();
        ingredients.add(di1);
        ingredients.add(di2);
        
        Dish hotDog = createDish(1L, "Hot Dog", 5.0, ingredients);
        
        // Ajout de plats à la commande
        DishOrder dishOrder = new DishOrder(order.getId(), hotDog, 2);
        dishOrder.setId(1L);
        order.addDishOrder(dishOrder);
        
        // Passage au statut CONFIRMED
        order.confirm();
        
        // Vérification que les plats sont aussi CONFIRMED
        assertEquals(OrderDishStatus.CONFIRMED, order.getDishOrders().get(0).getActualStatus());
        
        // Passage au statut IN_PREPARATION
        order.addStatus(OrderStatus.IN_PREPARATION);
        
        // Vérification que les plats sont aussi IN_PREPARATION
        assertEquals(OrderDishStatus.IN_PREPARATION, order.getDishOrders().get(0).getActualStatus());
    }
    
    @Test
    public void testOrderConfirmationWithInsufficientIngredients() {
        // Création d'une commande
        Order order = new Order("ORD-TEST-009");
        order.setId(1L);
        
        // Création des ingrédients avec stock limité
        Ingredient pain = createIngredientWithStock(1L, "Pain", 1.0, 3.0); // Stock: 3 pains
        Ingredient saucisse = createIngredientWithStock(2L, "Saucisse", 2.0, 1.0); // Stock: 1 saucisse
        
        // Création d'un plat (hot dog)
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(pain);
        di1.setRequiredQuantity(1.0); // Besoin de 1 pain par hot dog
        
        DishIngredient di2 = new DishIngredient();
        di2.setIngredient(saucisse);
        di2.setRequiredQuantity(1.0); // Besoin de 1 saucisse par hot dog
        
        List<DishIngredient> ingredients = new ArrayList<>();
        ingredients.add(di1);
        ingredients.add(di2);
        
        Dish hotDog = createDish(1L, "Hot Dog", 5.0, ingredients);
        
        // Ajout de 2 hot dogs à la commande (devrait fonctionner car on a assez de pain mais pas assez de saucisses)
        DishOrder dishOrder = new DishOrder(order.getId(), hotDog, 2);
        dishOrder.setId(1L);
        order.addDishOrder(dishOrder);
        
        // Vérification que la commande est au statut CREATED
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
        
        // La confirmation devrait échouer en raison du manque de saucisses
        Exception exception = assertThrows(InsufficientIngredientsException.class, () -> {
            order.confirm();
        });
        
        // Vérification du message d'erreur
        String expectedMessage = "Ingrédients insuffisants: 1 Saucisse est nécessaire pour fabriquer 1 Hot Dog supplémentaire";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), 
                   "Message d'erreur attendu: " + expectedMessage + ", obtenu: " + actualMessage);
        
        // La commande devrait rester au statut CREATED
        assertEquals(OrderStatus.CREATED, order.getActualStatus());
        
        // Modification de la commande pour n'avoir qu'un seul hot dog (devrait fonctionner)
        order.getDishOrders().clear();
        DishOrder singleHotDog = new DishOrder(order.getId(), hotDog, 1);
        singleHotDog.setId(2L);
        order.addDishOrder(singleHotDog);
        
        // La confirmation devrait maintenant réussir
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getActualStatus());
    }

    @Test
    public void testOrderAutoStatusUpdateWhenAllDishesAreFinished() {
        // Création d'une commande avec deux plats
        Order order = new Order("ORD-TEST-007");
        order.setId(1L);
        
        // Création des ingrédients avec stock suffisant
        Ingredient pain = createIngredientWithStock(1L, "Pain", 1.0, 10.0);
        Ingredient saucisse = createIngredientWithStock(2L, "Saucisse", 2.0, 10.0);
        Ingredient oeuf = createIngredientWithStock(3L, "Oeuf", 0.5, 10.0);
        
        // Création des plats avec ingrédients
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(pain);
        di1.setRequiredQuantity(1.0);
        
        DishIngredient di2 = new DishIngredient();
        di2.setIngredient(saucisse);
        di2.setRequiredQuantity(1.0);
        
        List<DishIngredient> hotDogIngredients = new ArrayList<>();
        hotDogIngredients.add(di1);
        hotDogIngredients.add(di2);
        
        Dish hotDog = createDish(1L, "Hot Dog", 5.0, hotDogIngredients);
        
        DishIngredient di3 = new DishIngredient();
        di3.setIngredient(oeuf);
        di3.setRequiredQuantity(2.0);
        
        List<DishIngredient> omeletteIngredients = new ArrayList<>();
        omeletteIngredients.add(di3);
        
        Dish omelette = createDish(2L, "Omelette", 4.0, omeletteIngredients);
        
        // Ajout des plats à la commande
        DishOrder dishOrder1 = new DishOrder(order.getId(), hotDog, 1);
        dishOrder1.setId(1L);
        order.addDishOrder(dishOrder1);
        
        DishOrder dishOrder2 = new DishOrder(order.getId(), omelette, 1);
        dishOrder2.setId(2L);
        order.addDishOrder(dishOrder2);
        
        // Mettre la commande en préparation
        order.confirm();
        order.addStatus(OrderStatus.IN_PREPARATION);
        
        System.out.println("Initial state: order status = " + order.getActualStatus());
        System.out.println("Initial dish statuses: " + order.getDishOrders().stream()
                .map(d -> d.getDish().getName() + "=" + d.getActualStatus())
                .collect(Collectors.joining(", ")));
        
        // Marquer le premier plat comme terminé
        dishOrder1.addStatus(OrderDishStatus.FINISHED);
        
        System.out.println("After first dish FINISHED: dish statuses = " + order.getDishOrders().stream()
                .map(d -> d.getDish().getName() + "=" + d.getActualStatus())
                .collect(Collectors.joining(", ")));
        
        // La commande devrait encore être EN_PREPARATION
        order.updateStatusBasedOnDishes();
        System.out.println("After updateStatusBasedOnDishes, order status = " + order.getActualStatus());
        assertEquals(OrderStatus.IN_PREPARATION, order.getActualStatus());
        
        // Marquer le deuxième plat comme terminé
        dishOrder2.addStatus(OrderDishStatus.FINISHED);
        
        System.out.println("After all dishes FINISHED: dish statuses = " + order.getDishOrders().stream()
                .map(d -> d.getDish().getName() + "=" + d.getActualStatus())
                .collect(Collectors.joining(", ")));
        
        // La commande devrait maintenant être FINISHED
        order.updateStatusBasedOnDishes();
        System.out.println("Final state: order status = " + order.getActualStatus());
        assertEquals(OrderStatus.FINISHED, order.getActualStatus());
    }
    
    @Test
    public void testOrderAutoStatusUpdateWhenAllDishesAreServed() {
        // Création d'une commande avec deux plats
        Order order = new Order("ORD-TEST-008");
        order.setId(1L);
        
        // Création des ingrédients avec stock suffisant
        Ingredient pain = createIngredientWithStock(1L, "Pain", 1.0, 10.0);
        Ingredient saucisse = createIngredientWithStock(2L, "Saucisse", 2.0, 10.0);
        
        // Création d'un plat
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(pain);
        di1.setRequiredQuantity(1.0);
        
        DishIngredient di2 = new DishIngredient();
        di2.setIngredient(saucisse);
        di2.setRequiredQuantity(1.0);
        
        List<DishIngredient> ingredients = new ArrayList<>();
        ingredients.add(di1);
        ingredients.add(di2);
        
        Dish hotDog = createDish(1L, "Hot Dog", 5.0, ingredients);
        
        // Ajout des plats à la commande
        DishOrder dishOrder1 = new DishOrder(order.getId(), hotDog, 1);
        dishOrder1.setId(1L);
        order.addDishOrder(dishOrder1);
        
        DishOrder dishOrder2 = new DishOrder(order.getId(), hotDog, 1);
        dishOrder2.setId(2L);
        order.addDishOrder(dishOrder2);
        
        // Configurer les statuts (simuler une commande qui est passée par tous les états)
        order.confirm();
        order.addStatus(OrderStatus.IN_PREPARATION);
        
        // Mettre à jour les plats en FINISHED
        dishOrder1.addStatus(OrderDishStatus.FINISHED);
        dishOrder2.addStatus(OrderDishStatus.FINISHED);
        
        // Marquer la commande comme FINISHED manuellement
        System.out.println("Setting order status to FINISHED manually");
        order.addStatus(OrderStatus.FINISHED);
        assertEquals(OrderStatus.FINISHED, order.getActualStatus());
        
        // Marquer le premier plat comme servi
        dishOrder1.addStatus(OrderDishStatus.SERVED);
        
        // La commande devrait encore être FINISHED
        order.updateStatusBasedOnDishes();
        System.out.println("After first dish served, order status: " + order.getActualStatus());
        assertEquals(OrderStatus.FINISHED, order.getActualStatus());
        
        // Marquer le deuxième plat comme servi
        dishOrder2.addStatus(OrderDishStatus.SERVED);
        
        // La commande devrait maintenant être SERVED
        order.updateStatusBasedOnDishes();
        System.out.println("After all dishes served, order status: " + order.getActualStatus());
        assertEquals(OrderStatus.SERVED, order.getActualStatus());
    }
    
    // Méthodes utilitaires pour la création d'objets de test
    
    private Ingredient createIngredient(Long id, String name, Double price) {
        List<Price> prices = new ArrayList<>();
        Price priceObj = new Price();
        priceObj.setId(id);
        priceObj.setAmount(price);
        priceObj.setDateValue(LocalDate.now());
        prices.add(priceObj);
        
        return new Ingredient(id, name, prices, new ArrayList<>());
    }
    
    private Ingredient createIngredientWithStock(Long id, String name, Double price, Double stock) {
        Ingredient ingredient = createIngredient(id, name, price);
        
        // Ajouter un mouvement de stock entrant
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(id);
        stockMovement.setQuantity(stock);
        stockMovement.setMovementType(StockMovementType.IN);
        stockMovement.setCreationDatetime(Instant.now());
        stockMovement.setIngredient(ingredient);
        
        List<StockMovement> movements = new ArrayList<>();
        movements.add(stockMovement);
        ingredient.setStockMovements(movements);
        
        return ingredient;
    }
    
    private Dish createDish(Long id, String name, Double price, List<DishIngredient> ingredients) {
        return new Dish(id, name, ingredients, price);
    }
} 