drop table if exists id_gen_tab;

create table id_gen_tab(
    id bigint unsigned not null auto_increment,
    primary key(id)
);

select * from id_gen_tab;