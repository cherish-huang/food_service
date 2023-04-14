drop table if exists t_store;

create table t_store(
    id bigint unsigned not null,
    name varchar(255) not null,
    address varchar(255) not null,
    latitude double not null,
    Longitude double not null,
    primary key(id)
);

insert into t_store values(1, "肯德基", "深圳市南山区粤海街道01号", 12.1568, 56.1111);
insert into t_store values(2, "麦当劳", "深圳市宝安区西乡街道01号", 12.1568, 56.1111);
insert into t_store values(3, "真功夫", "上海市松江区大学城01号", 12.1568, 56.1111);
insert into t_store values(4, "必胜客", "上海市浦东新区01号", "12.1568", 56.1111);
