-- {^} - delimiter for transaction related sql batch
-- {;} - delimiter for sql queries
drop table if exists xlsx_integration_users;
drop table if exists xlsx_integration_income_entry;

create table xlsx_integration_users (
  id uuid default random_uuid() not null primary key,
  name_order int not null,
  name varchar(50) unique not null
)^

create table xlsx_integration_income_entry (
  id uuid default random_uuid() not null primary key,
  user_id uuid not null references xlsx_integration_users(id),
  date_ varchar not null,
  income integer default 0 not null
)^

insert into xlsx_integration_users (name, name_order) values
  ('Stanley', 1),
  ('Kyle', 2),
  ('Eric', 3),
  ('Kenney', 4),
  ('Craig', 5)
;

insert into xlsx_integration_income_entry (user_id, date_, income)
  select distinct u.id as user_id,
                  ('2014/04/' || to_char(day.x, 'fm00')) as date_,
                  (day.x * u.number)::int as income
  from (SELECT id, rownum() as number from xlsx_integration_users ORDER BY name_order) as u,
        SYSTEM_RANGE(1, 10) as day
;


