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

drop sequence if exists users_id_seq;

drop procedure if exists digit_magic;
drop procedure if exists bill_zero;
drop procedure if exists discount_bill;
drop procedure if exists p_bit;
drop procedure if exists p_tinyint;
drop procedure if exists p_smallint;
drop procedure if exists p_int;
drop procedure if exists p_bigint;
drop procedure if exists p_real;
drop procedure if exists p_float;
drop procedure if exists p_decimal;
drop procedure if exists p_varchar;
drop procedure if exists p_date;
drop procedure if exists p_time;
drop procedure if exists p_datetime;
drop procedure if exists p_datetimeoffset;
drop procedure if exists p_varbinary;
drop procedure if exists p_uniqueidentifier;
---

CREATE SEQUENCE users_id_seq
AS bigint START WITH 1 INCREMENT BY 1;

create table users (
    id bigint not null DEFAULT NEXT VALUE FOR users_id_seq,
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
create procedure p_bit @x bit, @y bit output
    as begin set @y = @x; end;
---
create procedure p_tinyint @x tinyint, @y tinyint output
    as begin set @y = @x; end;
---
create procedure p_smallint @x smallint, @y smallint output
    as begin set @y = @x; end;
---
create procedure p_int @x int, @y int output
    as begin set @y = @x; end;
---
create procedure p_bigint @x bigint, @y bigint output
    as begin set @y = @x; end;
---
create procedure p_real @x real, @y real output
    as begin set @y = @x; end;
---
create procedure p_float @x float, @y float output
    as begin set @y = @x; end;
---
create procedure p_decimal @x decimal(15, 3), @y decimal(15, 3) output
    as begin set @y = @x; end;
---
create procedure p_varchar @x varchar, @y varchar output
    as begin set @y = @x; end;
---
create procedure p_date @x date, @y date output
    as begin set @y = @x; end;
---
create procedure p_time @x time, @y time output
    as begin set @y = @x; end;
---
create procedure p_datetime @x datetime, @y datetime output
    as begin set @y = @x; end;
---
create procedure p_datetimeoffset @x datetimeoffset, @y datetimeoffset output
    as begin set @y = @x; end;
---
create procedure p_varbinary @x varbinary(32), @y varbinary(32) output
    as begin set @y = @x; end;
---
create procedure p_uniqueidentifier @x uniqueidentifier, @y uniqueidentifier output
    as begin set @y = @x; end;