# wsdl2ksoap2-android
Automatically exported from code.google.com/p/wsdl2ksoap2-android

This project is a CLI based tool to create valid ksoap2-android based classes from WSDL file that is compatible with the Android platform.

### Features
- automatic source code generating from WSDL
- client and server side interfaces generating
- simple and complex data types generating
- attribute values support
- NULL values support
- Enum types support
- Array types support
- QName values support (partially, for fields only, not for attributes)
- generated source coed contains Endpoint implementation (it can be used for server side interface implementation)


### Usage
Make sure that you have latest ANT and JAVA installed on your system.

Run to see available options:
- Unix/Linux/MacOSX - ./wsdl2ksoap.sh
- Windows - .\wsdl2ksoap.bat

NOTE: On Windows make sure to run ANT first in order to compile library JAR. On Unix-like 
systems it will be done automatically upon execution of "./wsdl2ksoap.sh"


### Integration
Make sure to copy generated code to your source path and to add './libs/ksoap2-android-assembly-3.1.1-jar-with-dependencies.jar' and 'wsdl2ksoap2.jar' as dependencies for your project classpath.

For more info about ksoap2 usage please refer to: [http://simpligility.github.io/ksoap2-android/index.html]
