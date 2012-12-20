SQLBulkQueryTool
================

Query Tool for Bulk execution of SQL statements and Verification of expected results (i.e., regression testing) 

The premise of the tool is that you run a test ("scenario"), which defines a set of SQL statements to execute, and
compare the actual results with expected results.  For which several different level of reports (individual, group and summary)
are produced indicating success or failure, as well as, summary of successes and failures.

The tool is easy to run one scenario at a time, but it's also designed so that you can run multiple scenarios
in a single run.  This enables you to organize your scenarios in folder groupings, and then run those groups against the 
same configured data source (i.e, database, application server, etc.).

====================
PREREQUISITES:
====================
- Java JDK 6 or newer
- Maven

The tool has been successfully testing on Windows XP and linux.


====================
FEATURES:
====================
-   Regression testing
    a.  validates actual results against expected results, include expected failures.
    b.  query execution time deviation validation
-   Support for Read and Write (i.e.,INSERT, UPDATE and DELETE) testing.
-   Logical transaction boundary testing is supported by executing multiple steps defined for a "Query".
-   Perform simple load testing, by executing a query repeatatively for "N" number of time


====================
RESULT MODES:
====================
First let's state the "result modes" of execution, because they set the context for any test run. 

The tool has the following result modes when run:

1. sql     :  will generate sql queries (stored in .xml file) based on the databasemetadata from the connection
2. generate:  will create the expected result files from the executed queries.  1 result file per query.
3. compare :  execute the queries and compare the actual results to the expected results (even expected errors)
4. none    :  execute the queries (no comparison done), used for transaction tests or to just 
                verify the status of a query(s)
                
You can use the result modes to assist in building your regression testing portfolio and then automate the script execution
so that you can be pro-active in catching issues by running "compare" as your regression test.          

For configuration purposes, there is a hierarchy of files that can be configured.  The following list the files
in the order of granular applicability (global -> execution -> scenario)

NOTE: properties can be moved up or down, but is considered an advanced feature

1.  config/test.properties - these are the global properties, where you can set the driver, url, etc.,
2.  bin/run.conf - per execution properties, set the root locations of test artifacts (i.e., scenarios and query files)
3.  {scenario}.properties - the specific property file used to control 1 scenario.  Here you can set the specific scenario
            of queries and expected results to use, which must exist in the artifact root location (see run.conf above).
            The idea was that you reuse queries, but have a different set of expected results.  This was the case with Teiid,
            when the same queries can produce different rersults as each backend data source was tested.

===========================================================
STEP-BY-STEP GUIDE TO USING DISTRIBUTION KEY:
===========================================================

A distribution zip file is created from the build, which is meant to be used to execute the tests.


**************************
 A)  Building From Source
**************************

(Maven needs to be installed for the following commands to work - see http://maven.apache.org/guides/getting-started/index.html)

1.  mvn clean install               --> to compile source subprojects
2.  mvn clean install -Pintegration --> to run integration tests using H2 database
3.  mvn clean install -Pdistro      --> build the bqt-distro-*.zip distribution kit

or run:  mvn clean install -Pintegration,distro perform everything

**************************
 B) Setup
**************************

The following describes the steps to setting up the environment to execute the tests.

1.  Extract the contents of the distro .zip file. The following 6 folders should be present:
                    - /bin
                    - /config
                    - /lib
                    - /licenses
                    - /log
                    - /tests

2.  Place your jdbc driver into the /lib directory
3.  Change the /config/test.properies file to set the DRIVER, URL, etc.
4.  Use the /tests/scenarios/scenario.properties.template file to create a new <scenario>.properties file
5.  Update the /bin/run.conf file to set the SERVERNAME and PORT.


**************************
 C) Execution
**************************

To run a test, which could consist of one or more scenarios (i.e., {scenario}.properties files), you can run:

        -   ./run.sh (run.bat)  - uses settings defined in run.conf and test.properties
        -   ./run.sh (run.bat)  [scenarioname] [resultmode]
           where - scenarioname can either reference the folder or .properties file for which scenario(s) to run.
                   If a folder, must be under the tests/scenarios folder
                 - (optional) resultmode is either:  compare, generate, sql, none


    Windows users can also use Cygwin to execute the shell script, however care should be taken when setting path names. This 
    is due to the different way that the path is expressed (e.g. / versus \).

Look in the results directory for output files from the tests.  This could include individual and summary reports, 
error reports, generated sql query files and generated expected results files.

NOTE:  For the purposes of the document and describing what to execute, it will show how to execute ./run.sh [scenarioname] [resultmode]
Realize these 2 parameters can be set in the run.conf file and then execute ./run.sh without parameters.  

**************************
 D) Generating Queries
**************************

The "sql" result mode can be used to generate a set of queries based on the metadata from the database.  These queries
would be considered a baseline set of queries that could be used to confirm each table or view is accessible and 
contains the content you expect.  

To run the test in SQL mode, execute:  ./run.sh [scenarioname] sql

Look in the results/[scnearioname]/sql/[scenarioname] directory for the generated files.

To use the generated query files in resultmodes:  generate, compare or none,  
copy or move the [scenarioname] directory under /sql/ to test/query_sets directory, resulting in
test/query_sets/[scenarioname]/test_queries


**************************
 E) Creating Expected Results
**************************

The next step is to use the generated query files to create the expected results.
 
 
1.  Navigate to the location where the test queries folder was created. This will be under /bin/results/scenarios/sql
2.  Copy/move this folder to the following location: /tests/query_sets/[scenario].
    The structure should look like this: /tests/query_sets/[scenario folder]/test_queries
3.  Run the script in GENERATE mode. This is performed by executing "./run.sh  [scenarioname] generate" 
4.  The output files (.xml and .txt) will be created within the results/[scenarioname] folder. In particular,
    - The .txt report will be created directly under results/[scenariooname]
    - The /errors_for_generate directory will contain errors (if any)
    - The /generate directory holds the query xml files (1 for each query) that contain the expected results
 
 
 
********************************************
  F) Testing Queries against Expected Results / Regression Testing
********************************************
   
With the queries and their expected results in place, you can now perform regression testing.
First, the files containing the expected results must be put in place.
 
 
1.  Navigate to the location where the expected results folder was created. This will be under /results/[scenarioname]/generate/[query_set_name]
2.  Copy/move the [query_set_name] folder to the following location: tests/query_sets/.
          The structure should look like this: /tests/query_sets/[query_set_name]/expected_results
3.  Run the script in COMPARE mode. This is performed by executing "./run.sh  [scenarioname] compare".
          This will effectively perform regression testing by executing the queries and comparing the results against the expected results files.
4.  The output files (.xml and .txt) will be created within the /results/[scenarioname] folder. In particular,
          - The .txt report will be created directly under /results/[scenarioname]
          - The /errors_for_compare directory will contain errors (if any)
   
At this point, rerunning the scenario is essentially testing regression.  As changes (not new tables) are made to your data source,
you should see failures.
 
 
The following changes will cause a failure:
-   number of columns is changed
-   a column name is changed
-   an attribute is changed
-   the number of rows is different
-   the content that is in the returned result is different than expected
-   if the expected result is a failure (i.e., stackstrace), and the error message is different
   
   
=======================
SUPPORTED TRANSACTIONS:
=======================
 
 
Besides supporting SELECT queries, INSERT, UPDATE and DELETE are also supported.
See the example queries file found under /tests/query_sets/query_set_template_folder/test_queries.
For transaction testing, the ResultMode=None has to be used because no results are returned. 
Part of the validation process is to check the updateCount, and that's indicated in the examples regarding how to set that.
Also, logical boundary testing is supported. Multiple steps in a logical transaction can be performed.
This is also show in the examples.
 
 
======================
CONFIGURING SCENARIOS:
======================
 
 
A single scenario is defined by a single [scenarioname].properties file. 
One or more scenarios can be run at one time, in sequential order.
 
To create a scenario file, make a copy of the scenario.properties.template (found in the tests\scenarios folder) and rename it to {name}.properties.
  
Depending on whether you are creating a single or multiple scenarios the detailed steps are as follows:
 
 
SINGLE SCENARIOS
----------------
 
 
A single scenario can be created and placed in the /tests/scenarios folder.
 
 
1.          Make a copy of the scenario.properties.template in the scenarios folder and name it {name}.properties.
2.          Edit the properties according to the template.
3.          When executing ./run.sh pass in the <scenario>.properties file name
 
 
 
 
MULTIPLE SCENARIOS
------------------
 
 
Multiple scenarios can be set up to run at the same time.
 
 
1.          Create a new folder under the /tests/scenarios folder
2.          Within the new folder, create one scenario properties file for each scenario to run in the group
3.          When executing ./run.sh  pass in the folder that contains the scenario files
