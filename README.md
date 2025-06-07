Overview of ESQL-Stmt
=====================

ESQL-Stmt stands for Embedded SQL Statement (or Employing SQL Statement) framework that primarily utilizes Structured Query Language (SQL) to build business applications. In essence, ESQL-Stmt serves as a SQL Template Engine, with templates defined by an XML Domain-Specific Language (DSL) designed to harness the capabilities of modern SQL or SQL-like databases.

## ESQL-Stmt for Java

The ESQL-Stmt project is a Java framework/engine that allows developers to write SQL statements as actions and embed them within Java applications, enabling SQL to serve as the primary programming language for business logic. Java code functions mainly as "glue," while SQL statements handle the business logic through data queries, data manipulation (DML), and stored procedures for more complex operations. The ESQL-Stmt framework acts as a wrapper or engine, providing a straightforward way to map SQL statements to external interfaces. It also offers a special DSL to create "glue" links between SQL statements in more intricate scenarios.

The Java component of the engine focuses on non-functional features, such as parsing input data (JSON, XML, text, and form-encoded), executing SQL statements over JDBC connections, writing results (JSON, XML, CSV, text, and binary data), processing templates (XSLT or Mustache), handling security checks and access control, and managing JDBC connections, sessions, and threads. While Java code serves as the gateway for external interfaces (via REST, SOAP, or other APIs), all business functions are delegated to SQL statements. For more details on the underlying philosophy, please refer to the [[ESQL-Stmt Design Philosophy.md]].

## Getting Started

TBD.

## Why SQL and Why ESQL-Stmt Prefers Non-ORM

Despite the popularity of ORM frameworks in the Java ecosystem, they come with advantages and disadvantages when interfacing with SQL databases. Rather than framing this as a conflict between ORM and non-ORM approaches, I contend that ORM may not be ideal for "heavy queries and complex calculations." ORMs often fail to leverage the full potential of modern RDBMS, as they aim to provide an abstract-SQL programming style for developers who typically only need to read and write data or perform simple queries. This approach can be inefficient for data engineers focused on measurement, calculation, comparisons, and complex analytical queries, who may not prefer to work with classes or objects. Representing data tuples or table rows as object instances of DTO classes is often unsuitable, especially in applications with frequently changing structures or dynamic schemas.

I believe SQL is superior for "data calculation and manipulation" compared to Java code. Over the past decade, RDBMS have remained competitive and continue to evolve, even with the rise of NoSQL systems. SQL (and its stored procedure languages, such as PL/SQL or Pg/PL-SQL) remains the best tool for manipulating and querying structured data and implementing business logic. Even with numerous SQL-like languages in NoSQL systems, developers often still choose RDBMS over ORM frameworks. RDBMS are widely adopted, and SQL is powerful enough to handle billions of rows of data within a single system—far exceeding the limitations encountered in many enterprise applications.

Another advantage of SQL is its efficiency in resolving business requirements—particularly in reporting and transactions. Numerous features and functions in SQL have been developed to help programmers streamline their work.

The ESQL-Stmt framework retains DTOs for in-memory data processing and serialization, facilitating data transfer to and from RDBMS. These DTOs function as "value objects," designed for short lifetimes and implemented as immutable objects in Java. They are compatible with SQL data types, rows, records, or result sets from queries and can effectively handle input/output parameters. Additionally, these "value objects" support large objects (LOBs), array types, and structured data types like XML and JSON—both of which are now standard in mainstream RDBMS.

## History

The ESQL-Stmt project was initiated to rewrite ESI-DBTools, a component of ESI-Apps. **ESI-Apps** is a Java framework and engine designed to process SQL statement definitions within a Java project, enabling developers to easily create REST API services and separate SQL responsibilities from those of Java developers. This approach avoids embedding SQL code within Java code (unlike traditional ORM), allowing database developers to focus solely on SQL while Java developers handle UI and integration tasks. A backend business application can be developed using pure SQL if minimal customization logic is needed.

The current version of ESI-Apps is 3.5/3.6, but it has reached a development bottleneck that complicates integration with SQLStmt. As such, it is time for ESQL-Stmt to evolve separately from ESI-Apps, marking the beginning of **version 4.0** and beyond. While some proven code from the 3.x version will be carried over, much of it will require rewriting. I have also decided that this new module will be open-sourced to encourage community collaboration and improvement.

ESI-Apps and ESI-Report will be updated to incorporate the new version of ESQL-Stmt, although they will remain closed source.

@MyQuartz

*Rewritten for better English by ChatGPT*
