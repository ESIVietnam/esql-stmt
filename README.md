# Embedded SQL Statement (ESQL-Stmt)

## What is this project

The project is to rewrite from scratch of ESI-Apps, restructuring with new updates. **ESI-Apps** is a Java framework and engine that process SQL Statement definition inside a Java Project, it can create REST API Service easily, detaching the SQL works of Java Developer to Database Developer, **avoid writing SQL code in Java code** (but *not like the way ORM does*), by **letting Database Developers do their works with SQL only code**. A backend business application project can be written by purely SQL, if not much customization logic and no integration.

Along with ESI-Apps, ESI-Report is to create reports by SQL works only, and some of XML/XSLT code for customization. All projects based on an important part of ESI-Apps: SQLStmt (named **sqlstmt3** library).

ESI-Apps version is now 3.5/3.6 and it reached to the barrier of development: it complicates to integrate SQLStmt with other. It is now ESQL-Stmt is depart from ESI-Apps, and It should start version 4.0 and onward. We will copy some of proven code from 3.x version but most of them has to be rewritten. And I decide that the new version would be open sourced. I may help or improve projects that use SQL or SQL query builder style (but no ORM anyway).

In the future, ESI-Apps and ESI-Report will be upgrade to use ESQL-Stmt and it will still be closed source.

## Embedded SQL Statement in Java

TBD

## Why ESQL-Stmt and Why not ORM

In spite of ORM Frameworks are so popular in our Java world. That programming way has some pros and cons in interfacing with SQL Database. I don't want to create a war between ORM and anti-ORM, many people did that but I see that ORM is not suitable for "heavy data query and complex calculation". ORM does not utilize the power of modern RDBMS, it tries to be a tool or a style for every developers who use a No-SQL programming language to work with SQL database, to read and write data but only that. They facing with the problems how Java works with measurement, calculation, comparing and doing with expression only - not prefer to work with class instances. 

What I learned from Domain Driven Design, business rule and calculation requirement is very important of application.  I think that SQL is better for "calculation and manipulation data" without writing Java code for the purpose. In last ten years, we have seen RDBMS walking through the NoSQL emerging and it is still competitive in the enterprises. SQL (and it stored procedure language) would still be the best tool to manipulate and query structured-data, even there are dozen SQL-like languages in many NoSQL systems. SQL is powerful enough to work with billion rows of data in one system, that very rare to the enterprise application hits that limitation.

The advantage of SQL is: it is easier and take less time (in coding work) to resolve business requirement - especially in reporting and transaction. A lot of features of SQL and many new functions, many calculation utils are added to help programmers reduce their time for the requirements.
