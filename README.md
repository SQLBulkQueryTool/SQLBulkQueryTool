SQLBulkQueryTool
================

Query Tool for Bulk execution of SQL statements and Verification of expected results (i.e., regression testing) 


The basic usage is that you define a scenario, which indicates which queries and expected results to use.
When the test is run, indicate which scenario to run.  The output from the run will indicate which tests
succeeded or failed (and why).  

=========================
Features:

-   Regression testing
    a.  validates actual results against expected results, include expected failures.
    b.  query execution time deviation validation
-   Support for Read and Write (i.e.,INSERT, UPDATE and DELETE) testing.
-   Logical transaction boundary testing is supported by executing multiple steps defined for a "Query".
-   Perform simple load testing, by executing a query repetatively for "N" number of time


=========================


How to get started?

1st - create the scenario and the set of queries that will perform the test.

    Create the {scenarioname}.properties file, following the scenario.properties.template.
    Then, either the query file can be created manually or automated.  
    To do this manually, follow the layout of the test queries examples (tests/query_sets/query_set_template_folder)
    Or, you can generate an initial set of queries using this tool.  Run the
    test using the ResultMode=SQL which will create an xml file containing
    a query for each table / view defined in the database metadata.
    
2nd - generate expected results from the queries

    If you ran the ResultMode=SQL option in the 1st step, then in the results/{scenario}/sql directory, 
    copy (or move) the {queryset.dirname} directory to tests/query_sets.  Creating the directory
    structure of  test/query_sets/{queryset.dirname}/{test.queries.dirname}/{query file}.xml
    
    or
    
    set update QUERYSETDIR in the run.conf to the locationn of your query sets to use (which still must
    follow the structure of: {query set root dir}/{queryset.dirname}/{test.queries.dirname}/{query file}.xml


3rd - execute the test to compare the actual results to the expected results.  This is the step that can
    be setup to run regression tests to verify that something hasn't changed.  To run this step, 
    use the ResultMode=Compare. 
    
    Check the "results" directory for the output and summary reports.
    
    
===============================
User Guide Notes:

The tool has the following result modes when run:
-  sql     :  to generate sql queries based on the databasemetadata from the connection
-  generate:  to create the expected result files from the executed queries.  1 result file per query.
-  compare :  execute the queries and compare the actual results to the expected results (even expected errors)
-  none    :  execute the queries (no comparison done), can be used for transaction tests or to just 
                verify the status of a query(s)

The tool can assist you in getting started by using the "sql" option, or you can create your own queries, 
following the template (bqt-distro/src/main/resources/ctc_tests/query_sets.

TRANSACTIONS:

Besides supporting SELECT queries, INSERT, UPDATE and DELETE are also supported.   See the example queries 
file in tests/query_sets/query_set_template_folder/test_queries. 
For transaction testing, the ResultMode=None has to be used because no results are returned.  
Part of the validation process is to check
the updateCount, and that's indicated in the examples regarding how to set that.
Also, logical boundary testing is supported. Multiple steps in a logical transaction can be performed.  This
is also show in the examples.

