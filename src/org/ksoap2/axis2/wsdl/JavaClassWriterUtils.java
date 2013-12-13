package org.ksoap2.axis2.wsdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.util.JavaUtils;

public class JavaClassWriterUtils {

	private static String[] PRIMITIVE_TYPES = {"boolean", "int", "long", "short", "byte", "float", "double"};
	private static String[] PRIMITIVE_WRAPPER_TYPES = {"Boolean", "Integer", "Long", "Short", "Byte", "Float", "Double"};
	private static String[] PRIMITIVE_FETCH_VALUES = {"Boolean response = (Boolean) envelope.getResponse();",
		"Integer response = (Integer) envelope.getResponse();", "Long response = (Long) envelope.getResponse();",
		"Short response = (Short) envelope.getResponse();", "Byte response = (Byte) envelope.getResponse();",
		"Float response = (Float) envelope.getResponse();", "Double response = (Double) envelope.getResponse();"};
	private static String[] PRIMITIVE_RETURN_VALUES = {"return response.booleanValue();",
		"return response.intValue();", "return response.longValue();", "return response.shortValue();",
		"return response.byteValue();", "return response.floatValue();", "return response.doubleValue();"};
	private static String[] PRIMITIVE_RETURN_FAULT_VALUES = {"return false;", "return 0;", "return 0;", "return 0;",
		"return 0;", "return 0;", "return 0;"};
	private static final Map<String, String> type2typeMap = new HashMap<String, String>();
	private static final ArrayList<QName> restrictedTypes = new ArrayList<QName>();

	static {
		type2typeMap.put("org.apache.axis2.databinding.types.NCName", "java.lang.String");
		type2typeMap.put("org.apache.axis2.databinding.types.URI", "java.lang.String");
		restrictedTypes.add(QName.valueOf("{http://www.w3.org/2001/XMLSchema}anySimpleType"));
	}

	private JavaClassWriterUtils() {}

	public static String correctPackageName(String packageName) {
		return packageName != null ? packageName.toLowerCase().replace(".www.", ".") : packageName;
	}

	public static String correctClassName(String className) {
		if (className != null) {
			className = className.replace(".www.", ".");
			int index = className.lastIndexOf('.');
			if (index > 0) {
				className = className.substring(0, index).toLowerCase() + className.substring(index);
			}
		}
		return className;
	}

	private static int findPrimitiveTypeIndex(String typeName) {
		for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
			if (PRIMITIVE_TYPES[i].equals(typeName)) {
				return i;
			}
		}
		return -1;
	}

	public static String getWrapperClassName(String typeName) {
		int i = findPrimitiveTypeIndex(typeName);
		if (i != -1) {
			return PRIMITIVE_WRAPPER_TYPES[i];
		} else {
			return null;
		}
	}

	public static boolean isPrimitiveType(String typeName) {
		return findPrimitiveTypeIndex(typeName) != -1;
	}

	public static boolean isArrayType(String typeName) {
		return typeName.endsWith("[]");
	}

	public static boolean isStringType(String typeName) {
		return typeName.equals("java.lang.String");
	}

	public static boolean isWrapperType(String typeName) {
		String shortTypeName = getBaseType(typeName);
		for (String primitiveWrapperTypeName : PRIMITIVE_WRAPPER_TYPES) {
			if (shortTypeName.equals("java.lang." + primitiveWrapperTypeName)) {
				return true;
			}
		}
		return isStringType(shortTypeName);
	}

	public static String getPrimitiveTypeFetchValue(String typeName) {
		int i = findPrimitiveTypeIndex(typeName);
		if (i != -1) {
			return PRIMITIVE_FETCH_VALUES[i];
		} else {
			return "";
		}
	}

	public static String getPrimitiveTypeReturnValue(String typeName, boolean fault) {
		int i = findPrimitiveTypeIndex(typeName);
		if (i != -1) {
			return fault ? PRIMITIVE_RETURN_FAULT_VALUES[i] : PRIMITIVE_RETURN_VALUES[i];
		} else {
			return "";
		}
	}

	public static String getBaseType(String typeName) {
		return typeName.replace("[]", "").trim();
	}

	public static String getSimpleClassName(String className) {
		return className.substring(className.lastIndexOf('.') + 1);
	}

	public static String getPackageName(String className) {
		int i = className.lastIndexOf('.');
		return i > 0 ? className.substring(0, i) : "";
	}

	public static String getLocalName(QName qName) {
		String localName = qName.getLocalPart();
		int index = localName.lastIndexOf('>') + 1;
		return localName.substring(index).replace("_", "");
	}

	public static String makeCapitalizedJavaIdentifier(String xmlName) {
		return JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(xmlName));
	}

	public static String makeJavaIdentifier(String xmlName) {
		xmlName = JavaUtils.xmlNameToJavaIdentifier(xmlName);
		return JavaUtils.isJavaKeyword(xmlName) ? JavaUtils.makeNonJavaKeyword(xmlName) : xmlName;
	}

	public static String makeJavaClassName(String xmlName) {
		xmlName = JavaUtils.xmlNameToJavaIdentifier(xmlName);
		return JavaUtils.isJavaKeyword(xmlName) ? JavaUtils.makeNonJavaKeyword(xmlName) :
			JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(xmlName));
	}

	public static String mapTypeName(String typeName) {
		if (isArrayType(typeName)) {
			String baseTypeName = getBaseType(typeName);
			String newTypeName = type2typeMap.get(baseTypeName);
			if (newTypeName != null) {
				return newTypeName + "[]";
			} else {
				return baseTypeName + "[]";
			}
		} else {
			String newTypeName = type2typeMap.get(typeName);
			if (newTypeName != null) {
				return newTypeName;
			}
			return typeName;
		}
	}

	public static boolean isRestrictedType(QName qName) {
		return restrictedTypes.contains(qName);
	}
}
