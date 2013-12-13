package org.ksoap2.deserialization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.namespace.QName;

import org.kobjects.isodate.IsoDate;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class KSoap2Utils {

	private KSoap2Utils() {}

	public static String getAttribute(AttributeContainer response, String id) {
		return response.hasAttribute(id) ? response.getAttributeAsString(id) : null;
	}

	public static Object getProperty(SoapObject response, String id) {
		return response.hasProperty(id) ? response.getProperty(id) : null;
	}

	public static int getInteger(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Integer.valueOf(value);
		}
		return 0;
	}

	public static long getLong(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Long.valueOf(value);
		}
		return 0;
	}

	public static short getShort(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Short.valueOf(value);
		}
		return 0;
	}

	public static byte getByte(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Byte.valueOf(value);
		}
		return 0;
	}

	public static float getFloat(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Float.valueOf(value);
		}
		return 0;
	}

	public static double getDouble(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Double.valueOf(value);
		}
		return 0;
	}

	public static boolean getBoolean(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return Boolean.valueOf(value);
		}
		return false;
	}

	public static String getString(Object response, String id) {
		if (response instanceof SoapObject && ((SoapObject) response).hasProperty(id)) {
			Object value = ((SoapObject) response).getProperty(id);
			return value instanceof SoapObject ? "" : String.valueOf(value);
		} else if (response instanceof SoapPrimitive && ((SoapPrimitive) response).getName().equals(id)) {
			return response.toString();
		}
		return null;
	}

	public static Date getDate(String value) {
		return IsoDate.stringToDate(value, IsoDate.DATE_TIME);
	}

	public static Date getDate(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return IsoDate.stringToDate(value, IsoDate.DATE_TIME);
		}
		return new Date();
	}

	public static Calendar getCalendar(String response) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(getDate(response));
		return calendar;
	}

	public static Calendar getCalendar(Object response, String id) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(getDate(response, id));
		return calendar;
	}

	public static QName getQName(String value) {
		return QName.valueOf(value);
	}

	public static QName getQName(Object response, String id) {
		String value = getString(response, id);
		if (value != null && value.length() > 0) {
			return QName.valueOf(value);
		}
		return null;
	}

	public static Deserializable getObject(Deserializable instance, AttributeContainer response) {
		instance.fromSoapResponse(response);
		return instance;
	}

	public static List<Object> getObjectList(SoapObject response, String id) {
		PropertyInfo info = new PropertyInfo();
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < response.getPropertyCount(); i++) {
			response.getPropertyInfo(i, info);
			if (info.getName().equals(id) && info.getValue() != null) {
				list.add(info.getValue());
			}
		}
		return list;
	}
}
