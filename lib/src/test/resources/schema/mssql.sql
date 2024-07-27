drop procedure if exists hui;
---
create procedure hui
as begin
    DECLARE @DropConstraints NVARCHAR(max) = ''
    SELECT @DropConstraints += 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.'
                            +  QUOTENAME(OBJECT_NAME(parent_object_id)) + ' ' + 'DROP CONSTRAINT' + QUOTENAME(name)
    FROM sys.foreign_keys
    EXECUTE sp_executesql @DropConstraints;
end;
---
exec hui;

drop table if exists users;
drop table if exists city;
drop table if exists clinic;
drop table if exists bill;
drop table if exists clinic_users;
drop table if exists user_bills;
drop table if exists all_default_data_types;

drop procedure if exists digit_magic;
drop procedure if exists bill_zero;
drop procedure if exists discount_bill;
drop procedure if exists test_types_procedure;
---

create table users (
    id bigint not null IDENTITY(1, 1),
    name varchar(100) not null,
    info varchar(200),
    constraint users_pk PRIMARY KEY(id)
);
---
create table city (
    id bigint not null PRIMARY KEY,
    name varchar(50) not null
);
---
create table clinic (
    id bigint not null PRIMARY KEY,
    name varchar(100) not null,
    city_id bigint not null,
    constraint clinic_fk2       foreign key (city_id)   references city(id)
);
---
create table bill (
    id bigint not null PRIMARY KEY,
    amount decimal(15,2) not null,
    discount decimal(15,2),
    date datetimeoffset not null
);
---
create table clinic_users (
    clinic_id bigint not null,
    user_id bigint not null,
    constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id),
    constraint clinic_users_fk1 foreign key (user_id)   references users(id)
);
---
create table user_bills (
    user_id bigint not null,
    bill_id bigint not null,
    constraint user_bills_fk0   foreign key (user_id)   references users(id),
    constraint user_bills_fk1   foreign key (bill_id)   references bill(id)
);
---
create table all_default_data_types(
    byte_type tinyint not null,
    byte_type_null tinyint
);
---

insert into users(name, info) values ('Joe', null);
insert into users(name, info) values ('Donald', 'Do not disturb');

insert into city(id, name) values (1, 'London');
insert into city(id, name) values (2, 'Paris');

insert into clinic(id, name, city_id) values (1, 'Paris Neurology Hospital', 2);
insert into clinic(id, name, city_id) values (2, 'London Heart Hospital', 1);
insert into clinic(id, name, city_id) values (3, 'Diagnostic center', 1);

insert into bill(id, amount, discount, date) values(1, 2000.55, null, '2024-07-01 12:00:00Z');
insert into bill(id, amount, discount, date) values(2, 1000.20, null, '2024-07-01 16:00:00Z');
insert into bill(id, amount, discount, date) values(3, 5000, null,    '2024-08-01 15:00:00Z');
insert into bill(id, amount, discount, date) values(4, 7000.77, null, '2024-08-01 15:00:00Z');
insert into bill(id, amount, discount, date) values(5, 500.10, null,  '2024-09-01 15:00:00Z');

insert into clinic_users values(1, 2);
insert into clinic_users values(2, 1);

insert into user_bills values(1, 1);
insert into user_bills values(1, 2);
insert into user_bills values(2, 3);
insert into user_bills values(2, 4);
insert into user_bills values(2, 5);
---

create procedure digit_magic @x int, @y int output, @z int output
    as begin
        set @y = @y + @x;
        set @z = @y + @x;
    end;
---
create procedure bill_zero
    as begin
        update bill set amount = 0;
    end;
---
create procedure discount_bill @datedisc datetime
    as begin
        update bill set discount = amount * 0.1 where date = @datedisc;
    end;
---
create procedure test_types_procedure
    @big_decimal_type      decimal(15, 3)       ,
    @big_decimal_type_o    decimal(15, 3) output,
    @big_decimal_type_null decimal(15, 3) output,

    @boolean_type      bit       ,
    @boolean_type_o    bit output,
    @boolean_type_null bit output,

   -- @bytearray_type bytea            ,
   -- @bytearray_type bytea      output,
   -- @bytearray_type_null bytea output,

    @date_type      date       ,
    @date_type_o    date output,
    @date_type_null date output,

    @integer_type      integer       ,
    @integer_type_o    integer output,
    @integer_type_null integer output,

    @long_type      bigint       ,
    @long_type_o    bigint output,
    @long_type_null bigint output,

    @string_type      varchar(200)       ,
    @string_type_o    varchar(200) output,
    @string_type_null varchar(200) output,

    @short_type      smallint       ,
    @short_type_o    smallint output,
    @short_type_null smallint output,

    @byte_type      tinyint       ,
    @byte_type_o    tinyint output,
    @byte_type_null tinyint output,

    @time_type      time       ,
    @time_type_o    time output,
    @time_type_null time output,

    @timestamp_type      datetime       ,
    @timestamp_type_o    datetime output,
    @timestamp_type_null datetime output
   as begin

       set @big_decimal_type_o = @big_decimal_type;
       set @boolean_type_o     = @boolean_type    ;
       set @date_type_o        = @date_type       ;
       set @integer_type_o     = @integer_type    ;
       set @long_type_o        = @long_type       ;
       set @string_type_o      = @string_type     ;
       set @short_type_o       = @short_type      ;
       set @byte_type_o        = @byte_type       ;
       set @time_type_o        = @time_type       ;
       set @timestamp_type_o   = @timestamp_type  ;
   end;
