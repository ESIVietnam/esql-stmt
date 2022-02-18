# ESQL-Stmt Overview

ESQL-Stmt is stand for Embedding SQL Statement (or Employing SQL Statement) framework that using Structure Query Language (SQL) in main way for building your business application. In short, The ESQL-Stmt is a SQL Template Engine, the template is defined by an XML DSL designed to utilize the power of the modern SQL or SQL-Like database.

## ESQL-Stmt for Java first

The ESQL-Stmt project is a Java framework/engine to write SQL statements as actions and embedded them into a Java code/Java Application to create an application that SQL is the main programming language for business logic. The Java code is only for the "glue" function, not for business logic. SQL statements are used for doing business logic, by using data queries and data manipulation statement (DML) and store procedures (for more complex logic) for everything related to the business-problems to solve. The ESQL-Stmt framework (as a wrapper or an engine) provides a simple way to define how to map SQL Statement to interface for external world, or even provides a set special languate (DSL) to create "glue" link between SQL Statements in more complex cases. Java code of the engine is for non-functional features, for example, parsing input data (JSON, XML, text and form-encoded), executing the SQL Statements over the JDBC Connection, writing results (JSON, XML, CSV, text and binary data), processing template to write out (XSLT or Mustache), taking security check and access control, managing JDBC connections and sessions and threads. Java code is the gate of external interfacing stuff (via REST or SOAP, or other APIs) but delegating all business functions to SQL Statements. For more detail on philosophy, please see [[ESQL-Stmt design philosophy.md]].

## Getting started

TBD.

## Why SQL and why ESQL-Stmt is in favor of non-ORM

In spite of ORM Frameworks are so popular in our Java world. That ORM programming way has some advantage and disadvantage in interfacing with SQL Database. I don't want to feed a war between ORM and anti-ORM, but I see that ORM is not suitable for "heavily queries and complex calculation". ORM does not utilize the power of modern RDBMS, instead it tries to be a tool or a style for every developers who use an abstract-SQL programming language to work with SQL-based database, to read and write data only, or to find something in database by simple filter. They facing with the problems how Java works with business logic: it is not efficient for the Data Engineers, in measurement, calculation, comparing and doing with complex expression, analytic queries - who does not prefer to work with classes or objects. Treating data tuples or table rows as series objects of DTO classes are not suitable for many applications, and it got problems when often changing strutures and worthless for dynamic schema/schema-less entities.

I think that SQL is for "calculation and manipulation data" better than writing Java code for the purpose. Firstly we see into history in last ten years, we have seen RDBMS still competitive/upgrade in the enterprises, in the cloud, walking through the NoSQL emerging. SQL (and it Stored Procedure language, eg PL/SQL, Pg/PL-SQL) would still be the best tool to manipulate and query structured-data, and doing business logic. Even there are dozen SQL-like languages or not in many NoSQL systems, you often does choose a "non-RDBMS" in ORM. RDBMS are very popular, and SQL is powerful enough to work with billion rows of data in one system, that very rare to the enterprise application hits that limitation.

Another advantage of SQL is: it is easier and take less time (in coding work) to resolve business requirement - especially in reporting and transaction. A lot of features of SQL and many new functions, many calculation utils are added to help programmers reduce their time for the requirements

The ESQL-Stmt still has DTOs for in-memory data processing and serializing that carrying data from/to RDBMS. But they is like "value objects", they are short in life-time and they are implemented as immutable objects in Java and they are designed to be compatible with SQL's data types, data rowes, a record or a result set of the query, or suitable to store in/out parameter. Additionally, the "value objects" support LOBs (CLOBs, BLOBs), array type and the strutural data types like XML, JSON (this is new data type supported by main-stream RDBMS).

## The history

The project is to rewrite from scratch the ESI-DBTools, a part of ESI-Apps. **ESI-Apps** is a Java framework and engine that process SQL Statement definition inside a Java Project, it allows developers to create a REST API Service easily, and detaching the SQL works of Java Developers to Database Developers, **avoid writing SQL code in Java code** (but *not like the way ORM does*), split works by **letting the Database Developers do their works with SQL only code** and **the Java developers do UI/assembled works**. A backend business application project can be written by purely SQL, if not much customization logic and no integration.

ESI-Apps version is now 3.5/3.6 and it reached to the barrier of development: it complicates to integrate with SQLStmt, and it should focus to data parsing, function organization, REST path routing... It is now time ESQL-Stmt departs from ESI-Apps, and it become this project and starting with **version 4.0** and onward. We will copy some of proven code from 3.x version but most of them has to be rewritten. I decide that the new version of module would be open sourced, too. It may help us improving the project by the community.

Thought, ESI-Apps and ESI-Report will be upgraded to use the ESQL-Stmt new version and it will still be closed source.

.
