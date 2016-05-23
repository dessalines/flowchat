--liquibase formatted sql

--changeset tyler:1

CREATE TABLE user_ (
    id bigserial primary key,
    name varchar(255) NOT NULL,
    created timestamp default current_timestamp
);

CREATE TABLE comment (
    id bigserial primary key,
    user_id bigserial NOT NULL,
    text_ text NOT NULL,
    created timestamp default current_timestamp,
    CONSTRAINT fk1_user FOREIGN KEY (user_id)
        REFERENCES user_ (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE comment_tree (
    id bigserial primary key,
    parent_id bigserial NOT NULL,
    child_id bigserial NOT NULL,
    path_length integer NOT NULL,
    created timestamp default current_timestamp,
    CONSTRAINT fk1_parent_id FOREIGN KEY (parent_id)
        REFERENCES comment (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk2_child_id FOREIGN KEY (child_id)
        REFERENCES comment (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO user_ (id, name)
    VALUES (1,'user_1'),(2,'user_2'),(3,'user_3');

INSERT INTO comment (id, text_, user_id)
	VALUES (1,'Level 1',1),(2,'Level 2',2),(3,'Level 2',3),(4,'Level 3',2),(5,'Level 4',1),
	(6,'Level 1',2);

INSERT INTO comment_tree (parent_id, child_id, path_length)
	VALUES 	(1,1,0) , (1,2,1) , (1,3,1) , (1,4,2) , (1,5,3)	,
			(2,2,0) ,
			(3,3,0) ,                     (3,4,1) ,	(3,5,2)	,
			(4,4,0) ,					  			(4,5,1)	,
			(5,5,0)	,
			(6,6,0)
			;


