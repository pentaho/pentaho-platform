<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>
	<xsl:param name="message" select="''"/>

	<xsl:template match="publishers">
	
	<xsl:variable name="messages" select="msg:getInstance()" />
	
	<!--span class="portlet-section-header" style="border: 1px solid blue;"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_TITLE')" disable-output-escaping="yes"/></span-->
	<xsl:if test="$message != ''">
		<table width="100%" border="0">
		<tr>
			<td align="center"><div class="portlet-font"><xsl:value-of select="$message" disable-output-escaping="yes"/></div></td>
			</tr>
		</table>
	</xsl:if>
		<table class="list_table" cellpadding="0" cellspacing="0">
			<tr>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_NAME')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_DESCRIPTION')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_ACTION')" disable-output-escaping="yes"/>
				</td>
			</tr>
			<xsl:for-each select="publisher">
				<xsl:call-template name="doPublisher"/>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template name="doPublisher">

		<xsl:variable name="messages" select="msg:getInstance()" />

		<tr>
			<td class="portlet-table-text">
				<xsl:value-of select="name"/>
			</td>
			<td class="portlet-table-text">
				<xsl:value-of select="description"/>
			</td>
			<td class="portlet-table-text">
				<a class="portlet-font">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>Publish?publish=now&amp;class=<xsl:value-of select="class"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_PUBLISH')" disable-output-escaping="yes"/>
				</a>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>