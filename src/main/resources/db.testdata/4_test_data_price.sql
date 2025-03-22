insert into price(id, amount, date_value, id_ingredient)
values (1, 1000, '2025-03-15', 1),
       (2, 20, '2025-03-15', 2),
       (3, 10000, '2025-03-15', 3),
       (4, 1000, '2025-03-15', 4)
on conflict do nothing ;