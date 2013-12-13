package org.ksoap2.axis2.wsdl;

import org.apache.axis2.wsdl.WSDL2Code;

public class WSDL2KSOAP2 {

	private WSDL2KSOAP2() {}

	// ESCA-JAVA0139:
	public static void main(String args[]) throws Exception {
		System.setProperty("org.apache.axis2.codegen.config", "/codegen-config.properties");
		System.setProperty("org.apache.adb.properties", "/schema-compile.properties");
		WSDL2Code.main(args);
	}
}
