create table if not exists price
(
    id            bigserial primary key,
    amount        numeric,
    date_value    date,
    id_ingredient bigint,
    constraint fk_price_id_ingredient foreign key (id_ingredient) references ingredient (id)
);