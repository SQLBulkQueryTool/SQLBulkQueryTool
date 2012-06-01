#!/bin/sh

#  This script will run the client tests 
#
#  To run this script:    ./run.sh  [scenariofoldername] [resultmode]
#		where - scenariofoldername is the folder that contains the scenario files to run
#			  - resultmode is either;  compare, generate, sql, none

#	compare:  compare actuals to expected results
#	generate:  create new set of expected result files in the results directory
#	none:	execute the queries, but no comparison is done
#	sql:	create a query file containing a query for each table exposed in DatabaseMetaData
#

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


if [ -z "${QUERYSETDIR}" ] 
	then
	
	QUERYSETDIR="${PRGDIR}/ctc_tests/query_sets"
	
fi

if [ -n "$1" ]; then
	SCENARIODIR="${PRGDIR}/ctc_tests/scenarios/${1}"
else

	if [ ! -z "${SCENARIO}" ]
		then
		SCENARIODIR="${PRGDIR}/ctc_tests/scenarios/${SCENARIO}"
		
	fi
	
fi

if [ -n "$2" ]; then
	RMODE="${2}"
else
	RMODE=${RESULTMODE}
fi

if [ -z "${SCENARIODIR}" ] 
	then
	
	SCENARIODIR="${PRGDIR}/ctc_tests/scenarios"
	
fi

# ensure scenario dir exist
if [ ! -x "${SCENARIODIR}" ]; then
  
  		echo ""
		echo "SCENARIODIR ${SCENARIODIR} doesn't exist"
		echo "Exit"
		exit

fi

# ensure scenario dir exist
if [ ! -x "${QUERYSETDIR}" ]; then
  
  		echo ""
		echo "QUERYSETDIR - ${QUERYSETDIR} doesn't exist"
		echo "Exit"
		exit

fi


if [ -z "${OUTPUTDIR}" ] 
	then
	
	OUTPUTDIR="${PRGDIR}/results"

fi


echo "============================"
echo "Running scenarios ${SCENARIODIR}"
echo "Executing queris ${QUERYSETDIR}"
echo "Output Results to ${OUTPUTDIR}"
echo "============================"

CTCXML=${PRGDIR}/ctc_tests/ctc.xml


ANT_ARGS=" -Dscenario.dir=${SCENARIODIR}"
ANT_ARGS="${ANT_ARGS} -Dqueryset.artifacts.dir=${QUERYSETDIR}"
ANT_ARGS="${ANT_ARGS} -Dproj.dir=${PRGDIR}"
ANT_ARGS="${ANT_ARGS} -Dresult.mode=${RMODE}"
ANT_ARGS="${ANT_ARGS} -Doutput.dir=${OUTPUTDIR}"


#if [ ! -z "${DEBUG_OPTS}" ] 
#	then
	
#	ANT_ARGS="${ANT_ARGS} -Ddebug.opts=${DEBUG_OPTS} "
	
#fi

# default to the ip address used to start the server

if [ -z "${SERVERNAME}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dserver.host.name=localhost"
else
	ANT_ARGS="${ANT_ARGS} -Dserver.host.name=${SERVERNAME}"
fi



if [ -z "${XMLCLZZ}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dquery.scenario.classname=org.jboss.bqt.test.client.ctc.CTCQueryScenario"
else
	ANT_ARGS="${ANT_ARGS} -Dquery.scenario.classname=${XMLCLZZ}"
fi



if [ -z "${CONFIGFILE}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dconfig=${PRGDIR}/ctc_tests/ctc-test.properties"
else
    ANT_ARGS="${ANT_ARGS} -Dconfig=${CONFIGFILE}s"
fi


if [ -z "${USERNAME}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dusername=user"
else
	ANT_ARGS="${ANT_ARGS} -Dusername=${USERNAME}"
fi

if [ -z "${PASSWORD}" ] 
	then
    ANT_ARGS="${ANT_ARGS} -Dpassword=user"
else
	ANT_ARGS="${ANT_ARGS} -Dpassword=${PASSWORD}"
fi



if [ -z "${EXCEEDPERCENT}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dexceedpercent=-1"
else
	ANT_ARGS="${ANT_ARGS} -Dexceedpercent=${EXCEEDPERCENT}"
	
fi

if [ -z "${EXECTIMEMIN}" ] 
	then
	ANT_ARGS="${ANT_ARGS} -Dexectimemin=-1"
else
	ANT_ARGS="${ANT_ARGS} -Dexectimemin=${EXECTIMEMIN}"
	
fi




if [ ! -z "${PROPFILE}" ] 
	then
	
	ANT_ARGS=" -propertyfile $PROPFILE $ANT_ARGS "
	
fi




ANT_OPTS="-Xmx256m"
ANT_HOME=${PRGDIR}/ant

# uncomment for additional debugging related to ant, but this isn't debugging of the
# the app.  For the app, see ctx.xml.
#	ANT_ARGS="${ANT_ARGS} -verbose"

 
if [ ! -x "${PRGDIR}/log" ]; then
    echo "Create ${PRGDIR}/log directory"
	mkdir "${PRGDIR}"/log
fi

if [ -n "${JAVA_OPTS}" ]; then
	ANT_OPTS="${JAVA_OPTS} $ANT_OPTS "
fi

CP="${PRGDIR}:${PRGDIR}/lib/*:${PRGDIR}/ant/*"

LOGLEVEL=info

echo "ANT BUILDFILE=${CTCXML}"
echo "ANT_HOME=${ANT_HOME}"
echo "ANT_ARGS=${ANT_ARGS}"
echo "CP=$CP"

java -cp "${CP}" -Dant.home="${ANT_HOME}" $ANT_OPTS org.apache.tools.ant.Main $ANT_ARGS -buildfile ${CTCXML}

export CONFIGFILE=
export XMLCLZZ=
export SERVERNAME=







