DROP TABLE IF EXISTS config;
===
CREATE TABLE config (
    name TEXT, value TEXT
);
===
DROP TABLE IF EXISTS institutions;
===
CREATE TABLE institutions (
	name TEXT, id TEXT
);
===
DROP TABLE IF EXISTS departments;
===
CREATE TABLE departments (
	name TEXT, id TEXT, webpage TEXT, institution_id TEXT
);
===
DROP TABLE IF EXISTS people;
===
CREATE TABLE people (
	email TEXT, title TEXT, gender TEXT, first_name TEXT, last_name TEXT, 
        id TEXT, institution_id TEXT, department_id TEXT
);
