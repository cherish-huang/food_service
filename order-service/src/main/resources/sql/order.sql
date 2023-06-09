drop table if exists t_order_0000;
create table t_order_0000(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0001;
create table t_order_0001(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0002;
create table t_order_0002(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0003;
create table t_order_0003(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0004;
create table t_order_0004(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0005;
create table t_order_0005(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0006;
create table t_order_0006(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0007;
create table t_order_0007(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0008;
create table t_order_0008(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);

drop table if exists t_order_0009;
create table t_order_0009(
  id bigint NOT NULL,
  status tinyint NOT NULL,
  buyer_id bigint NOT NULL,
  store_id bigint NOT NULL,
  flag bigint NOT NULL default 0,
  total_amount double NOT NULL,
  delivery_fee double NOT NULL,
  delivery_name varchar(255) NOT NULL,
  delivery_phone varchar(255) NOT NULL,
  delivery_latitude double NOT NULL,
  delivery_longitude double NOT NULL,
  delivery_address varchar(255) NOT NULL,
  delivery_distance double NOT NULL,
  is_merchant_visible tinyint NOT NULL default 0,
  merchant_deadline bigint NOT NULL default 0,
  submit_time bigint NOT NULL default 0,
  pay_time bigint NOT NULL default 0,
  approve_time bigint NOT NULL default 0,
  merchant_confirm_time bigint NOT NULL default 0,
  driver_accept_time bigint NOT NULL default 0,
  driver_arrive_time bigint NOT NULL default 0,
  driver_pickup_time bigint NOT NULL default 0,
  deliver_time bigint NOT NULL default 0,
  complete_time bigint NOT NULL default 0,
  cancel_time bigint NOT NULL default 0,
  cancel_source int  NOT NULL default 0,
  cancel_reason int NOT NULL default 0,
  status_update_time bigint NOT NULL default 0,
  primary key (id)
);