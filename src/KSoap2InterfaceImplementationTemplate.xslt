<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no"/>

<xsl:template match="stub">/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL by WSDL2KSoap2 tool.
 */

package <xsl:value-of select="@package"/>;

import java.io.IOException;
import org.ksoap2.SoapFault;
import org.ksoap2.binding.BindingStubBase;
import org.ksoap2.deserialization.KSoap2Utils;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public class <xsl:value-of select="@name"/> extends BindingStubBase implements <xsl:value-of select="@interface"/> {

	public <xsl:value-of select="@name"/>(String url, int soapVersion, boolean debug) {
		super(url, NAMESPACE_URI, soapVersion, debug);
	}
<xsl:for-each select="method">
	<xsl:variable name="input">
		<xsl:choose>
			<xsl:when test="input/@typeName = &quot;void&quot;"></xsl:when>
			<xsl:otherwise><xsl:value-of select="input/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="input/@name"/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	public <xsl:value-of select="output/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:value-of select="$input"/>) throws IOException, XmlPullParserException {
		SoapSerializationEnvelope envelope = createSoapSerializationEnvelope();<xsl:if test="input/@typeName != &quot;void&quot;">
		envelope.bodyOut = <xsl:value-of select="input/@name"/>;</xsl:if>
		HttpTransportSE httpTransport = new HttpTransportSE(url);
		httpTransport.debug = debug;
		try {
			httpTransport.call(&quot;<xsl:value-of select="@soapAction"/>&quot;, envelope);
			if (envelope.bodyIn instanceof SoapFault) {
				throw (SoapFault) envelope.bodyIn;
			}<xsl:choose>
	<xsl:when test="output/@typeName = &quot;void&quot;"></xsl:when>
	<xsl:when test="output/@primitive"><xsl:text>
			</xsl:text><xsl:value-of select="output/@wrapperTypeName"/> response = <xsl:value-of select="output/@wrapperTypeName"/>.valueOf(envelope.bodyIn.toString());
			logInfo(&quot;<xsl:value-of select="@originalName"/>&quot;, response);
			return response;</xsl:when>
	<xsl:when test="output/@enum or output/@typeName = &quot;java.util.Calendar&quot; or output/@typeName = &quot;javax.xml.namespace.QName&quot;"><xsl:text>
			</xsl:text><xsl:value-of select="output/@typeName"/> response = <xsl:value-of select="output/@typeName"/>.valueOf(envelope.bodyIn.toString());
			logInfo(&quot;<xsl:value-of select="@originalName"/>&quot;, response);
			return response;</xsl:when>
	<xsl:otherwise><xsl:text>
			</xsl:text><xsl:value-of select="output/@typeName"/> response = (<xsl:value-of select="output/@typeName"/>) KSoap2Utils.getObject(new <xsl:value-of select="output/@typeName"/>(), (AttributeContainer) envelope.bodyIn);
			logInfo(&quot;<xsl:value-of select="@originalName"/>&quot;, response);
			return response;</xsl:otherwise>
</xsl:choose>
		} catch (IOException e) {
			logError(httpTransport);
			SoapFault soapFault = parseSoapFault(httpTransport, envelope);
			throw soapFault != null ? soapFault : e;
		} catch (XmlPullParserException e) {
			logError(httpTransport);
			throw e;
		}
	}
</xsl:for-each>}
</xsl:template>
</xsl:stylesheet>
