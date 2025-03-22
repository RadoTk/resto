package edu.restaurant.app.dao.operations;

import edu.restaurant.app.dao.DataSource;
import edu.restaurant.app.dao.entity.Price;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceCrudOperations implements CrudOperations<Price> {
    private final DataSource dataSource = new DataSource();

    @Override
    public List<Price> getAll(int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Price findById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Price> saveAll(List<Price> entities) {
        List<Price> prices = new ArrayList<>();
        String sql = "INSERT INTO price (id, amount, date_value, id_ingredient) VALUES (?, ?, ?, ?) "
                   + "ON CONFLICT (id) DO NOTHING RETURNING id, amount, date_value, id_ingredient";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            for (Price entityToSave : entities) {
                try {
                    statement.setLong(1, entityToSave.getId());
                    statement.setDouble(2, entityToSave.getAmount());
                    statement.setDate(3, Date.valueOf(entityToSave.getDateValue()));
                    statement.setLong(4, entityToSave.getIngredient().getId());
                    statement.addBatch(); // group by batch so executed as one query in database
                } catch (SQLException e) {
                    throw new RuntimeException("Error while adding price to batch: " + entityToSave.getId(), e);
                }
            }
            statement.executeBatch(); // Execute the batch

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                while (resultSet.next()) {
                    prices.add(mapFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving prices", e);
        }
        return prices;
    }

    public List<Price> findByIdIngredient(Long idIngredient) {
        List<Price> prices = new ArrayList<>();
        String sql = "SELECT p.id, p.amount, p.date_value FROM price p "
                   + "JOIN ingredient i ON p.id_ingredient = i.id "
                   + "WHERE p.id_ingredient = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            statement.setLong(1, idIngredient);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Price price = mapFromResultSet(resultSet);
                    prices.add(price);
                }
                return prices;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding prices by ingredient id: " + idIngredient, e);
        }
    }

    private Price mapFromResultSet(ResultSet resultSet) throws SQLException {
        Price price = new Price();
        price.setId(resultSet.getLong("id"));
        price.setAmount(resultSet.getDouble("amount"));
        price.setDateValue(resultSet.getDate("date_value").toLocalDate());
        return price;
    }
}
