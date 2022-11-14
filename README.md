# CASH - Corpus, Annotation and SearcH

A corpora and annotations management server.

CASH provides a set of services for the management of documents, creating a file-system-like abstraction, organising documents into folders. Documents can be annotated and searched, and as such, the main purpose of CASH in the overall architecture is that of dealing with how documents and annotations are represented and made persistent, and how such documents can be retrieved. 

## GETTING STARTED

Create and initialise the cash db:

```
cd model
mysql> create database cash;
Query OK, 1 row affected (0,01 sec)

mysql> source schema.sql;

mysql> CREATE USER 'cash'@'localhost' IDENTIFIED BY 'cash';
Query OK, 0 rows affected (0,03 sec)

mysql> GRANT ALL PRIVILEGES ON cash.* TO 'cash'@'localhost';
Query OK, 0 rows affected (0,00 sec)
```

Clone the repo and build the jar file:

```
git clone git@github.com:valeq/CASH-server.git
cd CASH-server
./buildJAR
```

Run it:

```
java -jar target/cash-x.x.x-SNAPSHOT.jar
```

Swagger can be found [here](http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/). 

If you change the db name and user credentials from cash/cash/cash to something else, you need to set the following environment variables before running it:

```
export MYSQL_URL=”jdbc:mysql://localhost:3306/cash?connectTimeout=0&socketTimeout=0&autoReconnect=true”
export MYSQL_USER=cash
export MYSQL_PASSWORD=cash
```
