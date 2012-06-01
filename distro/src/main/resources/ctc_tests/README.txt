{\rtf1\ansi\ansicpg1252\cocoartf1038\cocoasubrtf350
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
\margl1440\margr1440\vieww17020\viewh11020\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\ql\qnatural\pardirnatural

\f0\fs24 \cf0 Readme for How To use bqt-framework as a jdbc query testing tool that can:\
\
1.	generate set of "select" queries based on databasemetadata\
2.	execute queries to verify they run\
3.	execute queries to generate expected results to be used in step 4\
4.	execute queries and have the results validated against the expected results\
\
Besides supporting SELECT queries, INSERT, UPDATE and DELETE are supported.   See the example queries file in ctc_tests/query_sets/query_set_template_folder/test_queries.\
\
-----------------\
Setup:\
\
-	unzip the kit\
-	copy the Teiid jdbc driver to the "lib" directory\
-	update the run.conf file in the bin directory to set the SCENARIODIR and QUERYSETDIR properties.  (example: ../qe/teiid-test-artifacts/crc-tests/scenarios and  ../qe/test-test-artifacts/ctc-tests/queries).   If either, or both, are not setup, then their locations will default to ctc_tests/scenarios and ctc_tests/query_sets, respectively.\
-	cd into bin directory\
-	execute  run.sh <scenariofoldername> <resultsetmode>\
\
	where \
	-	<scenariofoldername> is a name of a folder that exist in the SCENARIODIR.\
	-	<resultsetmode> is either:\
		1.	sql  -	to generate sql queries\
		2.	none - execute queries to verify they run\
		3.	generate -  create the expected results\
		4.	compare - execute queries and compare actual results to expected results\
\
\
Options:\
-	the kit comes with template query_sets and scenarios folders, under the ctc_tests directory.   Each have a README.txt file that explains how their are setup and how to use them.   \
\
}