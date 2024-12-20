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

drop sequence if exists users_id_seq;

drop procedure if exists digit_magic;
drop procedure if exists bill_zero;
drop procedure if exists discount_bill;
drop procedure if exists test_types_procedure;
drop procedure if exists test_types_procedure_extended;
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
create table all_default_data_types(
    byte_type tinyint not null,
    byte_type_null tinyint,
    bytearray_type varbinary(200) not null,
    bytearray_type_null varbinary(200),
    datetime_offset datetimeoffset not null,
    datetime_offset_null datetimeoffset
);
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
    @timestamp_type_null datetime output,

    @float_type          real       ,
    @float_type_o        real output,
    @float_type_null     real output,

    @double_type         float       ,
    @double_type_o       float output,
    @double_type_null    float output
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
       set @float_type_o       = @float_type      ;
       set @double_type_o      = @double_type     ;
   end;
---
create procedure test_types_procedure_extended
    @bytearray_type           varbinary(200)       ,
    @bytearray_type_o         varbinary(200) output,
    @bytearray_type_null      varbinary(200) output,
    @datetimeoffset_type      datetimeoffset       ,
    @datetimeoffset_type_o    datetimeoffset output,
    @datetimeoffset_type_null datetimeoffset output
   as begin
       set @bytearray_type_o      = @bytearray_type;
       set @datetimeoffset_type_o = @datetimeoffset_type;
   end;