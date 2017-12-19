-- {^} - delimiter for transaction related sql batch
-- {;} - delimiter for sql queries
drop table if exists users;
drop table if exists time_entry;
drop table if exists months^
create table months (
	id int primary key,
	name varchar(50)
);
create table users (
	id uuid default random_uuid() not null primary key,
	login varchar(50) unique not null
)^
create table time_entry (
	id uuid default random_uuid() not null primary key,
	user_id uuid not null references users(id),
	date_ date not null,
	time_in_minutes integer default 0 not null
)^
insert into months (name, id) values
	('january', 1),
	('february', 2),
	('march', 3),
	('april', 4),
	('may', 5),
	('june', 6),
	('july', 7),
	('august', 8),
	('september', 9),
	('october', 10),
	('november', 11),
	('december', 12)
;
insert into users (login) values
	('tedd'),
	('fred'),
	('dead')
;
insert into time_entry (user_id, date_, time_in_minutes)
	select distinct u.id as user_id,
									(year(curdate()) || '-' || to_char(month.x, 'fm00')||'-'|| to_char(day.x, 'fm00'))::date as date_,
									(random() * 8 + 2)::int time_in_minutes
	from users u,
				system_range(1, random(30) * 28 + 2, 2) as day,
				system_range(1, random(30) * 11 + 1) as month
;


