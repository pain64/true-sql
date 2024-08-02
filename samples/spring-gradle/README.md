### How to run this demo
You need PostgreSQL to be accessible at `localhost:5432`. Also you need user with name `sa` and password `1234`
user and database creation script:
```shell
sudo su postgres
psql
```
```sql
create database truesqldb;
create user sa with password '1234';
grant all privileges on database truesqldb to sa;
```

schema and test data:

`psql -h localhost -U sa -d truesqldb`
```sql
create table users (
id bigserial not null,
name varchar(100) not null,
info varchar(200),
constraint users_pk primary key (id)
);

insert into users(name, info) values('Joe', null);
insert into users(name, info) values('Donald', 'Do not disturb');
```