#!/bin/bash
#
# This is script to generate SOAP client library based on provided WSDL
#

# define common variables
LIBRARIES_DIR="./libs"
EXECUTABLE="/usr/bin/java"
BINARY_JAR_NAME="wsdl2ksoap2.jar"
CLASS_PATH="$BINARY_JAR_NAME:$LIBRARIES_DIR/ksoap2-android-assembly-3.0.0-jar-with-dependencies.jar:$LIBRARIES_DIR/axis2-kernel-1.6.2.jar:$LIBRARIES_DIR/axis2-codegen-1.6.2.jar:$LIBRARIES_DIR/axis2-adb-1.6.2.jar:$LIBRARIES_DIR/axis2-adb-codegen-1.6.2.jar:$LIBRARIES_DIR/XmlSchema-1.4.7.jar:$LIBRARIES_DIR/axiom-api-1.2.13.jar:$LIBRARIES_DIR/axiom-impl-1.2.13.jar:$LIBRARIES_DIR/neethi-3.0.2.jar:$LIBRARIES_DIR/wsdl4j-1.6.3.jar:$LIBRARIES_DIR/commons-logging-1.1.1.jar"

# check if binary exists or build new one
if [ -e $BINARY_JAR_NAME ] 
then
	echo "Found $BINARY_JAR_NAME. Using it to generate client stubs."
else
	echo "$BINARY_JAR_NAME not found. Start building..."
	if [ -d "./bin" ]; then
		rm -r "./bin"
	fi
	mkdir "./bin"
	
	ANT_COMMANT_STRING="ant -f build.xml"
	eval $ANT_COMMANT_STRING
fi
	

eval "$EXECUTABLE -cp $CLASS_PATH org.ksoap2.axis2.wsdl.WSDL2KSOAP2 --noBuildXML -or -u %1 %2 %3 %4 %5 %6 %7 %8 %9"


