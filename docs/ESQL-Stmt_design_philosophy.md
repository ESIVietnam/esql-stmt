## Design overview

Embedded SQL Statement (ESQL-Stmt for short) framework is designed to help developing applications in a philosophy of **Separation of concerns** in 3 items:

1. Business Logic codes should be in separated code, and easy writing, easy testing by skilled SQL or DSL Developers with knowledge of the business requirement.
2. A generic programming language (like Java) should be use as "glue code" or "transpiled code" without business logic. It should provide library of utilities to parsing data, session and thread management, internal/external interfacing, message queuing, security and access control, etc... anything except business logic. It will be assembled with business code to provide APIs to solve the problem.
3. UI and other interface should be written in separated code (or by external tool) to inter-operate with backend to do business logic only via APIs. They should totally use "Mock APIs" in development phase.

To archive the philosophy, we design the framework helping us to build an application by the way:

1. Allow to write SQL (or SQL query builder) separated from Java code. It is similar to MyBatis in term of custom SQL statement, and it supports some standard SQL. All SQL statement defined in XML files with a set of predefined schemas (the ESQL-Stmt definition), and it will parsing and execution by the framework.
2. Writing glue code in a DSL, it is a set of other XML files that combine SQL statement definitions in to completed business logic, that called functions or logics.
3. To write global definition that defines how the funtions expose to API (REST, SOAP, other serverless framework like AWS Lambda).
