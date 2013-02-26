<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>
	<xsl:param name="urlTarget" select="''"/>
	<xsl:param name="style" select="''"/>
	<xsl:param name="columns" select="2"/>

	<xsl:template match="widgets">
	
		<div>
			<xsl:if test="$style!=''">
				<xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
			</xsl:if>
	
		<table border="0">
	
			<xsl:for-each select="//widget">
				<xsl:if test="((position()-1) mod number($columns)) = 0">
					<xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
				</xsl:if>
				<td valign="top">
					<xsl:call-template name="doWidget"/>
				</td>
				<xsl:if test="((position()-1) mod number($columns)) = number($columns)-1">
					<xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</table>
		</div>
	</xsl:template>

	<xsl:template match="widget">
		<xsl:call-template name="doWidget"/>
	</xsl:template>

	<xsl:template name="doWidget">
	<xsl:variable name="messages" select="msg:getInstance()" />
	<center>
		<span class="portlet-section-subheader"><xsl:value-of select="title"/></span>
		<br/>
		<xsl:choose>
		<xsl:when test="urlDrill != ''"> 
			<a>
				<xsl:attribute name="href"><xsl:value-of select="urlDrill"/></xsl:attribute>
				<xsl:attribute name="target"><xsl:value-of select="$urlTarget"/></xsl:attribute>
				<xsl:attribute name="title"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_DRILL_HINT')" disable-output-escaping="yes"/><xsl:value-of select="title"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="width"><xsl:value-of select="width"/></xsl:attribute>
					<xsl:attribute name="height"><xsl:value-of select="height"/></xsl:attribute>
					<xsl:attribute name="src"><xsl:value-of select="$baseUrl"/>getImage?image=<xsl:value-of select="image"/></xsl:attribute>
				</img>
			</a>
		</xsl:when>
		<xsl:otherwise>
				<img border="0">
					<xsl:attribute name="width"><xsl:value-of select="width"/></xsl:attribute>
					<xsl:attribute name="height"><xsl:value-of select="height"/></xsl:attribute>
					<xsl:attribute name="src"><xsl:value-of select="$baseUrl"/>getImage?image=<xsl:value-of select="image"/></xsl:attribute>
				</img>
		</xsl:otherwise>
		</xsl:choose>
		<br/>
		<xsl:if test="value/@in-image='false'">
			<span class="portlet-font"><xsl:value-of select="format-number(number(value), '0.##')"/></span>
		</xsl:if>
	</center>	
	</xsl:template>
</xsl:stylesheet>