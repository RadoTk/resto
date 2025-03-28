CREATE TABLE IF NOT EXISTS order_status (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE
); 