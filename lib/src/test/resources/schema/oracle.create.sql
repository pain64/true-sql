begin
   execute immediate '
        create table users (
              id number(19) not null,
              name varchar(100) not null,
              info varchar(200),
              -- sex enum_user_sex,
              constraint users_pk primary key (id)
          )';
   execute immediate '
       create table clinic (
              id number(19) primary key,
              name varchar(100) not null,
              city_id number(19) not null
          )';

   execute immediate '
          create table city (
                 id number(19) primary key,
                 name varchar(50) not null
             )';
   execute immediate '
       create table bill (
              id number(19) primary key,
              amount number(15,2) not null,
              discount number(15,2),
              "date" timestamp with time zone not null
          )';
   execute immediate '
       create table clinic_users (
              clinic_id number(19) not null,
              user_id number(19) not null
          )';
   execute immediate '
       create table user_bills (
              user_id number(19) not null,
              bill_id number(19) not null
          )';
   execute immediate '
      create table for_insert (
              id number(19, 0) not null
          )';

   execute immediate 'alter table clinic       add constraint clinic_fk2       foreign key (city_id)   references city(id)';
   execute immediate 'alter table clinic_users add constraint clinic_users_fk0 foreign key (clinic_id) references clinic(id)';
   execute immediate 'alter table clinic_users add constraint clinic_users_fk1 foreign key (user_id)   references users(id)';
   execute immediate 'alter table user_bills   add constraint user_bills_fk0   foreign key (user_id)   references users(id)';
   execute immediate 'alter table user_bills   add constraint user_bills_fk1   foreign key (bill_id)   references bill(id)';
   execute immediate '
       create procedure digit_magic(
              x IN int, y IN OUT int, z OUT int
          ) is begin
              y := y + x;
              z := y + x;
          end;';
   execute immediate '
          create procedure bill_zero
          is begin
              update bill set amount = 0;
          end;';

   execute immediate '
          create procedure discount_bill(datedisc IN timestamp)
          is begin
              update bill set discount = amount * 0.1 where "date" = datedisc;
          end;';

   execute immediate '
          create procedure p_boolean(x IN boolean, y OUT boolean)
          is begin y := x; end;';
   execute immediate '
          create procedure p_number(x IN number, y OUT number)
          is begin y := x; end;';
   execute immediate '
          create procedure p_int(x IN int, y OUT int)
          is begin y := x; end;';
   execute immediate '
          create procedure p_float(x IN binary_float, y OUT binary_float)
          is begin y := x; end;';
   execute immediate '
          create procedure p_double(x IN binary_double, y OUT binary_double)
          is begin y := x; end;';
   execute immediate '
          create procedure p_varchar(x IN varchar, y OUT varchar)
          is begin y := x; end;';
   execute immediate '
          create procedure p_date(x IN date, y OUT date)
          is begin y := x; end;';
   execute immediate '
          create procedure p_timestamp(x IN timestamp, y OUT timestamp)
          is begin y := x; end;';
   execute immediate '
          create procedure p_timestamptz(x IN timestamp with time zone, y OUT timestamp with time zone)
          is begin y := x; end;';
   execute immediate '
          create procedure p_raw(x IN raw, y OUT raw)
          is begin y := x; end;';
end;