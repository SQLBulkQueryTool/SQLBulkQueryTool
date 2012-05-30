SQLBulkQueryTool
================

Query Tool for Bulk execution of SQL statements and Verifcation of expected results.  


The basic usage is that you define a scenario, which indicates which queries and expected results to use.
When the test is run, indicate which scenario to run.  The output from the run will indicate which tests
succeeded or failed (and why).  

The tool has the following result modes when run:
-  sql     :  to generate sql queries based on the databasemetadata from the connection
-  generate:  to create the expected result files from the executed queries.  1 result file per query.
-  compare :  execute the queries and compare the actual results to the expected results (even expected errors)
-  none    :  execute the queries (no comparison done), can be used to verify the status of a query(s)

The tool can assist you in getting started by using the "sql" option, or you can create your own queries, 
following the template (bqt-distro/src/main/resources/ctc_tests/query_sets.