package org.ksoap2.axis2.wsdl;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.emitter.Emitter;
import org.apache.axis2.wsdl.codegen.writer.FileWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// ESCA-JAVA0166:
@SuppressWarnings("unchecked")
public class KSoap2ServiceEmitter implements Emitter {

	private static final Log log = LogFactory.getLog(KSoap2ServiceEmitter.class);
	private static final Map<QName, String> baseTypeMap = new JavaTypeMap().getTypeMap();
	private TypeMapper typeMapper;
	private CodeGenConfiguration codeGenConfiguration;
	private XSLTIncludeResolver resolver;

	public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        codeGenConfiguration = configuration;
        resolver = new XSLTIncludeResolver(codeGenConfiguration);
	}

	public void setMapper(TypeMapper newTypeMapper) {
		typeMapper = newTypeMapper;
	}

	public void emitStub() throws CodeGenerationException {
		try {
			for (AxisService service : codeGenConfiguration.getAxisServices()) {
				Map<String, AxisEndpoint> endpoints = service.getEndpoints();
				for (AxisEndpoint endpoint : endpoints.values()) {
					AxisBinding binding = endpoint.getBinding();
					writeInterface(service, endpoint, binding, codeGenConfiguration.isServerSide());
					if (!codeGenConfiguration.isServerSide()) {
						writeStub(service, endpoint, binding);
					}
				}
			}
		} catch (Exception e) {
			throw new CodeGenerationException(e);
		}
	}

	public void emitSkeleton() throws CodeGenerationException {
		try {
			for (AxisService service : codeGenConfiguration.getAxisServices()) {
				Map<String, AxisEndpoint> endpoints = service.getEndpoints();
				for (AxisEndpoint endpoint : endpoints.values()) {
					AxisBinding binding = endpoint.getBinding();
					writeInterface(service, endpoint, binding, codeGenConfiguration.isServerSide());
				}
			}
		} catch (Exception e) {
			throw new CodeGenerationException(e);
		}
	}

	private void writeFile(Document model, FileWriter writer) throws Exception {
		writer.setOverride(codeGenConfiguration.isOverride());
		writer.loadTemplate();
		String packageName = model.getDocumentElement().getAttribute("package");
		String className = model.getDocumentElement().getAttribute("name");
		writer.createOutFile(packageName, className);
		codeGenConfiguration.addOutputFileName(writer.getOutputFile().getAbsolutePath());
		writer.parse(model, resolver);
	}

	private static File getOutputDirectory(File outputDir, String sourceDir) {
		if (sourceDir != null && sourceDir.length() > 0) {
			outputDir = new File(outputDir, sourceDir);
		}
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			log.warn("Cannot create output directory " + outputDir.getAbsolutePath());
		}
		return outputDir;
	}

	private String mapTypeName(QName qName) {
		String typeName = typeMapper.getTypeMappingName(qName);
		if (typeName == null) {
			typeName = "void";
		}
		String mappedTypeName = baseTypeMap.get(qName);
		return mappedTypeName != null ? mappedTypeName : typeName;
	}

	private static boolean isEnum(QName qName) {
		Document model = (Document) SchemaPropertyLoader.getBeanWriterInstance().getModelMap().get(qName);
		return model != null ? "enum".equals(model.getDocumentElement().getNodeName()) : false;
	}

	private void deleteSourceFile(String typeName) {
		String fileSeparator = System.getProperty("file.separator");
		typeName = typeName.replace(".", fileSeparator) + ".java";
		File sourseFile = new File(codeGenConfiguration.getOutputLocation(), codeGenConfiguration.isFlattenFiles() ? typeName :
			codeGenConfiguration.getSourceLocation() + fileSeparator + typeName);
		if (sourseFile.delete()) {
			// delete empty parent folder
			File parent = sourseFile.getParentFile();
			if (parent.list().length < 1) {
				parent.delete();
			}
		}
	}

	private void writeInterface(AxisService service, AxisEndpoint endpoint, AxisBinding binding, boolean serverSide) throws Exception {
		Document model = XSLTUtils.getDocument();
		Element root = XSLTUtils.getElement(model, "interface");
		XSLTUtils.addAttribute(model, "name", JavaClassWriterUtils.makeJavaClassName(endpoint.getName()), root);
		XSLTUtils.addAttribute(model, "package", codeGenConfiguration.getPackageName(), root);
		XSLTUtils.addAttribute(model, "nsUri", service.getTargetNamespace(), root);
		if (serverSide) {
			XSLTUtils.addAttribute(model, "serverSide", "true", root);
			XSLTUtils.addAttribute(model, "serviceName", service.getName(), root);
		}
		addMethods(model, root, binding, false);
		model.appendChild(root);
//		System.out.println(com.ibm.wsdl.util.xml.DOM2Writer.nodeToString(root));
		InterfaceWriter writer = new InterfaceWriter(codeGenConfiguration.isFlattenFiles() ?
			getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
			getOutputDirectory(codeGenConfiguration.getOutputLocation(), codeGenConfiguration.getSourceLocation()),
			codeGenConfiguration.getOutputLanguage());
		writeFile(model, writer);
	}

	private void writeStub(AxisService service, AxisEndpoint endpoint, AxisBinding binding) throws Exception {
		Document model = XSLTUtils.getDocument();
		Element root = XSLTUtils.getElement(model, "stub");
		XSLTUtils.addAttribute(model, "name", JavaClassWriterUtils.makeJavaClassName(service.getName()), root);
		XSLTUtils.addAttribute(model, "package", codeGenConfiguration.getPackageName(), root);
		XSLTUtils.addAttribute(model, "interface", endpoint.getName(), root);
		addMethods(model, root, binding, true);
		model.appendChild(root);
//		System.out.println(com.ibm.wsdl.util.xml.DOM2Writer.nodeToString(root));
		InterfaceImplementationWriter writer = new InterfaceImplementationWriter(codeGenConfiguration.isFlattenFiles() ?
			getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
			getOutputDirectory(codeGenConfiguration.getOutputLocation(), codeGenConfiguration.getSourceLocation()),
			codeGenConfiguration.getOutputLanguage());
		writeFile(model, writer);
	}

	private void addMethods(Document model, Element root, AxisBinding binding, boolean stub) {
		Iterator<AxisBindingOperation> bindingOperations = binding.getChildren();
		while (bindingOperations.hasNext()) {
			AxisBindingOperation bindingOperation = bindingOperations.next();
			AxisOperation axisOperation = bindingOperation.getAxisOperation();
			String localName = axisOperation.getName().getLocalPart();
			Element method = XSLTUtils.addChildElement(model, "method", root);
			XSLTUtils.addAttribute(model, "name", JavaClassWriterUtils.makeJavaIdentifier(localName), method);
			if (stub) {
				XSLTUtils.addAttribute(model, "originalName", localName, method);
			}
			XSLTUtils.addAttribute(model, "soapAction", axisOperation.getSoapAction(), method);
//			String comment = axisOperation.getDocumentation();
//			if (comment != null) {
//				XSLTUtils.addAttribute(model, "comment", comment.trim(), method);
//			}
			// input parameter
			AxisBindingMessage bindingMessage = (AxisBindingMessage) bindingOperation.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			Element input = XSLTUtils.addChildElement(model, "input", method);
			if (bindingMessage != null) {
				QName qName = bindingMessage.getAxisMessage().getElementQName();
				String typeName = mapTypeName(qName);
				XSLTUtils.addAttribute(model, "typeName", typeName, input);
				XSLTUtils.addAttribute(model, "name", bindingMessage.getAxisMessage().getPartName(), input);
				String baseTypeName = JavaClassWriterUtils.getBaseType(typeName);
				if (JavaClassWriterUtils.isPrimitiveType(baseTypeName)) {
					XSLTUtils.addAttribute(model, "primitive", "true", input);
					XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getWrapperClassName(baseTypeName), input);
				}
				if (JavaClassWriterUtils.isWrapperType(baseTypeName)) {
					XSLTUtils.addAttribute(model, "wrapperType", "true", input);
					XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getSimpleClassName(baseTypeName), input);
				}
				if (isEnum(qName)) {
					XSLTUtils.addAttribute(model, "enum", "true", input);
				}
				if (JavaClassWriterUtils.isArrayType(typeName)) {
					XSLTUtils.addAttribute(model, "array", "true", input);
				}
			} else {
				XSLTUtils.addAttribute(model, "typeName", "void", input);
			}
			// output parameter
			bindingMessage = (AxisBindingMessage) bindingOperation.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			Element output = XSLTUtils.addChildElement(model, "output", method);
			if (bindingMessage != null) {
				QName qName = bindingMessage.getAxisMessage().getElementQName();
				String typeName = mapTypeName(bindingMessage.getAxisMessage().getElementQName());
				XSLTUtils.addAttribute(model, "typeName", typeName, output);
				if (stub) {
					String baseTypeName = JavaClassWriterUtils.getBaseType(typeName);
					if (JavaClassWriterUtils.isPrimitiveType(baseTypeName)) {
						XSLTUtils.addAttribute(model, "primitive", "true", output);
						XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getWrapperClassName(baseTypeName),
							output);
					}
					if (JavaClassWriterUtils.isWrapperType(baseTypeName)) {
						XSLTUtils.addAttribute(model, "wrapperType", "true", output);
						XSLTUtils.addAttribute(model, "wrapperTypeName", JavaClassWriterUtils.getSimpleClassName(baseTypeName),
							output);
					}
					if (isEnum(qName)) {
						XSLTUtils.addAttribute(model, "enum", "true", output);
					}
					if (JavaClassWriterUtils.isArrayType(typeName)) {
						XSLTUtils.addAttribute(model, "array", "true", output);
					}
				}
			} else {
				XSLTUtils.addAttribute(model, "typeName", "void", output);
			}
			// remove fault source files
			for (AxisMessage message : axisOperation.getFaultMessages()) {
				String typeName = mapTypeName(message.getElementQName());
				deleteSourceFile(typeName);
			}
		}
	}
}
