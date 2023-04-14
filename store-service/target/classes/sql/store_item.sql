drop table if exists t_store_item;

create table t_store_item(
    id bigint unsigned not null,
    name varchar(255) not null,
    price int unsigned  not null,
    store_id bigint unsigned not null,
    primary key(id)
);

insert into t_store_item values(1, "汉堡包", 25, 1);
insert into t_store_item values(2, "鸡肉卷", 20, 2);
insert into t_store_item values(3, "香菇滑鸡肉", 18, 3);
insert into t_store_item values(4, "炸鸡翅", 22, 4);