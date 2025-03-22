insert into dish_ingredient(id, id_dish, id_ingredient, required_quantity, unit)
values (1, 1, 1, 1, 'U'),
       (2, 1, 2, 100, 'G'),
       (3, 1, 3, 0.15, 'L'),
       (4, 1, 4, 1, 'U')
on conflict do nothing ;