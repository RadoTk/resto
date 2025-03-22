create table if not exists dish
(
    id    bigserial primary key,
    name  varchar,
    price numeric
);