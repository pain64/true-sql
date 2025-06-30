truncate users cascade;
truncate user_bills cascade;
truncate clinic_users cascade;
truncate bill cascade;
truncate clinic cascade;
truncate city cascade;
truncate grouped_dto cascade;

insert into users values(1, 'Joe', null, 'MALE');
insert into users values(2, 'Donald', 'Do not disturb', 'MALE');

insert into city values(1, 'London');
insert into city values(2, 'Paris');

insert into clinic values(1, 'Paris Neurology Hospital', 2);
insert into clinic values(2, 'London Heart Hospital', 1);
insert into clinic values(3, 'Diagnostic center', 1);

insert into bill values(1, 2000.55, null, '2024-07-01T12:00:00Z'::timestamptz);
insert into bill values(2, 1000.20, null, '2024-07-01T16:00:00Z'::timestamptz);
insert into bill values(3, 5000   , null, '2024-08-01T15:00:00Z'::timestamptz);
insert into bill values(4, 7000.77, null, '2024-08-01T15:00:00Z'::timestamptz);
insert into bill values(5, 500.10 , null, '2024-09-01T15:00:00Z'::timestamptz);

insert into clinic_users values(1, 2);
insert into clinic_users values(2, 1);

insert into user_bills values(1, 1);
insert into user_bills values(1, 2);
insert into user_bills values(2, 3);
insert into user_bills values(2, 4);
insert into user_bills values(2, 5);

