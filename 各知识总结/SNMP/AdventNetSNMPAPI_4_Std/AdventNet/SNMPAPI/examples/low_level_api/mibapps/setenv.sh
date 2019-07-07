#!/bin/sh

# Set the java home directory, 
# For example: If jdk1.2 is installed in your machine in the /usr/jdk1.2 
# directory then set JAVA_HOME_DIR=/usr/jdk1.2
JAVA_HOME_DIR=`which java`
if [ -z ${JAVA_HOME_DIR} ]
  then
        echo "No Java VM found"
        echo "Set JAVA_HOME directory (eg: /javadir/jdk1.3/bin) in the PATH"
  else              
JAVA_HOME_DIR=`dirname $JAVA_HOME_DIR`
JAVA_HOME_DIR=`dirname $JAVA_HOME_DIR`       

# Set the PATH
   PATH=${JAVA_HOME_DIR}:${PATH}

# Set the CLASSPATH
   CLASSPATH=.:../../../jars/AdventNetSnmp.jar:../../../jars/AdventNetLogging.jar:../../../cryptix/classes
	export PATH CLASSPATH
fi
