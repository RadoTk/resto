-- Insert sample orders
INSERT INTO "order" (reference, creation_datetime) VALUES
    ('ORD-001', '2023-03-01 12:30:00'),
    ('ORD-002', '2023-03-01 13:45:00'),
    ('ORD-003', '2023-03-02 18:15:00');

-- Insert sample order dishes
INSERT INTO order_dish (order_id, dish_id, quantity) VALUES
    (1, 1, 2), -- 2 of dish 1 in order 1
    (1, 2, 1), -- 1 of dish 2 in order 1
    (2, 3, 1), -- 1 of dish 3 in order 2
    (2, 1, 3), -- 3 of dish 1 in order 2
    (3, 2, 2); -- 2 of dish 2 in order 3

-- Insert sample order statuses
INSERT INTO order_status (order_id, status, status_datetime) VALUES
    (1, 'CREATED', '2023-03-01 12:30:00'),
    (1, 'CONFIRMED', '2023-03-01 12:32:00'),
    (1, 'IN_PREPARATION', '2023-03-01 12:35:00'),
    (1, 'FINISHED', '2023-03-01 12:50:00'),
    (1, 'SERVED', '2023-03-01 12:55:00'),
    (2, 'CREATED', '2023-03-01 13:45:00'),
    (2, 'CONFIRMED', '2023-03-01 13:47:00'),
    (2, 'IN_PREPARATION', '2023-03-01 13:50:00'),
    (3, 'CREATED', '2023-03-02 18:15:00');

-- Insert sample order dish statuses
INSERT INTO order_dish_status (order_dish_id, status, status_datetime) VALUES
    (1, 'CREATED', '2023-03-01 12:30:00'),
    (1, 'CONFIRMED', '2023-03-01 12:32:00'),
    (1, 'IN_PREPARATION', '2023-03-01 12:35:00'),
    (1, 'FINISHED', '2023-03-01 12:48:00'),
    (1, 'SERVED', '2023-03-01 12:55:00'),
    
    (2, 'CREATED', '2023-03-01 12:30:00'),
    (2, 'CONFIRMED', '2023-03-01 12:32:00'),
    (2, 'IN_PREPARATION', '2023-03-01 12:35:00'),
    (2, 'FINISHED', '2023-03-01 12:50:00'),
    (2, 'SERVED', '2023-03-01 12:55:00'),
    
    (3, 'CREATED', '2023-03-01 13:45:00'),
    (3, 'CONFIRMED', '2023-03-01 13:47:00'),
    (3, 'IN_PREPARATION', '2023-03-01 13:50:00'),
    
    (4, 'CREATED', '2023-03-01 13:45:00'),
    (4, 'CONFIRMED', '2023-03-01 13:47:00'),
    (4, 'IN_PREPARATION', '2023-03-01 13:50:00'),
    
    (5, 'CREATED', '2023-03-02 18:15:00'); 