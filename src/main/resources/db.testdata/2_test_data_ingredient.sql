insert into ingredient(id, name)
values (1, 'Oeuf'),
       (2, 'Saucisse'),
       (3, 'Huile'),
       (4, 'Pain')
on conflict do nothing;