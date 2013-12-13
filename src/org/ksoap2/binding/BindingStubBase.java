package org.ksoap2.binding;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.AttributeInfo;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.MarshalHashtable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BindingStubBase {

	private static final Logger LOG = Logger.getLogger(BindingStubBase.class);
	protected String nameSpace;
	protected String url;
	protected int soapVersion;
	protected boolean debug = true;


	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String newNameSpace) {
		nameSpace = newNameSpace;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String newUrl) {
		url = newUrl;
	}

	
	public int getSoapVersion() {
		return soapVersion;
	}

	
	public void setSoapVersion(int soapVersion) {
		if (soapVersion == SoapEnvelope.VER10 || soapVersion == SoapEnvelope.VER11 || soapVersion == SoapEnvelope.VER12) {
			this.soapVersion = soapVersion;
		} else {
			this.soapVersion = SoapEnvelope.VER11;
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public BindingStubBase(String url, String nameSpace, int soapVersion, boolean debug) {
		this.nameSpace = nameSpace;
		this.url = url;
		setSoapVersion(soapVersion);
		this.debug = debug;
	}

	protected SoapSerializationEnvelope createSoapSerializationEnvelope() {
		return new CustomSoapSerializationEnvelope(soapVersion);
	}

	protected SoapFault parseSoapFault(HttpTransportSE httpTransport, SoapSerializationEnvelope envelope) {
		if (httpTransport.responseDump != null) {
			StringReader sr = new StringReader(httpTransport.responseDump);
			try {
				KXmlParser kXmlParser = new KXmlParser();
				kXmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
				kXmlParser.setInput(sr);
				envelope.parse(kXmlParser);
			} catch (XmlPullParserException e) {
				LOG.error("Failed to parse XML");
				LOG.debug(httpTransport.responseDump, e);
			} catch (IOException e) {
				LOG.error("Failed to parse XML");
				LOG.debug(httpTransport.responseDump, e);
			}
			sr.close();
			return envelope.bodyIn instanceof SoapFault ? (SoapFault) envelope.bodyIn : null;
		}
		return null;
	}

	protected void logError(HttpTransportSE httpTransport) {
		if (debug) {
			if (httpTransport.requestDump != null) {
				LOG.error(httpTransport.requestDump);
				LOG.error("length=" + httpTransport.requestDump.length());
			}
			if (httpTransport.responseDump != null) {
				LOG.error(httpTransport.responseDump);
				LOG.error("length=" + httpTransport.responseDump.length());
			}
		}
	}

	protected void logInfo(String methodName, Object response) {
		if (debug) {
			LOG.debug(methodName + " reponse: " + String.valueOf(response));
		}
	}

	private static class CustomSoapSerializationEnvelope extends SoapSerializationEnvelope {

	    private static final String TYPE_LABEL = "type";
		private static final MarshalFloat marshalFloat = new MarshalFloat();
		private static final MarshalDate marshalDate = new MarshalDate();
		private static final MarshalHashtable marshalHashtable = new MarshalHashtable();
		private static final MarshalBase64 marshalBase64 = new MarshalBase64();
		private List<Object> multiRef = new ArrayList<Object>();

	    private CustomSoapSerializationEnvelope(int version) {
			super(version);
			implicitTypes = true;
			addAdornments = false;
			marshalFloat.register(this);
			marshalDate.register(this);
			marshalHashtable.register(this);
			marshalBase64.register(this);
		}

		public void writeBody(XmlSerializer writer) throws IOException {
			if (bodyOut != null) {
				multiRef.clear();
				multiRef.add(bodyOut);
				Object[] qName = getInfo(null, bodyOut);
				writer.startTag((dotNet) ? "" : (String) qName[QNAME_NAMESPACE], (String) qName[QNAME_TYPE]);
				if (dotNet) {
					writer.attribute(null, "xmlns", (String) qName[QNAME_NAMESPACE]);
				}
				if (addAdornments) {
					writer.attribute(null, "id", qName[2] == null ? ("o" + 0) : (String) qName[2]);
					writer.attribute(enc, "root", "1");
				}
				writeElement(writer, bodyOut, null, qName[QNAME_MARSHAL]);
				writer.endTag((dotNet) ? "" : (String) qName[QNAME_NAMESPACE], (String) qName[QNAME_TYPE]);
			}
		}

		private void writeElement(XmlSerializer writer, Object element, PropertyInfo type, Object marshal) throws IOException {
			if (marshal != null) {
				((Marshal) marshal).writeInstance(writer, element);
			} else if (element instanceof SoapObject) {
				writeObjectBody(writer, (SoapObject) element);
			} else if (element instanceof SoapObject[]) {
				for (SoapObject soapObject : (SoapObject[]) element) {
					writeObjectBody(writer, soapObject);
				}
			} else if (element instanceof KvmSerializable) {
				writeObjectBody(writer, (KvmSerializable) element);
			} else if (element instanceof KvmSerializable[]) {
				for (KvmSerializable kvmSerializable : (KvmSerializable[]) element) {
					writeObjectBody(writer, kvmSerializable);
				}
			} else if (element instanceof Vector) {
				writeVectorBody(writer, (Vector<?>) element, type.elementType);
			} else {
				throw new RuntimeException("Cannot serialize: " + element);
			}
		}

		public void writeObjectBody(XmlSerializer writer, KvmSerializable object) throws IOException {
			PropertyInfo propertyInfo = new PropertyInfo();
			String namespace;
			String name;
			String type;
			for (int i = 0; i < object.getPropertyCount(); i++) {
				Object property = object.getProperty(i);
				object.getPropertyInfo(i, properties, propertyInfo);
				if (property instanceof SoapObject) {
					SoapObject nestedSoap = (SoapObject) property;
					Object[] qName = getInfo(null, nestedSoap);
					type = (String) qName[QNAME_TYPE];
					if (propertyInfo.name != null && propertyInfo.name.length() > 0) {
						name = propertyInfo.name;
					} else {
						name = (String) qName[QNAME_TYPE];
					}
					if (propertyInfo.namespace != null && propertyInfo.namespace.length() > 0) {
						namespace = propertyInfo.namespace;
					} else {
						namespace = (String) qName[QNAME_NAMESPACE];
					}
					writer.startTag(namespace, name);
					if (!implicitTypes) {
						String prefix = writer.getPrefix(namespace, true);
						writer.attribute(xsi, TYPE_LABEL, prefix + ":" + type);
					}
					writeObjectBody(writer, nestedSoap);
					writer.endTag(namespace, name);
				} else if ((propertyInfo.flags & PropertyInfo.TRANSIENT) == 0) {
					if (property instanceof Object[]) {
						for (Object item : (Object[]) property) {
							if (item != null) {
								writer.startTag(propertyInfo.namespace, propertyInfo.name);
								writeProperty(writer, item, propertyInfo);
								writer.endTag(propertyInfo.namespace, propertyInfo.name);
							}
						}
					} else if (property != null) {
						writer.startTag(propertyInfo.namespace, propertyInfo.name);
						writeProperty(writer, property, propertyInfo);
						writer.endTag(propertyInfo.namespace, propertyInfo.name);
					}
				}
			}
		}

		protected void writeProperty(XmlSerializer writer, Object object, PropertyInfo type) throws IOException {
			if (object == null) {
				writer.attribute(xsi, version >= VER12 ? "nil" : "null", "true");
				return;
			}
			QName qName = null;
			if (object instanceof Enum) {
				object = object.toString();
			} else if (object instanceof QName) {
				qName = (QName) object;
				object = "";
			}
			Object[] info = getInfo(null, object);
			if (type.multiRef || info[2] != null) {
				int i = multiRef.indexOf(object);
				if (i == -1) {
					i = multiRef.size();
					multiRef.add(object);
				}
				writer.attribute(null, "href", info[2] == null ? ("#o" + i) : "#" + info[2]);
			} else {
				if (!implicitTypes || object.getClass() != type.type) {
					String prefix = writer.getPrefix((String) info[QNAME_NAMESPACE], true);
					writer.attribute(xsi, TYPE_LABEL, prefix + ":" + info[QNAME_TYPE]);
				}
				if (object instanceof SoapPrimitive) {
					SoapPrimitive sp = (SoapPrimitive) object;
					for (int i = 0; i < sp.getAttributeCount(); i++) {
						AttributeInfo attributeInfo = new AttributeInfo();
						sp.getAttributeInfo(i, attributeInfo);
						writer.attribute(attributeInfo.getNamespace(), attributeInfo.getName(), attributeInfo.getValue().toString());
					}
					Object value = sp.getValue();
					if (value instanceof Enum) {
						writer.text(value.toString());
					} else if (value instanceof QName) {
						qName = (QName) value;
						String prefix = writer.getPrefix(qName.getNamespaceURI(), true);
						writer.text(prefix + ":" + qName.getLocalPart());
					} else {
						writer.text(object.toString());
					}
				} else {
					if (qName != null) {
						String prefix = writer.getPrefix(qName.getNamespaceURI(), true);
						object = prefix + ":" + qName.getLocalPart();
					}
					writeElement(writer, object, type, info[QNAME_MARSHAL]);
				}
			}
		}

		protected Object readUnknown(XmlPullParser parser, String typeNamespace, String typeName) throws IOException,
				XmlPullParserException {
			String name = parser.getName();
			String namespace = parser.getNamespace();
			List<AttributeInfo> attributeInfoList = new ArrayList<AttributeInfo>();
			for (int i = 0; i < parser.getAttributeCount(); i++) {
				AttributeInfo info = new AttributeInfo();
				info.setName(parser.getAttributeName(i));
				info.setValue(parser.getAttributeValue(i));
				info.setNamespace(parser.getAttributeNamespace(i));
				info.setType(parser.getAttributeType(i));
				attributeInfoList.add(info);
			}
			parser.next();
			Object result = null;
			String text = null;
			if (parser.getEventType() == XmlPullParser.TEXT) {
				text = parser.getText();
				int n = text.indexOf(':');
				if (n >= 0) {
					String namspace = parser.getNamespace(text.substring(0, n));
					if (namspace != null) {
						text = "{" + namspace + "}" + text.substring(n + 1);
					}
				}
				SoapPrimitive sp = new SoapPrimitive(typeNamespace, typeName, text);
				for (int i = 0; i < attributeInfoList.size(); i++) {
					sp.addAttribute(attributeInfoList.get(i));
				}
				result = sp;
				parser.next();
			} else if (parser.getEventType() == XmlPullParser.END_TAG) {
				SoapObject so = new SoapObject(typeNamespace, typeName);
				for (int i = 0; i < attributeInfoList.size(); i++) {
					so.addAttribute(attributeInfoList.get(i));
				}
				result = so;
			}
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				if (text != null && text.trim().length() != 0) {
					throw new RuntimeException("Malformed input: Mixed content");
				}
				SoapObject so = new SoapObject(typeNamespace, typeName);
				for (int i = 0; i < attributeInfoList.size(); i++) {
					so.addAttribute(attributeInfoList.get(i));
				}
				while (parser.getEventType() != XmlPullParser.END_TAG) {
					so.addProperty(parser.getName(), read(parser, so, so.getPropertyCount(), null, null, PropertyInfo.OBJECT_TYPE));
					parser.nextTag();
				}
				result = so;
			}
			parser.require(XmlPullParser.END_TAG, namespace, name);
			return result;
		}
	}
}
