<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	exclude-result-prefixes="html">

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>
    
	<xsl:template match="result">
		<xsl:choose>
			<xsl:when test="@result-type='success'">
				<xsl:call-template name="doSuccess"/>
			</xsl:when>
			<xsl:when test="@result-type='show-input'">
				<xsl:call-template name="doShowInput"/>
			</xsl:when>
			<xsl:when test="@result-type='failed'">
				<xsl:call-template name="doFailure"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="doFailure" >
		<xsl:element name="h2" >
			<xsl:value-of select="./text()" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="doSuccess" >
		<xsl:element name="h2" >
			<xsl:value-of select="./text()" />
		</xsl:element>
		<br />
		<h3>
			<xsl:value-of select="./orphaned-files/orphan-handling/text()"/>
		</h3>
		<xsl:for-each select="./orphaned-files/file-name">
		    <br />
		    <xsl:value-of select="./text()"/>
    	</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="doShowInput">
		<xsl:element name="form" >
			<xsl:attribute name="method">post</xsl:attribute>
 
			<xsl:element name="h2" >
				<xsl:value-of select="./path-title/text()" />
			</xsl:element>
		
			<xsl:element name="input">
				<xsl:attribute name="type">hidden</xsl:attribute>
				<xsl:attribute name="name">path</xsl:attribute>
				<xsl:attribute name="value">Dummy</xsl:attribute>
			</xsl:element>
			
			<xsl:element name="p" />

			<xsl:element name="input">
				<xsl:attribute name="type">checkbox</xsl:attribute>
				<xsl:attribute name="name">delete</xsl:attribute>
				<xsl:value-of select="./delete-title/text()" />
			</xsl:element>
			
			<xsl:element name="p" />
			<xsl:element name="input">
				<xsl:attribute name="type">submit</xsl:attribute>
				<xsl:attribute name="value">Load Repository</xsl:attribute>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>