create type enum_user_sex as enum('MALE', 'FEMALE');

create table users (
    id bigint not null,
    name varchar(100) not null,
    info varchar(200),
    sex enum_user_sex,
    constraint users_pk primary key (id)
);
create table clinic (
    id bigint primary key,
    name varchar(100) not null,
    city_id bigint not null
);
create table city (
    id bigint primary key,
    name varchar(50) not null
);
create table bill (
    id bigint primary key,
    amount decimal(15,2) not null,
    discount decimal(15,2),
    date timestamp with time zone not null
);
create table clinic_users (
    clinic_id bigint not null,
    user_id bigint not null
);
create table user_bills (
    user_id bigint not null,
    bill_id bigint not null
);
create table for_insert (
    id bigint not null
);

alter table clinic       add constraint clinic_fk2       foreign key (city_id)   references city(id);
alter table clinic_users add constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id);
alter table clinic_users add constraint clinic_users_fk1 foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk0   foreign key (user_id)   references users(id);
alter table user_bills   add constraint user_bills_fk1   foreign key (bill_id)   references bill(id);

create procedure digit_magic(in x int, inout y int, out z int)
language plpgsql AS $$ begin
    y = y + x;
    z = y + x;
end; $$;

create procedure bill_zero()
language plpgsql AS $$ begin
    update bill set amount = 0;
end; $$;

create procedure discount_bill(in datedisc timestamp)
language plpgsql AS $$ begin
    update bill set discount = amount * 0.1 where date = datedisc;
end; $$;

create table grouped_dto(
    gg varchar(50) not null,
    flo real not null
);

create procedure p_bool(in a bool, out b bool)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_smallint(in a smallint, out b smallint)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_int(in a int, out b int)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_bigint(in a bigint, out b bigint)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_real(in a real, out b real)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_float(in a float, out b float)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_decimal(in a decimal(15, 3), out b decimal(15, 3))
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_varchar(in a varchar, out b varchar)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_date(in a date, out b date)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_time(in a time, out b time)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_timestamp(in a timestamp, out b timestamp)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_timestamptz(in a timestamptz, out b timestamptz)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_timetz(in a timetz, out b timetz)
language plpgsql AS $$ begin b = a; end; $$;

create procedure p_uuid(in a uuid, out b uuid)
language plpgsql AS $$ begin b = a; end; $$;