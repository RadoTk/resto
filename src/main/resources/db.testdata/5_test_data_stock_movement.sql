insert into stock_movement (id, quantity, unit, movement_type, creation_datetime, id_ingredient)
values (1, 100, 'U', 'IN', '2025-02-01T05:00:00', 1),
       (2, 50, 'U', 'IN', '2025-02-01T05:00:00', 4),
       (3, 10000, 'G', 'IN', '2025-02-01T05:00:00', 2),
       (4, 20, 'L', 'IN', '2025-02-01T05:00:00', 3),
       (5, 10, 'U', 'OUT', '2025-02-10T07:00:00', 1),
       (6, 10, 'U', 'OUT', '2025-02-03T12:00:00', 1),
       (7, 20, 'U', 'OUT', '2025-02-20T12:00:00', 4)
on conflict do nothing;