create table user_data (
	id INT,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	email VARCHAR(50),
	admin VARCHAR(50),
	gender VARCHAR(50),
	country VARCHAR(50),
	password VARCHAR(50)
);

create table user_post (
	id INT,
	user_id INT,
	post TEXT,
	published DATETIME
);