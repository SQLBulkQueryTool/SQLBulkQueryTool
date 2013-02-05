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

NOTE:  The distribution kit is the current means of using the tool to execute tests.  You cannot, currently, use maven,
other than building and integration tests.   See the STEP-BY-STEP section below.

The tool has been successfully testing on Windows XP and linux.

====================
Documentation
====================
See https://github.com/SQLBulkQueryTool/SQLBulkQueryTool/wiki


**************************
  Building From Source
**************************

(Maven needs to be installed for the following commands to work - see http://maven.apache.org/guides/getting-started/index.html)

1.  mvn clean install               --> to compile source subprojects
2.  mvn clean install -Pintegration --> to run integration tests using H2 database
3.  mvn clean install -Pdistro      --> build the bqt-distro-*.zip distribution kit

or run:  mvn clean install -Pintegration,distro perform everything
