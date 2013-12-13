package org.ksoap2.axis2.wsdl;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.extension.PackageFinder;

public class KSoap2PackageFinder extends PackageFinder {

	public void engage(CodeGenConfiguration configuration) {
		super.engage(configuration);
		configuration.setPackageName(JavaClassWriterUtils.correctPackageName(configuration.getPackageName()));
	}
}
