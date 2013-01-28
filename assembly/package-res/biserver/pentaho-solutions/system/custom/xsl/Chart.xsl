<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>
    
    <xsl:template match="error">
    	<p/>
		<xsl:value-of disable-output-escaping="yes" select="title"/>
		<br/>
		<xsl:value-of disable-output-escaping="yes" select="message"/>
    </xsl:template>
    
	<xsl:template match="chart">
		<xsl:call-template name="doChart"/>
	</xsl:template>

	<xsl:template name="doChart">
		<center>
			<xsl:value-of select="title"/>
			<br/>
			<!-- Need to select and save the image map -->			
			<xsl:value-of disable-output-escaping="yes" select="imageMap"/>
			<img border="0">
				<xsl:attribute name="width"><xsl:value-of select="width"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="height"/></xsl:attribute>
				<xsl:attribute name="usemap">#<xsl:value-of select="mapName" /></xsl:attribute>
				<xsl:attribute name="src"><xsl:value-of select="$baseUrl"/>getImage?image=<xsl:value-of select="image"/></xsl:attribute>
			</img>
		</center>
	</xsl:template>
</xsl:stylesheet>