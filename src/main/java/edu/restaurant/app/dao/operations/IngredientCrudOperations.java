package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientCrudOperations implements CrudOperations<Ingredient> {
    private final DataSource dataSource = new DataSource();
    private final PriceCrudOperations priceCrudOperations = new PriceCrudOperations();
    private final StockMovementCrudOperations stockMovementCrudOperations = new StockMovementCrudOperations();

    @Override
    public List<Ingredient> getAll(int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Ingredient findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT i.id, i.name, di.id AS dish_ingredient_id, di.required_quantity, di.unit FROM ingredient i"
                     + " JOIN dish_ingredient di ON i.id = di.id_ingredient"
                     + " WHERE i.id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapFromResultSet(resultSet);
                }
                throw new RuntimeException("Ingredient.id=" + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding ingredient by id: " + id, e);
        }
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> entities) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "INSERT INTO ingredient (name) VALUES (?) ON CONFLICT (id) DO UPDATE SET name = excluded.name RETURNING id, name";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            for (Ingredient entityToSave : entities) {
                try {
                    statement.setString(1, entityToSave.getName());
                    statement.addBatch(); // group by batch so executed as one query in database
                } catch (SQLException e) {
                    throw new RuntimeException("Error while adding ingredient to batch: " + entityToSave.getName(), e);
                }
            }
            statement.executeBatch(); // Execute the batch

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    ingredients.add(mapFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving ingredients", e);
        }
        return ingredients;
    }

    public List<DishIngredient> findByDishId(Long dishId) {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT i.id, i.name, di.id AS dish_ingredient_id, di.required_quantity, di.unit FROM ingredient i"
                     + " JOIN dish_ingredient di ON i.id = di.id_ingredient"
                     + " WHERE di.id_dish = ?")) {
            statement.setLong(1, dishId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = mapFromResultSet(resultSet);
                    DishIngredient dishIngredient = mapDishIngredient(resultSet, ingredient);
                    dishIngredients.add(dishIngredient);
                }
                return dishIngredients;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding dish ingredients by dish id: " + dishId, e);
        }
    }

    private Ingredient mapFromResultSet(ResultSet resultSet) throws SQLException {
        Long idIngredient = resultSet.getLong("id");
        List<Price> ingredientPrices = priceCrudOperations.findByIdIngredient(idIngredient);
        List<StockMovement> ingredientStockMovements = stockMovementCrudOperations.findByIdIngredient(idIngredient);

        Ingredient ingredient = new Ingredient();
        ingredient.setId(idIngredient);
        ingredient.setName(resultSet.getString("name"));
        ingredient.setPrices(ingredientPrices);
        ingredient.setStockMovements(ingredientStockMovements);
        return ingredient;
    }

    private DishIngredient mapDishIngredient(ResultSet resultSet, Ingredient ingredient) throws SQLException {
        double requiredQuantity = resultSet.getDouble("required_quantity");
        Unit unit = Unit.valueOf(resultSet.getString("unit"));
        Long dishIngredientId = resultSet.getLong("dish_ingredient_id");
        return new DishIngredient(dishIngredientId, ingredient, requiredQuantity, unit);
    }
}
