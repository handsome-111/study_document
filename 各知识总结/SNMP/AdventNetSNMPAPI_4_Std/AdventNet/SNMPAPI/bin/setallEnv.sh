#!/bin/sh
currDir=`pwd`
cd ..
# Set Green Thread
THREADS_FLAG=green
export THREADS_FLAG

# DISABLE JIT COMPILER
JAVA_COMPILER=NONE      
export JAVA_COMPILER    

JAVA_EXE=`which java`
if [ $? -ne 0 ]; then
	echo java command not found
	echo "Set the JAVA Path"
	cd $currDir
	exit 1
fi	
	
JAVA_HOME_DIR=`dirname $JAVA_EXE`
JAVA_HOME_DIR=`dirname $JAVA_HOME_DIR`

TOOLS="lib/tools.jar"

if [ ! -f $JAVA_HOME_DIR/$TOOLS ]
then
    JAVA_HOME_DIR=`dirname $JAVA_HOME_DIR`
    if [ -f $JAVA_HOME_DIR/bin/java ]
    then
        PATH=$JAVA_HOME_DIR/bin:$PATH
        export PATH
    else
        echo "Java executable not found in directory $JAVA_HOME_DIR/bin "
		cd $currDir
        exit -1
    fi
    
    if [ ! -f $JAVA_HOME_DIR/$TOOLS ]
    then
        echo "The file $JAVA_HOME_DIR/$TOOLS is not found"
		cd $currDir
        exit -1
    fi
fi

SNMP_HOME=.

CLASSPATH=$SNMP_HOME/classes:$SNMP_HOME/jars/crimson.jar:$SNMP_HOME/jars/JimiProClasses.zip:$SNMP_HOME/jars/jaxp.jar:$SNMP_HOME/jars/AdventNetUpdateManager.jar

CLASSPATH=$JAVA_HOME_DIR/lib/tools.jar:$JAVA_HOME_DIR/lib/dt.jar:$CLASSPATH

export CLASSPATH

JAVA_CMD="$JAVA_HOME_DIR/bin/java "
export JAVA_CMD
cd $currDir

