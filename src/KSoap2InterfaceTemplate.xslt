<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no"/>

<xsl:template match="interface">/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL by WSDL2KSoap2 tool.
 */

package <xsl:value-of select="@package"/>;
<xsl:if test="not(@serverSide)">
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
</xsl:if>
public interface <xsl:value-of select="@name"/> {
<xsl:if test="@serverSide">
	String SERVICE_NAME = &quot;<xsl:value-of select="@serviceName"/>&quot;;</xsl:if>
	String NAMESPACE_URI = &quot;<xsl:value-of select="@nsUri"/>&quot;;
<xsl:for-each select="method">
	<xsl:choose>
		<xsl:when test="/interface/@serverSide"><xsl:text>
	</xsl:text>// SOAPAction: <xsl:value-of select="@soapAction"/><xsl:text>
	</xsl:text><xsl:value-of select="output/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:value-of select="input/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="input/@name"/>);
</xsl:when>
		<xsl:otherwise><xsl:text>
	</xsl:text><xsl:value-of select="output/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>(<xsl:value-of select="input/@typeName"/><xsl:text> </xsl:text><xsl:value-of select="input/@name"/>) throws IOException, XmlPullParserException;
</xsl:otherwise>
	</xsl:choose>
</xsl:for-each>}
</xsl:template>
</xsl:stylesheet>
