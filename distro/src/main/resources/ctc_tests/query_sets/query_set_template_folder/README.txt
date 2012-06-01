{\rtf1\ansi\ansicpg1252\cocoartf1038\cocoasubrtf350
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\ql\qnatural\pardirnatural

\f0\fs24 \cf0 queries README\
\
A query_sets folder defines the test queries to run and the expected results that are used to compare to the actual results for validation.   This folder is specified in the <scenario>.properties file by the "queryset.dir" property and provides the location to find the test queries and expected results to use when executing the specified scenario.\
\
The query_set_template_folder is an example for setting up a new query set folder.\
\
NOTE:   A common use case pattern is for there to be a single set of test queries, but there are multiple expected results directories.   This is common when the same query run against a different vdb and produces a slightly different  result.      \
}