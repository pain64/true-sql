delete from user_bills;
delete from clinic_users;
delete from bill;
delete from clinic;
delete from city;
delete from users;

alter table users alter column id restart with 1;

insert into users(id, name, info) values (1, 'Joe', null);
insert into users(id, name, info) values (2, 'Donald', 'Do not disturb');

insert into city(id, name) values (1, 'London');
insert into city(id, name) values (2, 'Paris');

insert into clinic(id, name, city_id) values (1, 'Paris Neurology Hospital', 2);
insert into clinic(id, name, city_id) values (2, 'London Heart Hospital', 1);
insert into clinic(id, name, city_id) values (3, 'Diagnostic center', 1);

insert into bill(id, amount, discount, date) values(1, 2000.55, null, timestamp '2024-07-01 12:00:00+0:00');
insert into bill(id, amount, discount, date) values(2, 1000.20, null, timestamp '2024-07-01 16:00:00+0:00');
insert into bill(id, amount, discount, date) values(3, 5000, null,    timestamp '2024-08-01 15:00:00+0:00');
insert into bill(id, amount, discount, date) values(4, 7000.77, null, timestamp '2024-08-01 15:00:00+0:00');
insert into bill(id, amount, discount, date) values(5, 500.10, null,  timestamp '2024-09-01 15:00:00+0:00');

insert into clinic_users values(1, 2);
insert into clinic_users values(2, 1);

insert into user_bills values(1, 1);
insert into user_bills values(1, 2);
insert into user_bills values(2, 3);
insert into user_bills values(2, 4);
insert into user_bills values(2, 5);
