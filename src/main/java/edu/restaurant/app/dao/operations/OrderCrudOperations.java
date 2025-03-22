package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.DishOrder;
import edu.restaurant.app.dao.entity.Order;
import edu.restaurant.app.dao.entity.OrderStatus;
import edu.restaurant.app.dao.entity.OrderStatusEntry;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderCrudOperations {
    private final DataSource dataSource;
    private final DishOrderCrudOperations dishOrderCrudOperations;

    public OrderCrudOperations(DataSource dataSource, DishOrderCrudOperations dishOrderCrudOperations) {
        this.dataSource = dataSource;
        this.dishOrderCrudOperations = dishOrderCrudOperations;
    }

    public Order create(Order order) {
        String sql = "INSERT INTO \"order\" (reference, creation_datetime) VALUES (?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, order.getReference());
            statement.setTimestamp(2, Timestamp.from(order.getCreationDatetime()));
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    order.setId(id);
                    
                    // Create initial status
                    createOrderStatus(order.getId(), OrderStatus.CREATED, order.getCreationDatetime());
                    
                    // Create dish orders if any
                    if (order.getDishOrders() != null && !order.getDishOrders().isEmpty()) {
                        order.getDishOrders().forEach(dishOrder -> {
                            dishOrder.setOrderId(id);
                            dishOrderCrudOperations.create(dishOrder);
                        });
                    }
                    
                    return findById(id).orElse(order);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT id, reference, creation_datetime FROM \"order\" WHERE id = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = mapResultSetToOrder(resultSet);
                    
                    // Load dish orders
                    order.setDishOrders(dishOrderCrudOperations.findByOrderId(id));
                    
                    // Load status history
                    order.setStatusHistory(findOrderStatusHistory(id));
                    
                    return Optional.of(order);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order: " + e.getMessage(), e);
        }
    }
    
    public Optional<Order> findByReference(String reference) {
        String sql = "SELECT id, reference, creation_datetime FROM \"order\" WHERE reference = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, reference);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = mapResultSetToOrder(resultSet);
                    
                    // Load dish orders
                    order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                    
                    // Load status history
                    order.setStatusHistory(findOrderStatusHistory(order.getId()));
                    
                    return Optional.of(order);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order by reference: " + e.getMessage(), e);
        }
    }

    public List<Order> findAll() {
        String sql = "SELECT id, reference, creation_datetime FROM \"order\"";
        List<Order> orders = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Order order = mapResultSetToOrder(resultSet);
                
                // Load dish orders
                order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                
                // Load status history
                order.setStatusHistory(findOrderStatusHistory(order.getId()));
                
                orders.add(order);
            }
            
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all orders: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM \"order\" WHERE id = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting order: " + e.getMessage(), e);
        }
    }
    
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        createOrderStatus(orderId, status, Instant.now());
    }
    
    private void createOrderStatus(Long orderId, OrderStatus status, Instant datetime) {
        String sql = "INSERT INTO order_status (order_id, status, status_datetime) VALUES (?, ?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, orderId);
            statement.setString(2, status.name());
            statement.setTimestamp(3, Timestamp.from(datetime));
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error creating order status: " + e.getMessage(), e);
        }
    }
    
    private List<OrderStatusEntry> findOrderStatusHistory(Long orderId) {
        String sql = "SELECT id, order_id, status, status_datetime FROM order_status WHERE order_id = ? ORDER BY status_datetime";
        List<OrderStatusEntry> statusHistory = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, orderId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderStatusEntry statusEntry = new OrderStatusEntry(
                            resultSet.getLong("id"),
                            resultSet.getLong("order_id"),
                            OrderStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("status_datetime").toInstant()
                    );
                    statusHistory.add(statusEntry);
                }
            }
            
            return statusHistory;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order status history: " + e.getMessage(), e);
        }
    }
    
    private Order mapResultSetToOrder(ResultSet resultSet) throws SQLException {
        return new Order(
                resultSet.getLong("id"),
                resultSet.getString("reference"),
                resultSet.getTimestamp("creation_datetime").toInstant(),
                null,
                null
        );
    }

    public Order save(Order order) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            
            Order savedOrder;
            if (order.getId() == null) {
                // Cas d'une nouvelle commande
                savedOrder = createWithTransaction(connection, order);
            } else {
                // Cas d'une mise à jour
                savedOrder = updateWithTransaction(connection, order);
            }
            
            // Sauvegarder les plats associés
            if (savedOrder.getDishOrders() != null) {
                for (DishOrder dishOrder : savedOrder.getDishOrders()) {
                    dishOrder.setOrderId(savedOrder.getId());
                    dishOrderCrudOperations.saveWithTransaction(connection, dishOrder);
                }
            }
            
            // Sauvegarder les statuts
            if (savedOrder.getStatusHistory() != null) {
                for (OrderStatusEntry statusEntry : savedOrder.getStatusHistory()) {
                    if (statusEntry.getId() == null) {
                        // Nouvelle entrée de statut
                        createOrderStatusWithTransaction(connection, savedOrder.getId(), 
                                                       statusEntry.getStatus(), 
                                                       statusEntry.getStatusDatetime());
                    }
                }
            }
            
            connection.commit();
            return savedOrder;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erreur lors du rollback de la transaction: " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de la commande: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Erreur lors de la fermeture de la connexion: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Création d'une commande dans une transaction existante
     */
    private Order createWithTransaction(Connection connection, Order order) throws SQLException {
        String sql = "INSERT INTO \"order\" (reference, creation_datetime) VALUES (?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, order.getReference());
            statement.setTimestamp(2, Timestamp.from(order.getCreationDatetime()));
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    order.setId(id);
                    
                    // Créer le statut initial
                    createOrderStatusWithTransaction(connection, id, OrderStatus.CREATED, order.getCreationDatetime());
                    
                    return order;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Mise à jour d'une commande dans une transaction existante
     */
    private Order updateWithTransaction(Connection connection, Order order) throws SQLException {
        String sql = "UPDATE \"order\" SET reference = ?, creation_datetime = ? WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getReference());
            statement.setTimestamp(2, Timestamp.from(order.getCreationDatetime()));
            statement.setLong(3, order.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating order failed, no rows affected.");
            }
            
            return order;
        }
    }

    /**
     * Création d'un statut de commande dans une transaction existante
     */
    private void createOrderStatusWithTransaction(Connection connection, Long orderId, OrderStatus status, Instant datetime) 
            throws SQLException {
        String sql = "INSERT INTO order_status (order_id, status, status_datetime) VALUES (?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setString(2, status.name());
            statement.setTimestamp(3, Timestamp.from(datetime));
            
            statement.executeUpdate();
        }
    }
} 