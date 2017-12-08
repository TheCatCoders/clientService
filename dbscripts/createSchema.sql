DROP TABLE IF EXISTS USERS;
DROP SEQUENCE IF EXISTS users_seq;

CREATE SEQUENCE users_seq
 START WITH     1
 INCREMENT BY   1;

CREATE TABLE USERS(
   ID   INT              NOT NULL,
   NAME VARCHAR (20)     NOT NULL,
   GENDER VARCHAR (1)    NOT NULL,
   BIRTHDATE  DATE,
   EMAILADR  VARCHAR(50) NOT NULL,
   PASSWORD  VARCHAR(20) NOT NULL,
   CREATE_DATE DATE,
   PRIMARY KEY (ID)
);
