#!/bin/sh

#  This script will run the client tests 
#
#  To run this script:   
#       - To use the defaults in run.conf, execute:  ./run.sh
#       - Are pass parameters, execute:  ./run.sh  [scenarioname] [resultmode]
#
#       where - scenarioname can either reference the folder or .properties file for which scenario(s) to run
#               this would need to include the full path to the folder or file
#             - resultmode is either;  compare, generate, sql, none
#
#   compare:  compare actuals to expected results
#   generate:  create new set of expected result files in the results directory
#   none:   execute the queries, but no comparison is done
#   sql:    create a query file containing a query for each table exposed in DatabaseMetaData
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

ROOTDIR=`cd "$DIRNAME/.."; pwd`

cd "${ROOTDIR}"

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
   RUN_CONF="${ROOTDIR}/bin/run.conf"
fi
   echo "RUNCONF: ${RUN_CONF}"
if [ -r "$RUN_CONF" ]; then
   . "$RUN_CONF"
fi

# Convert paths for Windows Java if Cygwin is used
if [ "$(uname -o)" == "Cygwin" ]; then
    SCENARIODIR=`cygpath -w ${SCENARIODIR}`
    QUERYSETDIR=`cygpath -w ${QUERYSETDIR}`
    OUTPUTDIR=`cygpath -w ${OUTPUTDIR}`
fi

echo "SCENARIODIR: ${SCENARIODIR}"
#--------------------

# ensure scenario dir property is set
if [ -z "${SCENARIODIR}" ]; then

echo ""
echo "SCENARIODIR property is not set, check run.conf"
echo "Exit"
exit

fi

if [ -n "$1" ]; then
    S_DIR=${SCENARIODIR}/${1}
else

if [  -z "${SCENARIO}" ]
then
    S_DIR=${SCENARIODIR}/${SCENARIO}

else

echo ""
echo "SCENARIO property is not set or passed in as argument, check run.conf"
echo "Exit"
exit

fi

fi

if [ -n "$2" ]; then
    RMODE="${2}"
else
    RMODE=${RESULTMODE}
fi

# ensure scenario dir exist
if [ ! -x "${QUERYSETDIR}" ]; then

echo ""
echo "QuerySet Directory - ${QUERYSETDIR} doesn't exist, check run.conf"
echo "Exit"
exit

fi


echo "============================"
echo "Result Mode ${RMODE}"
echo "Using Configuration File ${CONFIG}"
echo "Running scenarios ${S_DIR}"
echo "Executing queris ${QUERYSETDIR}"
echo "Output Results to ${OUTPUTDIR}"
echo "============================"



ARGS=" -Dscenario.file=${S_DIR}"
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
    ARGS="${ARGS} -Dhost.name=localhost"
else
    ARGS="${ARGS} -Dhost.name=${SERVERNAME}"
fi

# default to the ip address used to start the server

if [ ! -z "${PORT}" ] 
then
    ARGS="${ARGS} -Dhost.port=${PORT}"
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

if [ ! -z "${USERNAME}" ] 
then
    ARGS="${ARGS} -Dusername=${USERNAME}"
fi

if [ ! -z "${PASSWORD}" ] 
then
    ARGS="${ARGS} -Dpassword=${PASSWORD}"
fi

if [ ! -z "${SUPPORT_PRE10_SCENARIO}" ] 
then
    ARGS="${ARGS} -Dsupport.pre1.0.scenario=${SUPPORT_PRE10_SCENARIO}"
fi

if [ ! -z "${TEIID_QUERYPLAN}" ] 
then
    ARGS="${ARGS} -Dbqt.query.plan=${TEIID_QUERYPLAN}"
fi


if [ ! -x "${ROOTDIR}/log" ]; then
    echo "Create ${ROOTDIR}/log directory"
    mkdir "${ROOTDIR}"/log
fi


CP="${ROOTDIR}:${ROOTDIR}/config/*:${ROOTDIR}/lib/*"

# Convert classpath for Windows Java if Cygwin is used
if [ "$(uname -o)" == "Cygwin" ]; then
    CP=$(cygpath -pw $CP)
fi

LOGLEVEL=info

echo "CP=$CP"

java -cp "${CP}" ${JAVA_OPTS} ${ARGS} org.jboss.bqt.client.TestClient
