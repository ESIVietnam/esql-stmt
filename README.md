# Embedded SQL Statement (ESQL-Stmt)

The project is to rewrite from scratch a part of ESI-Apps. **ESI-Apps** is a Java framework and engine that process SQL Statement definition inside a Java Project, it allow developers to create REST API Service easily, and detaching the SQL works of Java Developer to Database Developer, **avoid writing SQL code in Java code** (but *not like the way ORM does*), split works by **letting Database Developers do their works with SQL only code** and **Java developers do UI/assembled works**. A backend business application project can be written by purely SQL, if not much customization logic and no integration.

Along with ESI-Apps, ESI-Report is to create reports by SQL works only, and some of XML/XSLT code for definition. All projects based on an important part of ESI-Apps: SQLStmt (named **sqlstmt3** library).

ESI-Apps version is now 3.5/3.6 and it reached to the barrier of development: it complicates to integrate with SQLStmt, and it should focus to data parsing, function organization, REST path routing... It is now time ESQL-Stmt is depart from ESI-Apps, and it become this project and starting with **version 4.0** and onward. We will copy some of proven code from 3.x version but most of them has to be rewritten. I decide that the new version of module would be open sourced, too. It may help us improving the project by the community.

Thought, ESI-Apps and ESI-Report will be upgraded to use the ESQL-Stmt new version and it will still be closed source.

## Embedded SQL Statement in Java

The Embedded SQL Statement (ESQL-Stmt for short) is a framework and engine to write SQL statements as actions and embedded them into a Java code to do business. SQL statements are for doing business logic, through data queries and data manipulation statement (DML) and call store procedures. The framework provides a set special languate (DSL) to build functions, helping in parsing input data (JSON, XML, text and form-encoded), writing result (JSON, XML, CSV, text and binary data), security and access control, session and thread management and external interfacing (via REST or SOAP, or other APIs). For more detail on philosophy, please see [[ESQL-Stmt design philosophy.md]]

## Getting started

TBD.

## Why ESQL-Stmt in favor of ORM

In spite of ORM Frameworks are so popular in our Java world. That ORM programming way has some advantage and disadvantage in interfacing with SQL Database. I don't want to create a war between ORM and anti-ORM, but I see that ORM is not suitable for "heavy data query and complex calculation". ORM does not utilize the power of modern RDBMS, it tries to be a tool or a style for every developers who use a No-SQL programming language to work with SQL database, to read and write data but only that. They facing with the problems how Java works with business logic: it is not efficient in data-only, in measurement, calculation, comparing and doing with expression only - not prefer to work with class instances. Treat data tuples or table row as objects are not suitable for often change or dynamic schema (aka schema-less entity).

I think that SQL is for "calculation and manipulation data" better than writing Java code for the purpose. Firstly we see into history in last ten years, we have seen RDBMS still competitive/upgrade in the enterprises, in the cloud, walking through the NoSQL emerging. SQL (and it Stored Procedure language, eg PL/SQL, Pg/PL-SQL) would still be the best tool to manipulate and query structured-data, and doing business logic. Even there are dozen SQL-like languages or not in many NoSQL systems, you often does choose a "non-RDBMS" in ORM. RDBMS are very popular, and SQL is powerful enough to work with billion rows of data in one system, that very rare to the enterprise application hits that limitation.

Another advantage of SQL is: it is easier and take less time (in coding work) to resolve business requirement - especially in reporting and transaction. A lot of features of SQL and many new functions, many calculation utils are added to help programmers reduce their time for the requirements.
