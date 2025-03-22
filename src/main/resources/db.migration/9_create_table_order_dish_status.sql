CREATE TABLE IF NOT EXISTS order_dish_status (
    id SERIAL PRIMARY KEY,
    order_dish_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_dish_id) REFERENCES order_dish(id) ON DELETE CASCADE
); 