package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DishOrderCrudOperations {
    private final DataSource dataSource;
    private final DishCrudOperations dishCrudOperations;

    public DishOrderCrudOperations(DataSource dataSource, DishCrudOperations dishCrudOperations) {
        this.dataSource = dataSource;
        this.dishCrudOperations = dishCrudOperations;
    }

    public DishOrder create(DishOrder dishOrder) {
        String sql = "INSERT INTO order_dish (order_id, dish_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setLong(1, dishOrder.getOrderId());
            statement.setLong(2, dishOrder.getDish().getId());
            statement.setInt(3, dishOrder.getQuantity());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating dish order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    dishOrder.setId(id);
                    
                    // Create initial status if not already set
                    if (dishOrder.getStatusHistory() == null || dishOrder.getStatusHistory().isEmpty()) {
                        createDishOrderStatus(id, OrderDishStatus.CREATED, Instant.now());
                    } else {
                        // Save all statuses
                        for (OrderDishStatusEntry statusEntry : dishOrder.getStatusHistory()) {
                            statusEntry.setOrderDishId(id);
                            createDishOrderStatus(id, statusEntry.getStatus(), statusEntry.getStatusDatetime());
                        }
                    }
                    
                    return findById(id).orElse(dishOrder);
                } else {
                    throw new SQLException("Creating dish order failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating dish order: " + e.getMessage(), e);
        }
    }

    public Optional<DishOrder> findById(Long id) {
        String sql = "SELECT id, order_id, dish_id, quantity FROM order_dish WHERE id = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    DishOrder dishOrder = mapResultSetToDishOrder(resultSet);
                    
                    // Load status history
                    dishOrder.setStatusHistory(findDishOrderStatusHistory(id));
                    
                    return Optional.of(dishOrder);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding dish order: " + e.getMessage(), e);
        }
    }

    public List<DishOrder> findByOrderId(Long orderId) {
        String sql = "SELECT id, order_id, dish_id, quantity FROM order_dish WHERE order_id = ?";
        List<DishOrder> dishOrders = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, orderId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DishOrder dishOrder = mapResultSetToDishOrder(resultSet);
                    
                    // Load status history
                    dishOrder.setStatusHistory(findDishOrderStatusHistory(dishOrder.getId()));
                    
                    dishOrders.add(dishOrder);
                }
            }
            
            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding dish orders by order ID: " + e.getMessage(), e);
        }
    }

    public void updateDishOrderStatus(Long dishOrderId, OrderDishStatus status) {
        createDishOrderStatus(dishOrderId, status, Instant.now());
    }
    
    private void createDishOrderStatus(Long dishOrderId, OrderDishStatus status, Instant datetime) {
        String sql = "INSERT INTO order_dish_status (order_dish_id, status, status_datetime) VALUES (?, ?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, dishOrderId);
            statement.setString(2, status.name());
            statement.setTimestamp(3, Timestamp.from(datetime));
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error creating dish order status: " + e.getMessage(), e);
        }
    }
    
    private List<OrderDishStatusEntry> findDishOrderStatusHistory(Long dishOrderId) {
        String sql = "SELECT id, order_dish_id, status, status_datetime FROM order_dish_status WHERE order_dish_id = ? ORDER BY status_datetime";
        List<OrderDishStatusEntry> statusHistory = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, dishOrderId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderDishStatusEntry statusEntry = new OrderDishStatusEntry(
                            resultSet.getLong("id"),
                            resultSet.getLong("order_dish_id"),
                            OrderDishStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("status_datetime").toInstant()
                    );
                    statusHistory.add(statusEntry);
                }
            }
            
            return statusHistory;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding dish order status history: " + e.getMessage(), e);
        }
    }
    
    private DishOrder mapResultSetToDishOrder(ResultSet resultSet) throws SQLException {
        Long dishId = resultSet.getLong("dish_id");
        Dish dish = dishCrudOperations.findById(dishId);
        
        return new DishOrder(
                resultSet.getLong("id"),
                resultSet.getLong("order_id"),
                dish,
                resultSet.getInt("quantity"),
                null
        );
    }

    public DishOrder saveWithTransaction(Connection connection, DishOrder dishOrder) throws SQLException {
        if (dishOrder.getId() == null) {
            return createWithTransaction(connection, dishOrder);
        } else {
            return updateWithTransaction(connection, dishOrder);
        }
    }

    private DishOrder createWithTransaction(Connection connection, DishOrder dishOrder) throws SQLException {
        String sql = "INSERT INTO order_dish (order_id, dish_id, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, dishOrder.getOrderId());
            statement.setLong(2, dishOrder.getDish().getId());
            statement.setInt(3, dishOrder.getQuantity());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating dish order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    dishOrder.setId(id);
                    
                    // Sauvegarder les statuts du plat
                    if (dishOrder.getStatusHistory() != null) {
                        for (OrderDishStatusEntry statusEntry : dishOrder.getStatusHistory()) {
                            createDishOrderStatusWithTransaction(connection, id, statusEntry.getStatus(), statusEntry.getStatusDatetime());
                        }
                    }
                    
                    return dishOrder;
                } else {
                    throw new SQLException("Creating dish order failed, no ID obtained.");
                }
            }
        }
    }

    private DishOrder updateWithTransaction(Connection connection, DishOrder dishOrder) throws SQLException {
        String sql = "UPDATE order_dish SET order_id = ?, dish_id = ?, quantity = ? WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishOrder.getOrderId());
            statement.setLong(2, dishOrder.getDish().getId());
            statement.setInt(3, dishOrder.getQuantity());
            statement.setLong(4, dishOrder.getId());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating dish order failed, no rows affected.");
            }
            
            // Sauvegarder les nouveaux statuts du plat
            if (dishOrder.getStatusHistory() != null) {
                for (OrderDishStatusEntry statusEntry : dishOrder.getStatusHistory()) {
                    if (statusEntry.getId() == null) {
                        createDishOrderStatusWithTransaction(connection, dishOrder.getId(), 
                                                          statusEntry.getStatus(), 
                                                          statusEntry.getStatusDatetime());
                    }
                }
            }
            
            return dishOrder;
        }
    }

    /**
     * Création d'un statut de plat de commande dans une transaction existante
     */
    private void createDishOrderStatusWithTransaction(Connection connection, Long dishOrderId, 
                                                   OrderDishStatus status, Instant statusDatetime) 
            throws SQLException {
        String sql = "INSERT INTO order_dish_status (order_dish_id, status, status_datetime) VALUES (?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishOrderId);
            statement.setString(2, status.name());
            statement.setTimestamp(3, Timestamp.from(statusDatetime));
            
            statement.executeUpdate();
        }
    }

    public DishOrder save(DishOrder dishOrder) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            
            DishOrder savedDishOrder = saveWithTransaction(connection, dishOrder);
            
            connection.commit();
            return savedDishOrder;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erreur lors du rollback de la transaction: " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat de commande: " + e.getMessage(), e);
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
} 