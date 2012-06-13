#!/bin/sh

#  This script will run the client tests 
#
#  To run this script:    ./run.sh  [scenarioname] [resultmode]
#		where - scenarioname can either reference the folder or .properties file for which scenario(s) to run
#               this would need to include the full path to the folder or file
#			  - resultmode is either;  compare, generate, sql, none

#	compare:  compare actuals to expected results
#	generate:  create new set of expected result files in the results directory
#	none:	execute the queries, but no comparison is done
#	sql:	create a query file containing a query for each table exposed in DatabaseMetaData
#
#
#   NOTE: See the run.conf file for the defaults and the place that settings can be changed 

# resolve links - $0 may be a softlink
LOC="$0"

while [ -h "$LOC" ] ; do
  ls=`ls -ld "$LOC"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    LOC="$link"
  else
    LOC=`dirname "$LOC"`/"$link"
  fi
done

DIRNAME=`dirname "$LOC"`
PROGNAME=`basename $0`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi


#--------------------


PRGDIR=`cd "$DIRNAME/.."; pwd`

cd "${PRGDIR}"


if [ -n "$1" ]; then
	SCENARIO_DIR="${1}"
else
	SCENARIO_DIR="${SCENARIO}"
fi

if [ -n "$2" ]; then
	RMODE="${2}"
else
	RMODE=${RESULTMODE}
fi

# ensure scenario dir exist
if [ ! -x "${SCENARIO_DIR}" ]; then
  
        echo ""
        echo "Scenario Location - ${SCENARIO_DIR} doesn't exist"
        echo "Exit"
        exit

fi

# ensure scenario dir exist
if [ ! -x "${SCENARIO_DIR}" ]; then
  
        echo ""
        echo "Scenario Location - ${SCENARIO_DIR} doesn't exist"
        echo "Exit"
        exit

fi

# ensure scenario dir exist
if [ ! -x "${QUERYSETDIR}" ]; then
  
  		echo ""
		echo "QuerySet Directory - ${QUERYSETDIR} doesn't exist"
		echo "Exit"
		exit

fi


echo "============================"
echo "Result Mode ${RMODE}"
echo "Using Configuration File ${CONFIG}"
echo "Running scenarios ${SCENARIO_DIR}"
echo "Executing queris ${QUERYSETDIR}"
echo "Output Results to ${OUTPUTDIR}"
echo "============================"



ARGS=" -Dscenario.file=${SCENARIO_DIR}"
ARGS="${ARGS} -Dqueryset.artifacts.dir=${QUERYSETDIR}"
ARGS="${ARGS} -Dresult.mode=${RMODE}"
ARGS="${ARGS} -Doutput.dir=${OUTPUTDIR}"
ARGS="${ARGS} -Dconfig=${CONFIG}"

JAVA_OPTS=" "

if [ ! -z "${DEBUG_OPTS}" ] 
	then
	
	JAVA_OPTS="${DEBUG_OPTS}"
	
fi

# default to the ip address used to start the server

if [ -z "${SERVERNAME}" ] 
	then
	ARGS="${ARGS} -Dserver.host.name=localhost"
else
	ARGS="${ARGS} -Dserver.host.name=${SERVERNAME}"
fi



if [ -z "${EXCEEDPERCENT}" ] 
	then
	ARGS="${ARGS} -Dexceedpercent=-1"
else
	ARGS="${ARGS} -Dexceedpercent=${EXCEEDPERCENT}"
	
fi

if [ -z "${EXECTIMEMIN}" ] 
	then
	ARGS="${ARGS} -Dexectimemin=-1"
else
	ARGS="${ARGS} -Dexectimemin=${EXECTIMEMIN}"
	
fi

 
if [ ! -x "${PRGDIR}/log" ]; then
    echo "Create ${PRGDIR}/log directory"
	mkdir "${PRGDIR}"/log
fi


CP="${PRGDIR}:${PRGDIR}/config/*:${PRGDIR}/lib/*"

LOGLEVEL=info

echo "CP=$CP"

java -cp "${CP}" ${JAVA_OPTS} ${ARGS} org.jboss.bqt.client.TestClient









