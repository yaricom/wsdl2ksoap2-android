<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no"/>

<xsl:variable name="packageDeclaration">/**
 * <xsl:value-of select="*/@name"/>.java
 *
 * This file was auto-generated from WSDL by WSDL2KSoap2 tool.
 */

package <xsl:value-of select="*/@package"/>;</xsl:variable>

<xsl:template match="enum">
<xsl:value-of select="$packageDeclaration"/>

public enum <xsl:value-of select="@name"/> {

	<xsl:for-each select="enumValue[position() != last()]">
		<xsl:value-of select="text()"/>,
	</xsl:for-each>
	<xsl:value-of select="enumValue[position() = last()]"/>;

	public String value() {
		return name();
	}

	public static <xsl:value-of select="@name"/> fromValue(String value) {
		return valueOf(value);
	}
}
</xsl:template>

<xsl:template match="extendingClass">
<xsl:value-of select="$packageDeclaration"/>

import org.ksoap2.deserialization.Deserializable;
import org.ksoap2.serialization.AttributeContainer;

public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@extends"/> {

	public <xsl:value-of select="@name"/>() {
		super(&quot;<xsl:value-of select="@nsUri"/>&quot;, &quot;<xsl:value-of select="@originalName"/>&quot;);
	}

	protected <xsl:value-of select="@name"/>(String nsUri, String name) {
		super(nsUri, name);
	}

	public void fromSoapResponse(AttributeContainer response) {
		fromSoapResponse(this, response);
	}
}
</xsl:template>

<xsl:template match="class">
<xsl:value-of select="$packageDeclaration"/>

import org.ksoap2.deserialization.*;
import org.ksoap2.deserialization.Deserializable;
import org.ksoap2.serialization.*;
import org.ksoap2.serialization.PropertyInfo;
<xsl:variable name="extends">
	<xsl:choose>
		<xsl:when test="@simple">SoapPrimitive</xsl:when>
		<xsl:otherwise>SoapObject</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
public class <xsl:value-of select="@name"/> extends <xsl:value-of select="$extends"/> implements Deserializable {<xsl:text>
</xsl:text>

<!-- Fields -->
<xsl:if test="@attributeCount > 0">
	private final java.util.ArrayList&lt;AttributeInfo&gt; attributeInfos = new java.util.ArrayList&lt;AttributeInfo&gt;();
</xsl:if>
<xsl:for-each select="field">
	<xsl:variable name="presence">
		<xsl:choose>
			<xsl:when test="@optional">Optional</xsl:when>
			<xsl:otherwise>Mandatory</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="propertyType">
		<xsl:choose>
			<xsl:when test="@attribute">attribute</xsl:when>
			<xsl:otherwise>property</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	/** <xsl:value-of select="$presence"/><xsl:text> </xsl:text><xsl:value-of select="$propertyType"/> */
	private <xsl:value-of select="@typeName"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>;
</xsl:for-each>

<!-- Constructor -->
<xsl:choose>
	<xsl:when test="@simple">
	public <xsl:value-of select="@name"/>() {
		super(&quot;<xsl:value-of select="@nsUri"/>&quot;, &quot;<xsl:value-of select="@originalName"/>&quot;, null);
	}

	protected <xsl:value-of select="@name"/>(String nsUri, String name) {
		super(nsUri, name, null);
	}
</xsl:when>
	<xsl:otherwise>
	public <xsl:value-of select="@name"/>() {
		super(&quot;<xsl:value-of select="@nsUri"/>&quot;, &quot;<xsl:value-of select="@originalName"/>&quot;);
	}

	protected <xsl:value-of select="@name"/>(String nsUri, String name) {
		super(nsUri, name);
	}
</xsl:otherwise>
</xsl:choose>

<!-- Methods -->
<!-- Deserialization methods -->
<xsl:text></xsl:text>
	public void fromSoapResponse(AttributeContainer response) {
		fromSoapResponse(this, response);
	}

	protected void fromSoapResponse(<xsl:value-of select="@name"/> object, AttributeContainer response) {<xsl:choose>
	<xsl:when test="@simple">
		<xsl:for-each select="field">
		<xsl:choose>
			<xsl:when test="@attribute">
				<xsl:choose>
					<xsl:when test="@wrapperTypeName = &quot;Boolean&quot;">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : false);</xsl:when>
					<xsl:when test="@primitive">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : 0);</xsl:when>
					<xsl:when test="@wrapperType">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
					<xsl:when test="@enum">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@typeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
					<xsl:when test="@typeName = &quot;java.util.Calendar&quot; or @typeName = &quot;javax.xml.namespace.QName&quot;">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? KSoap2Utils.get<xsl:value-of select="@wrapperTypeName"/>(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
					<xsl:otherwise>
		// WARNING: Attribute '<xsl:value-of select="@name"/>' of class '<xsl:value-of select="@typeName"/>' cannot be deserialized</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="@primitive or @wrapperType">
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@wrapperTypeName"/>.valueOf(response.toString()));</xsl:when>
					<xsl:when test="@enum">
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@typeName"/>.valueOf(response.toString()));</xsl:when>
					<xsl:when test="@typeName = &quot;java.util.Calendar&quot; or @typeName = &quot;javax.xml.namespace.QName&quot;">
		object.set<xsl:value-of select="@capitalizedName"/>(KSoap2Utils.get<xsl:value-of select="@wrapperTypeName"/>(response.toString()));</xsl:when>
					<xsl:otherwise>
		// WARNING: Property '<xsl:value-of select="@name"/>' cannot be deserialized</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:for-each>
	</xsl:when>
	<xsl:otherwise>
		<xsl:for-each select="field">
			<xsl:choose>
				<xsl:when test="@array">
		java.util.List <xsl:value-of select="@name"/>List = KSoap2Utils.getObjectList((SoapObject) response, &quot;<xsl:value-of select="@originalName"/>&quot;);<xsl:choose>
						<xsl:when test="@primitive or @wrapperType"><xsl:text>
		</xsl:text><xsl:value-of select="@baseTypeName"/>[] <xsl:value-of select="@name"/>Array = new <xsl:value-of select="@baseTypeName"/>[<xsl:value-of select="@name"/>List.size()];
		for (int i = 0; i &lt; <xsl:value-of select="@name"/>Array.length; ++i) {
			<xsl:value-of select="@name"/>Array[i] = <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>List.get(i).toString());
		}
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Array);</xsl:when>
						<xsl:when test="@typeName = &quot;java.util.Calendar&quot; or @typeName = &quot;javax.xml.namespace.QName&quot;"><xsl:text>
		</xsl:text><xsl:value-of select="@baseTypeName"/>[] <xsl:value-of select="@name"/>Array = new <xsl:value-of select="@baseTypeName"/>[<xsl:value-of select="@name"/>List.size()];
		for (int i = 0; i &lt; <xsl:value-of select="@name"/>Array.length; ++i) {
			<xsl:value-of select="@name"/>Array[i] = KSoap2Utils.get<xsl:value-of select="@wrapperTypeName"/>(<xsl:value-of select="@name"/>List.get(i).toString());
		}
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Array);</xsl:when>
						<xsl:when test="@enum"><xsl:text>
		</xsl:text><xsl:value-of select="@baseTypeName"/>[] <xsl:value-of select="@name"/>Array = new <xsl:value-of select="@baseTypeName"/>[<xsl:value-of select="@name"/>List.size()];
		for (int i = 0; i &lt; <xsl:value-of select="@name"/>Array.length; ++i) {
			<xsl:value-of select="@name"/>Array[i] = <xsl:value-of select="@baseTypeName"/>.valueOf(<xsl:value-of select="@name"/>List.get(i).toString());
		}
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Array);</xsl:when>
						<xsl:otherwise><xsl:text>
		</xsl:text><xsl:value-of select="@baseTypeName"/>[] <xsl:value-of select="@name"/>Array = new <xsl:value-of select="@baseTypeName"/>[<xsl:value-of select="@name"/>List.size()];
		for (int i = 0; i &lt; <xsl:value-of select="@name"/>Array.length; ++i) {
			<xsl:value-of select="@name"/>Array[i] = (<xsl:value-of select="@baseTypeName"/>) KSoap2Utils.getObject(new <xsl:value-of select="@baseTypeName"/>(), (AttributeContainer) <xsl:value-of select="@name"/>List.get(i));
		}
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Array);</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="@attribute">
							<xsl:choose>
								<xsl:when test="@wrapperTypeName = &quot;Boolean&quot;">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : false);</xsl:when>
								<xsl:when test="@primitive">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : 0);</xsl:when>
								<xsl:when test="@wrapperType">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@wrapperTypeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
								<xsl:when test="@enum">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@typeName"/>.valueOf(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
								<xsl:when test="@typeName = &quot;java.util.Calendar&quot; or @typeName = &quot;javax.xml.namespace.QName&quot;">
		String <xsl:value-of select="@name"/>Value = KSoap2Utils.getAttribute(response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? KSoap2Utils.get<xsl:value-of select="@wrapperTypeName"/>(<xsl:value-of select="@name"/>Value) : null);</xsl:when>
								<xsl:otherwise>
		// WARNING: Attribute '<xsl:value-of select="@name"/>' of class '<xsl:value-of select="@typeName"/>' cannot be deserialized</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="@primitive or @wrapperType or @typeName = &quot;java.util.Calendar&quot; or @typeName = &quot;javax.xml.namespace.QName&quot;">
		object.set<xsl:value-of select="@capitalizedName"/>(KSoap2Utils.get<xsl:value-of select="@wrapperTypeName"/>(response, &quot;<xsl:value-of select="@originalName"/>&quot;));</xsl:when>
								<xsl:when test="@enum">
		Object <xsl:value-of select="@name"/>Value = KSoap2Utils.getProperty((SoapObject) response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? <xsl:value-of select="@typeName"/>.valueOf(<xsl:value-of select="@name"/>Value.toString()) : null);</xsl:when>
								<xsl:otherwise>
		Object <xsl:value-of select="@name"/>Value = KSoap2Utils.getProperty((SoapObject) response, &quot;<xsl:value-of select="@originalName"/>&quot;);
		object.set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@name"/>Value != null ? (<xsl:value-of select="@typeName"/>) KSoap2Utils.getObject(new <xsl:value-of select="@typeName"/>(), (AttributeContainer) <xsl:value-of select="@name"/>Value) : null);</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:otherwise>
</xsl:choose>
	}
<xsl:text></xsl:text>

<!-- Property serialization methods -->

<xsl:if test="not(@simple)">
	<xsl:variable name="nsUri"><xsl:value-of select="@nsUri"/></xsl:variable>
	<xsl:variable name="propertyCount"><xsl:value-of select="count(field) - @attributeCount"/></xsl:variable>
	public int getPropertyCount() {
		return <xsl:value-of select="$propertyCount"/>;
	}

	public Object getProperty(int index) {<xsl:if test="$propertyCount > 0">
		switch (index) {<xsl:for-each select="field[position() &lt;= $propertyCount]">
		case <xsl:value-of select="position() - 1"/>:<xsl:choose>
			<xsl:when test="@array">
			return <xsl:value-of select="@name"/>;</xsl:when>
			<xsl:when test="@typeName = &quot;java.util.Calendar&quot;">
			return <xsl:value-of select="@name"/> != null ? org.kobjects.isodate.IsoDate.dateToString(<xsl:value-of select="@name"/>.getTime(),
				org.kobjects.isodate.IsoDate.DATE_TIME) : null;</xsl:when>
			<xsl:otherwise>
			return <xsl:value-of select="@name"/>;</xsl:otherwise>
		</xsl:choose>
	</xsl:for-each>
		default:
		}</xsl:if>
		return null;
	}

	public void getPropertyInfo(int index, java.util.Hashtable table, PropertyInfo info) {<xsl:if test="$propertyCount > 0">
		switch (index) {<xsl:for-each select="field[position() &lt;= $propertyCount]">
		case <xsl:value-of select="position() - 1"/>:
			info.name = &quot;<xsl:value-of select="@originalName"/>&quot;;<xsl:choose>
			<xsl:when test="@enum or @typeName = &quot;java.util.Calendar&quot;">
			info.type = java.lang.String.class;</xsl:when>
			<xsl:otherwise>
			info.type = <xsl:value-of select="@typeName"/>.class;</xsl:otherwise>
		</xsl:choose>
			info.namespace = &quot;<xsl:value-of select="$nsUri"/>&quot;;
			break;</xsl:for-each>
		default:
		}</xsl:if>
	}

	public void setProperty(int index, Object object) {}</xsl:if>

<!-- Attribute serialization methods -->
<xsl:if test="@attributeCount > 0">

	public void addAttribute(AttributeInfo attributeInfo) {}

	public void addAttributeIfValue(AttributeInfo attributeInfo) {}

	public int getAttributeCount() {
		return attributeInfos.size();
	}

	public void getAttributeInfo(int index, AttributeInfo attrInfo) {
		attrInfo.setName(attributeInfos.get(index).getName());
		attrInfo.setValue(String.valueOf(attributeInfos.get(index).getValue()));
	}

	public boolean hasAttribute(String name) {
		return getAttributeInfo(name) != null;
	}

	private AttributeInfo getAttributeInfo(String name) {
		for (AttributeInfo attrInfo : attributeInfos) {
			if (attrInfo.getName().equals(name)) {
				return attrInfo;
			}
		}
		return null;
	}</xsl:if>

<!-- Field getters and setters -->
<xsl:for-each select="field">
	<xsl:variable name="prefix">
		<xsl:choose>
			<xsl:when test="@wrapperTypeName = &quot;Boolean&quot;">is</xsl:when>
			<xsl:otherwise>get</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	public <xsl:value-of select="@typeName"/><xsl:text> </xsl:text><xsl:value-of select="$prefix"/><xsl:value-of select="@capitalizedName"/>() {
		return <xsl:value-of select="@name"/>;
	}

	public void set<xsl:value-of select="@capitalizedName"/>(<xsl:value-of select="@typeName"/> newValue) {
		<xsl:value-of select="@name"/> = newValue;<xsl:if test="@attribute">
		AttributeInfo attrInfo = getAttributeInfo(&quot;<xsl:value-of select="@originalName"/>&quot;);
		if (<xsl:value-of select="@name"/> != null) {
			if (attrInfo != null) {
				attrInfo.setValue(String.valueOf(<xsl:value-of select="@name"/>));
			} else {
				attrInfo = new AttributeInfo();
				attrInfo.setName(&quot;<xsl:value-of select="@originalName"/>&quot;);
				attrInfo.setValue(<xsl:value-of select="@name"/>);
				attributeInfos.add(attrInfo);
			}
		} else {
			attributeInfos.remove(attrInfo);
		}</xsl:if>
	}</xsl:for-each>

<!-- toString() method -->
<xsl:if test="count(field) > 0">

	public String toString() {<xsl:choose>
		<xsl:when test="@simple">
		return String.valueOf(value);</xsl:when>
		<xsl:otherwise>
		StringBuilder sb = new StringBuilder(&quot;<xsl:value-of select="@name"/> [&quot;);<xsl:for-each select="field">
				<xsl:choose>
					<xsl:when test="@array">
		sb.append(&quot;<xsl:value-of select="@name"/>=&quot;).append(java.util.Arrays.toString(<xsl:value-of select="@name"/>));</xsl:when>
					<xsl:when test="@typeName = &quot;java.util.Calendar&quot;">
		sb.append(&quot;<xsl:value-of select="@name"/>=&quot;).append(<xsl:value-of select="@name"/> != null ? <xsl:value-of select="@name"/>.getTime() : null);</xsl:when>
					<xsl:otherwise>
		sb.append(&quot;<xsl:value-of select="@name"/>=&quot;).append(<xsl:value-of select="@name"/>);</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="position() != last()">
		sb.append(&quot;, &quot;);</xsl:if>
			</xsl:for-each>
		return sb.append(']').toString();</xsl:otherwise>
	</xsl:choose>
	}</xsl:if>
}
</xsl:template>
</xsl:stylesheet>
