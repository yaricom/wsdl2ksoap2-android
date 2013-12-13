package org.ksoap2.deserialization;

import org.ksoap2.serialization.AttributeContainer;

public interface Deserializable {

	void fromSoapResponse(AttributeContainer response);
}
