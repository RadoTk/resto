package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.Dish;
import edu.restaurant.app.dao.entity.DishIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishCrudOperations implements CrudOperations<Dish> {
    private final DataSource dataSource = new DataSource();
    private final IngredientCrudOperations ingredientCrudOperations = new IngredientCrudOperations();

    @Override
    public List<Dish> getAll(int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dish findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT d.id, d.name, d.price FROM dish d WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapFromResultSet(resultSet);
                }
            }
            throw new RuntimeException("Dish.id=" + id + " not found");
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding dish by id: " + id, e);
        }
    }

    private Dish mapFromResultSet(ResultSet resultSet) throws SQLException {
        Long idDish = resultSet.getLong("id");

        Dish dish = new Dish();
        dish.setId(idDish);
        dish.setName(resultSet.getString("name"));
        dish.setPrice(resultSet.getDouble("price"));
        List<DishIngredient> dishIngredients = ingredientCrudOperations.findByDishId(idDish);
        dish.setDishIngredients(dishIngredients);

        return dish;
    }

    @Override
    public List<Dish> saveAll(List<Dish> entities) {
        List<Dish> dishes = new ArrayList<>();
        String sql = "INSERT INTO dish (id, name, price) VALUES (?, ?, ?) "
                   + "ON CONFLICT (id) DO UPDATE SET name = excluded.name, price = excluded.price "
                   + "RETURNING id, name, price";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            for (Dish entityToSave : entities) {
                try {
                    statement.setLong(1, entityToSave.getId());
                    statement.setString(2, entityToSave.getName());
                    statement.setDouble(3, entityToSave.getPrice());
                    statement.addBatch(); // group by batch so executed as one query in database
                } catch (SQLException e) {
                    throw new RuntimeException("Error while adding dish to batch: " + entityToSave.getId(), e);
                }
            }
            statement.executeBatch(); // Execute the batch

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    dishes.add(mapFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving dishes", e);
        }
        return dishes;
    }
}
