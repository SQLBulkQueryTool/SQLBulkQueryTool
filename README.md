SQLBulkQueryTool
================

Query Tool for Bulk execution of SQL statements and Verification of expected results (i.e., regression testing) 

The premise of the tool is that you run a test ("scenario"), executing a defined set of SQL statements, and
compare those results with expected results.  For which several different level of reports (individual, group and summary)
are produced indicating success or failure.

The tool is easy to run one scenario at a time, but it's also designed so that you can run multiple scenarios
in a single run.  This allows you to organize your scenarios in folder groupings, and then run those groups against the 
same configured data source (i.e, database, application server, etc.).

First let's state the "result modes" of execution, because they set the context for any execution. 

The tool has the following result modes when run:
1.  sql     :  will generate sql queries (stored in .xml file) based on the databasemetadata from the connection
2.  generate:  will create the expected result files from the executed queries.  1 result file per query.
3.  compare :  execute the queries and compare the actual results to the expected results (even expected errors)
4.  none    :  execute the queries (no comparison done), used for transaction tests or to just 
                verify the status of a query(s)


If you are starting fresh with tool, here's how you can use the tool:

1. First, run using the "sql" result mode.  This will give you a base set of queries against every table and view.  
    These set of queries are useful as a baseline test to ensure expected tables/views are all visible and accessible. 
2. Second, run using the "generate" result mode.  This will generate expected results files for the executed queries.
    Now you have what is referred to as a "scenario".  You have:
            a.  the scenario.properties file
            b.  test queries .xml file
            c.  expected results
3. Third, run using the "compare" result mode.  View the results to confirm all compared correctly.
   
At this point, rerunning the scenario is essentially testing regression.  As changes (not new tables) are made to your data source,
you should see failures. 

What will cause a failure:
-   number of columns is changed
-   a column name is changed
-   an attribute is changed
-   the number of rows is different
-   the content that is in the returned result is different than expected
-   if the expected result is a failure (i.e., stackstrace), and the error message is different


Now you can begin making changes/additions to  

The design of this allows you to scale test coverage 
=========================
Features:

-   Regression testing
    a.  validates actual results against expected results, include expected failures.
    b.  query execution time deviation validation
-   Support for Read and Write (i.e.,INSERT, UPDATE and DELETE) testing.
-   Logical transaction boundary testing is supported by executing multiple steps defined for a "Query".
-   Perform simple load testing, by executing a query repeatatively for "N" number of time


=========================
How to get started
=========================

Build from source

1.  mvn clean install  - to just compile source subprojects
2.  mvn clean install -Pintegration - to run integration tests using H2 database
3.  mvn clean install -Pdistro   - build the bqt-distro-*.zip distribution kit


=========================
How to use the distro kit
=========================

-   setup
    a.  unzip the kit
    b.  place your JDBC driver into the lib directory
    
    
- create the scenario and the set of queries that will perform the test.

    Create the {scenarioname}.properties file, following the scenario.properties.template.
    Then, either the query file can be created manually or automated.  
    
    Manually
    
    To do this manually, follow the layout of the test queries examples (tests/query_sets/query_set_template_folder)
    
    Generate
    
    You generate an initial set of queries using this tool.  Execute: ./run.sh
    where ResultMode=SQL which will cause the creation an xml file containing
    a query for each table / view defined in the database metadata.  Find the results/{scenario}/sql directory.
    Copy the directory below that which begins with {scenario}/test_queries to tests/query_sets, so that the 
    directory ends up like:  tests/query_sets/{scenario}/test_queries
    
- generate expected results from the queries

    If you ran the ResultMode=SQL option above then your directory struction is set.in the 1st step, then in the results/{scenario}/sql directory, 
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


