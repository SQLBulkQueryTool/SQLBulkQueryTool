SQLBulkQueryTool
================

Query Tool for Bulk execution of SQL statements and Verification of expected results (i.e., regression testing) 

The premise of the tool is that you run a test ("scenario"), executing a defined set of SQL statements, and
compare those results with expected results.  For which several different level of reports (individual, group and summary)
are produced indicating success or failure.

The tool is easy to run one scenario at a time, but it's also designed so that you can run multiple scenarios
in a single run.  This enables you to organize your scenarios in folder groupings, and then run those groups against the 
same configured data source (i.e, database, application server, etc.).

The design of this allows you to scale test coverage 
=========================
Features:

-   Regression testing
    a.  validates actual results against expected results, include expected failures.
    b.  query execution time deviation validation
-   Support for Read and Write (i.e.,INSERT, UPDATE and DELETE) testing.
-   Logical transaction boundary testing is supported by executing multiple steps defined for a "Query".
-   Perform simple load testing, by executing a query repeatatively for "N" number of time



First let's state the "result modes" of execution, because they set the context for any execution. 

The tool has the following result modes when run:

1. sql     :  will generate sql queries (stored in .xml file) based on the databasemetadata from the connection
2. generate:  will create the expected result files from the executed queries.  1 result file per query.
3. compare :  execute the queries and compare the actual results to the expected results (even expected errors)
4. none    :  execute the queries (no comparison done), used for transaction tests or to just 
                verify the status of a query(s)
                
You can use the result modes to assist in building your regression testing portfolio and then automate the script execution
to so that you can be pro-active in catching issues.           

For configuration purposes, there is a hierarchy of files that can be configured.  The following list the files
in the order of granular applicability (global -> execution -> scenario)

NOTE: properties can be moved up or down, but is considered and advanced feature

1.  config/test.properties - these are the global properties, where you can set the driver, url, etc.,
2.  bin/run.conf - per execution properties, set the root locations of test artifacts (i.e., scenarios and query files)
3.  <scenario>.properties - the specific property file used to control 1 scenario.  Here you can set the specific scenario
            of queries and expected results to use, which must exist in the artifact root location (see run.conf above).
            The idea was that you reuse queries, but have a different set of expected results.  This was the case with Teiid,
            when the same queries can produce different rersults as each backend data source was tested.

Here's the quick start to using distribution kit:

1.  place your jdbc driver into the lib directory
2.  change the config/test.properies to set the driver and url
3.  copy the scenario.properties.template to create your <scenario>.properties file
4.  update run.conf, setting the SERVERNAME and PORT.
5.  execute:  ./run.sh   (with no parameters), this will use the default result mode (sql) in the run.conf and find the
        your <scenario>.properties file located in the scenario directory.
6.  Look in the results directory for the generated files
        
To use the generated query files for the next step of generating expected results, do the following
-  find the results/<scenario [1]>/sql/<scenario [2]> directory
-  copy/move the [2] <scenario> directory to test/query_sets directory, resulting in  test/query_sets/<scenario>/test_queries

Run "generate" expected results:
1.  execute: ./run.sh  <scenario> generate
2.  look in the results/<scenario>
    -   errors_for_generate directory will contain errors,if any
    -   generate directory will contain the query xml files that contains the expected results, 1 for each query

With the creation of the expected results, you now have what you need to put in place for regression testing.
To put those expected results in place to use, do the following:
1.  find the expected results directory:  results/<scenario>/generate/<scenario [2]/expected_results  
2.  copy/move the expected_results directory to test/query_sets/<scenario>, 
        resulting in test/query_sets/<scenario>/expected_results
        
Run "compare" to perform regression testing that will execute the queries and compare the results to the
expected results files.
1.  execute:  ./run.sh <scenario> compare
2.  look in the results directory for the Summary report (useful when multiple scenarios are run) and
    in the <scenario> directory, you should  the "COMPARE" report.
     
At this point, rerunning the scenario is essentially testing regression.  As changes (not new tables) are made to your data source,
you should see failures. 

What will cause a failure:
-   number of columns is changed
-   a column name is changed
-   an attribute is changed
-   the number of rows is different
-   the content that is in the returned result is different than expected
-   if the expected result is a failure (i.e., stackstrace), and the error message is different
  


=========================
Build from source
=========================

1.  mvn clean install  - to just compile source subprojects
2.  mvn clean install -Pintegration - to run integration tests using H2 database
3.  mvn clean install -Pdistro   - build the bqt-distro-*.zip distribution kit


TRANSACTIONS:

Besides supporting SELECT queries, INSERT, UPDATE and DELETE are also supported.   See the example queries 
file in tests/query_sets/query_set_template_folder/test_queries. 
For transaction testing, the ResultMode=None has to be used because no results are returned.  
Part of the validation process is to check
the updateCount, and that's indicated in the examples regarding how to set that.
Also, logical boundary testing is supported. Multiple steps in a logical transaction can be performed.  This
is also show in the examples.


