package org.ksoap2.axis2.wsdl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis2.schema.BeanWriterMetaInfoHolder;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.axis2.schema.SchemaConstants;
import org.apache.axis2.schema.i18n.SchemaCompilerMessages;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.schema.writer.BeanWriter;
import org.apache.axis2.schema.writer.JavaBeanWriter;
import org.apache.axis2.util.FileWriter;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PrettyPrinter;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XSLTTemplateProcessor;
import org.apache.axis2.util.XSLTUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// ESCA-JAVA0166:
@SuppressWarnings("unchecked")
public class KSoap2BeanWriter implements BeanWriter {

	private static final Log log = LogFactory.getLog(KSoap2BeanWriter.class);
	private static final String USELESS_SUFFIX = "_type0";
	private static final Map<QName, String> baseTypeMap = new JavaTypeMap().getTypeMap();
	private final Map<QName, Document> modelMap = new HashMap<QName, Document>();
	private final Map<String, String> namespace2packageNameMap = new HashMap<String, String>();
	private final Map<String, List<String>> packageNameToClassNamesMap = new HashMap<String, List<String>>();
	private final List<String> nameList = new ArrayList<String>();
	private Templates templateCache;
	private File rootDir;
	private String javaBeanTemplateName;
	private String rootPackageName;
	private String mappingClassPackage;
	private boolean writeClasses;
	private boolean templateLoaded;

	public void init(CompilerOptions options) throws SchemaCompilationException {
		modelMap.clear();
		namespace2packageNameMap.clear();
		packageNameToClassNamesMap.clear();
		nameList.clear();
		rootDir = options.getOutputLocation();
		if (rootDir == null) {
			rootDir = new File(".");
		} else if (!rootDir.isDirectory()) {
			throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.rootnotfolderexception"));
		}
		rootPackageName = JavaClassWriterUtils.correctPackageName(options.getPackageName());
		writeClasses = options.isWriteOutput();
		javaBeanTemplateName = SchemaPropertyLoader.getBeanTemplate();
		namespace2packageNameMap.putAll(options.getNs2PackageMap());
		if (options.isMapperClassPackagePresent()) {
			mappingClassPackage = options.getMapperClassPackage();
		} else {
			mappingClassPackage = null;
		}
	}

	public void writeBatch() throws SchemaCompilationException {}

	public Map<QName, Document> getModelMap() {
		return modelMap;
	}

	public String makeFullyQualifiedClassName(QName qName) {
		String namespaceURI = qName.getNamespaceURI();
		String packageName = getPackageName(namespaceURI);
		String originalName = qName.getLocalPart();
		String className = null;
		if (writeClasses) {
			if (!packageNameToClassNamesMap.containsKey(packageName)) {
				packageNameToClassNamesMap.put(packageName, new ArrayList<String>());
			}
			className = makeUniqueJavaClassName(packageNameToClassNamesMap.get(packageName), originalName);
		} else {
			className = makeUniqueJavaClassName(nameList, originalName);
		}
		String packagePrefix = null;
		String fullyQualifiedClassName;
		if (writeClasses) {
			packagePrefix = packageName;
		}
		if (packagePrefix != null) {
			fullyQualifiedClassName = packagePrefix + (packagePrefix.endsWith(".") ? "" : ".") + className;
		} else {
			fullyQualifiedClassName = className;
		}
		// WORKAROUND: exclude suffix _type0 from class name
		return excludeUselessSuffix(fullyQualifiedClassName);
	}

	public String write(QName qName, Map<QName, String> typeMap, Map<QName, String> groupTypeMap, BeanWriterMetaInfoHolder metaInfo,
			boolean isAbstract) throws SchemaCompilationException {
		return process(qName, metaInfo, false);
	}

	public String write(XmlSchemaElement element, Map<QName, String> typeMap, Map<QName, String> groupTypeMap,
			BeanWriterMetaInfoHolder metaInfo) throws SchemaCompilationException {
		return process(element.getQName(), metaInfo, true);
	}

	public String write(XmlSchemaSimpleType simpleType, Map<QName, String> typeMap, Map<QName, String> groupTypeMap,
			BeanWriterMetaInfoHolder metaInfo) throws SchemaCompilationException {
		QName qName = simpleType.getQName();
		if (qName == null) {
			qName = (QName) simpleType.getMetaInfoMap().get(SchemaConstants.SchemaCompilerInfoHolder.FAKE_QNAME);
		}
		metaInfo.addtStatus(qName, SchemaConstants.SIMPLE_TYPE_OR_CONTENT);
		return process(qName, metaInfo, false);
	}

	public String getExtensionMapperPackageName() {
		return mappingClassPackage;
	}

	public void registerExtensionMapperPackageName(String mapperPackageName) {
        mappingClassPackage = mapperPackageName;
	}

	public void writeExtensionMapper(BeanWriterMetaInfoHolder[] metaInfofArray) throws SchemaCompilationException {
		if (!templateLoaded) {
			loadTemplate();
		}
	}

	public String getDefaultClassName() {
		return JavaBeanWriter.DEFAULT_CLASS_NAME;
	}

	public String getDefaultClassArrayName() {
		return JavaBeanWriter.DEFAULT_CLASS_ARRAY_NAME;
	}

	public String getDefaultAttribClassName() {
		return JavaBeanWriter.DEFAULT_ATTRIB_CLASS_NAME;
	}

	public String getDefaultAttribArrayClassName() {
		return JavaBeanWriter.DEFAULT_ATTRIB_ARRAY_CLASS_NAME;
	}

	private String getPackageName(String namespaceURI) {
		String basePackageName = (namespace2packageNameMap != null) && namespace2packageNameMap.containsKey(namespaceURI) ?
			namespace2packageNameMap.get(namespaceURI) : URLProcessor.makePackageName(namespaceURI);
		return JavaClassWriterUtils.correctPackageName(rootPackageName == null ? basePackageName : rootPackageName + basePackageName);
	}

	private static String makeUniqueJavaClassName(List<String> listOfNames, String xmlName) {
		String javaName = JavaUtils.isJavaKeyword(xmlName) ? JavaUtils.makeNonJavaKeyword(xmlName) :
			JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(xmlName));
		listOfNames.add(javaName.toLowerCase());
		return javaName;
	}

	private void loadTemplate() throws SchemaCompilationException {
		if (javaBeanTemplateName != null) {
			try {
				URL xsltUrl = getClass().getResource(javaBeanTemplateName);
				templateCache = TransformerFactory.newInstance().newTemplates(new StreamSource(xsltUrl.toExternalForm()));
				templateLoaded = true;
			} catch (TransformerConfigurationException e) {
				throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.templateLoadException"), e);
			}
		} else {
			throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.templateNotFoundException"));
		}
	}

	private void parse(Document doc, File outputFile) throws TransformerConfigurationException, TransformerException,
			SchemaCompilationException, IOException {
		OutputStream outStream = new FileOutputStream(outputFile);
		XSLTTemplateProcessor.parse(outStream, doc, getTransformer());
		outStream.flush();
		outStream.close();
		PrettyPrinter.prettify(outputFile);
	}

	private Transformer getTransformer() throws TransformerConfigurationException, SchemaCompilationException {
		try {
			return templateCache.newTransformer();
		} catch (TransformerConfigurationException e) {
			loadTemplate();
			return templateCache.newTransformer();
		}
	}

	private boolean isEnum(QName qName, String className) {
		Document model = modelMap.get(new QName(qName.getNamespaceURI(), className));
		return model != null ? "enum".equals(model.getDocumentElement().getNodeName()) : false;
	}

	private static boolean isAny(BeanWriterMetaInfoHolder metaInfo, QName qNname) {
		return metaInfo.getAttributeStatusForQName(qNname) ? metaInfo.getAnyAttributeStatusForQName(qNname) :
			metaInfo.getAnyStatusForQName(qNname);
	}

	private static String excludeUselessSuffix(String str) {
		if (str.endsWith(USELESS_SUFFIX)) {
			str = str.substring(0, str.length() - USELESS_SUFFIX.length());
		}
		return str;
	}

	private String process(QName qName, BeanWriterMetaInfoHolder metaInfo, boolean isElement) throws SchemaCompilationException {
		String fullyQualifiedClassName = metaInfo.getOwnClassName();
		if (fullyQualifiedClassName == null) {
			fullyQualifiedClassName = makeFullyQualifiedClassName(qName);
		}
		int index = fullyQualifiedClassName.lastIndexOf('.');
		String className = fullyQualifiedClassName.substring(index + 1);
		String packageName = index < 0 ? "" : fullyQualifiedClassName.substring(0, index);
		// WORKAROUND: exclude suffix _type0 from original name
		String originalName = excludeUselessSuffix(qName.getLocalPart());
		qName = new QName(qName.getNamespaceURI(), className);
		if (!templateLoaded) {
			loadTemplate();
		}
		if (isElement) {
		}
		if (!JavaClassWriterUtils.isRestrictedType(qName) && !modelMap.containsKey(qName)) {
			String extendedClassName = metaInfo.getClassNameForQName(qName);
			if (extendedClassName == null || (!JavaClassWriterUtils.isPrimitiveType(extendedClassName) &&
					!JavaClassWriterUtils.isWrapperType(extendedClassName))) {
				if (extendedClassName != null) {
					// the element extends another type
					metaInfo.setExtension(true);
					metaInfo.setExtensionClassName(extendedClassName);
					metaInfo.setExtensionBaseType(metaInfo.getSchemaQNameForQName(qName));
					metaInfo.clearTables();
				}
				try {
					Document model = XSLTUtils.getDocument();
					model.appendChild(getBeanElement(model, className, originalName, packageName, qName.getNamespaceURI(), metaInfo));
					if (writeClasses) {
						parse(model, FileWriter.createClassFile(rootDir, packageName, className, ".java"));
					}
					modelMap.put(qName, model);
				} catch (Exception e) {
					throw new SchemaCompilationException(e);
				}
			} else {
				baseTypeMap.put(qName, extendedClassName);
			}
		}
		return fullyQualifiedClassName;
	}

	private Element getBeanElement(Document model, String className, String originalName, String packageName, String nsUir,
			BeanWriterMetaInfoHolder metaInfo) {
		Element root;
		if (metaInfo.isExtension()) {
			root = XSLTUtils.getElement(model, "extendingClass");
		} else if (metaInfo.getEnumFacet().size() > 0) {
			root = XSLTUtils.getElement(model, "enum");
		} else {
			root = XSLTUtils.getElement(model, "class");
		}
		XSLTUtils.addAttribute(model, "name", className, root);
		XSLTUtils.addAttribute(model, "originalName", originalName, root);
		XSLTUtils.addAttribute(model, "package", packageName, root);
		XSLTUtils.addAttribute(model, "nsUri", nsUir, root);
		if (metaInfo.isExtension()) {
			XSLTUtils.addAttribute(model, "extends", metaInfo.getExtensionClassName(), root);
		} else if (metaInfo.getEnumFacet().size() > 0) {
			// enum
			for (String enumValue : metaInfo.getEnumFacet()) {
				XSLTUtils.addChildElement(model, "enumValue", root).setTextContent(enumValue);
			}
		} else {
			// usual class
			int attributeCount = 0;
			int simpleFieldCount = 0;
			QName[] fieldQNames = metaInfo.isOrdered() ? metaInfo.getOrderedQNameArray() : metaInfo.getQNameArray();
			if (metaInfo.isSimple()) {
				// simple type (one property + attributes)
				XSLTUtils.addAttribute(model, "simple", "true", root);
				for (int i = 0; i < fieldQNames.length; ++i) {
					if (isAny(metaInfo, fieldQNames[i])) {
						log.warn("Skip 'anytype' field " + packageName + "." + className + "." + fieldQNames[i].getLocalPart());
					} else {
						Element field = XSLTUtils.addChildElement(model, "field", root);
						XSLTUtils.addAttribute(model, "originalName", i > 0 ? fieldQNames[i].getLocalPart() : "value", field);
						XSLTUtils.addAttribute(model, "name", i > 0 ?
							JavaClassWriterUtils.makeJavaIdentifier(fieldQNames[i].getLocalPart()) : "value", field);
						XSLTUtils.addAttribute(model, "capitalizedName",
							i > 0 ? JavaClassWriterUtils.makeCapitalizedJavaIdentifier(fieldQNames[i].getLocalPart()) : "Value", field);
						String typeName = JavaClassWriterUtils.mapTypeName(metaInfo.getClassNameForQName(fieldQNames[i]));
						XSLTUtils.addAttribute(model, "typeName", typeName, field);
						String baseTypeName = JavaClassWriterUtils.getBaseType(typeName);
						String simpleTypeName = JavaClassWriterUtils.getSimpleClassName(baseTypeName);
						if ("java.util.Calendar".equals(typeName) || "javax.xml.namespace.QName".equals(typeName)) {
							XSLTUtils.addAttribute(model, "wrapperTypeName", simpleTypeName, field);
						}
						if (JavaClassWriterUtils.isPrimitiveType(baseTypeName)) {
							XSLTUtils.addAttribute(model, "primitive", "true", field);
							XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getWrapperClassName(baseTypeName),
								field);
						}
						if (JavaClassWriterUtils.isWrapperType(baseTypeName)) {
							XSLTUtils.addAttribute(model, "wrapperType", "true", field);
							XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getSimpleClassName(baseTypeName),
								field);
						}
						if (isEnum(metaInfo.getSchemaQNameForQName(fieldQNames[i]), simpleTypeName)) {
							XSLTUtils.addAttribute(model, "enum", "true", field);
						}
						if (metaInfo.getAttributeStatusForQName(fieldQNames[i])) {
							// attribute field
							attributeCount++;
							XSLTUtils.addAttribute(model, "attribute",  "true", field);
							if (metaInfo.getOptionalAttributeStatusForQName(fieldQNames[i])) {
								XSLTUtils.addAttribute(model, "optional",  "true", field);
							}
						} else {
							// property field
							if (metaInfo.getArrayStatusForQName(fieldQNames[i])) {
								XSLTUtils.addAttribute(model, "array",  "true", field);
								XSLTUtils.addAttribute(model, "baseTypeName", JavaClassWriterUtils.getBaseType(typeName), field);
							}
							XSLTUtils.addAttribute(model, "nsUri", fieldQNames[i].getNamespaceURI(), field);
							if (metaInfo.getMinOccurs(fieldQNames[i]) == 0) {
								XSLTUtils.addAttribute(model, "optional",  "true", field);
							}
						}
					}
				}
			} else {
				// complex type (properties + attributes)
				for (QName fieldQNname : fieldQNames) {
					if (isAny(metaInfo, fieldQNname)) {
						log.warn("Skip 'anytype' field " + packageName + "." + className + "." + fieldQNname.getLocalPart());
					} else {
						Element field = XSLTUtils.addChildElement(model, "field", root);
						XSLTUtils.addAttribute(model, "originalName", fieldQNname.getLocalPart(), field);
						XSLTUtils.addAttribute(model, "name", JavaClassWriterUtils.makeJavaIdentifier(fieldQNname.getLocalPart()),
							field);
						XSLTUtils.addAttribute(model, "capitalizedName",
							JavaClassWriterUtils.makeCapitalizedJavaIdentifier(fieldQNname.getLocalPart()), field);
						String typeName = JavaClassWriterUtils.mapTypeName(metaInfo.getClassNameForQName(fieldQNname));
						XSLTUtils.addAttribute(model, "typeName", typeName, field);
						String baseTypeName = JavaClassWriterUtils.getBaseType(typeName);
						String simpleTypeName = JavaClassWriterUtils.getSimpleClassName(baseTypeName);
						if ("java.util.Calendar".equals(typeName) || "javax.xml.namespace.QName".equals(typeName)) {
							XSLTUtils.addAttribute(model, "wrapperTypeName", simpleTypeName, field);
						}
						if (JavaClassWriterUtils.isPrimitiveType(baseTypeName)) {
							XSLTUtils.addAttribute(model, "primitive", "true", field);
							XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getWrapperClassName(baseTypeName),
								field);
						}
						if (JavaClassWriterUtils.isWrapperType(baseTypeName)) {
							XSLTUtils.addAttribute(model, "wrapperType", "true", field);
							XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getSimpleClassName(baseTypeName),
								field);
						}
						if (isEnum(metaInfo.getSchemaQNameForQName(fieldQNname), simpleTypeName)) {
							XSLTUtils.addAttribute(model, "enum", "true", field);
						}
						if (metaInfo.getAttributeStatusForQName(fieldQNname)) {
							// attribute field
							attributeCount++;
							XSLTUtils.addAttribute(model, "attribute",  "true", field);
							if (metaInfo.getOptionalAttributeStatusForQName(fieldQNname)) {
								XSLTUtils.addAttribute(model, "optional",  "true", field);
							}
						} else {
							// property field
							if (metaInfo.getArrayStatusForQName(fieldQNname)) {
								XSLTUtils.addAttribute(model, "array",  "true", field);
								XSLTUtils.addAttribute(model, "baseTypeName", baseTypeName, field);
							} else {
								simpleFieldCount++;
							}
							XSLTUtils.addAttribute(model, "nsUri", fieldQNname.getNamespaceURI(), field);
							if (metaInfo.getMinOccurs(fieldQNname) == 0) {
								XSLTUtils.addAttribute(model, "optional",  "true", field);
							}
						}
					}
				}
			}
			XSLTUtils.addAttribute(model, "attributeCount", String.valueOf(attributeCount), root);
			XSLTUtils.addAttribute(model, "simpleFieldCount", String.valueOf(simpleFieldCount), root);
		}
//		System.out.println(com.ibm.wsdl.util.xml.DOM2Writer.nodeToString(root));
		return root;
	}
}
