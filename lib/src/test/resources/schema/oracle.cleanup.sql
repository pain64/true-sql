

begin

execute immediate 'delete from user_bills';
execute immediate 'delete from clinic_users';
execute immediate 'delete from bill';
execute immediate 'delete from clinic';
execute immediate 'delete from city';
execute immediate 'delete from users';
execute immediate 'delete from all_default_data_types';

execute immediate 'ALTER TABLE users MODIFY(ID GENERATED AS IDENTITY (START WITH 1))';

execute immediate 'insert into users(name, info) values(''Joe'', null)';
execute immediate 'insert into users(name, info) values(''Donald'', ''Do not disturb'')';

execute immediate 'insert into city(id, name) values(1, ''London'')';
execute immediate 'insert into city(id, name) values(2, ''Paris'')';

execute immediate 'insert into clinic(id, name, city_id) values(1, ''Paris Neurology Hospital'', 2)';
execute immediate 'insert into clinic(id, name, city_id) values(2, ''London Heart Hospital'', 1)';
execute immediate 'insert into clinic(id, name, city_id) values(3, ''Diagnostic center'', 1)';

execute immediate 'insert into bill(id, amount, discount, "date") values(1, 2000.55, null, timestamp ''2024-07-01 12:00:00 utc'')';
execute immediate 'insert into bill(id, amount, discount, "date") values(2, 1000.20, null, timestamp ''2024-07-01 16:00:00 utc'')';
execute immediate 'insert into bill(id, amount, discount, "date") values(3, 5000, null,    timestamp ''2024-08-01 15:00:00 utc'')';
execute immediate 'insert into bill(id, amount, discount, "date") values(4, 7000.77, null, timestamp ''2024-08-01 15:00:00 utc'')';
execute immediate 'insert into bill(id, amount, discount, "date") values(5, 500.10, null,  timestamp ''2024-09-01 15:00:00 utc'')';

execute immediate 'insert into clinic_users values(1, 2)';
execute immediate 'insert into clinic_users values(2, 1)';

execute immediate 'insert into user_bills values(1, 1)';
execute immediate 'insert into user_bills values(1, 2)';
execute immediate 'insert into user_bills values(2, 3)';
execute immediate 'insert into user_bills values(2, 4)';
execute immediate 'insert into user_bills values(2, 5)';

end;