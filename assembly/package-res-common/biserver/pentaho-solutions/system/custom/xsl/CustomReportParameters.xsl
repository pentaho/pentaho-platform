<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
    xmlns:loc="org.pentaho.platform.util.messages.LocaleHelper"
	exclude-result-prefixes="html msg loc">
	
	<xsl:variable name="USEPOSTFORFORMS" select="'false'" />
    <xsl:include href="system/custom/xsl/html4.xsl"/>
    <xsl:include href="system/custom/xsl/xslUtil.xsl"/>
    
	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template match="filters">
		<xsl:variable name="messages" select="msg:getInstance()" />
		<html>
			<head>
				<link rel='stylesheet' type='text/css' href='/pentaho-style/pentaho.css' />
				<title><xsl:value-of select="title" disable-output-escaping="yes"/></title>
			    <script type="text/javascript" language="javascript" src="../../../js/parameters.js"></script>
				<script type="text/javascript">
					var pentaho_notOptionalMessage = '<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PARAMETER_NOT_OPTIONAL')" disable-output-escaping="yes"/>';
					var pentaho_backgroundWarning = '<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PARAMETER_BACKGROUND_WARNING')" disable-output-escaping="yes"/>';
					var USEPOSTFORFORMS = <xsl:value-of select="$USEPOSTFORFORMS" />;
			        <xsl:for-each select="filter">
						<xsl:if test="@optional = 'true'">
							<xsl:text>pentaho_optionalParams.push('form_</xsl:text><xsl:value-of select="../id"/><xsl:text>.</xsl:text><xsl:value-of select="id"/><xsl:text>');
					</xsl:text>
						</xsl:if>
						<xsl:text>pentaho_paramName["form_</xsl:text><xsl:value-of select="../id"/><xsl:text>.</xsl:text><xsl:value-of select="id"/><xsl:text>"]='</xsl:text>
						<xsl:call-template name="replace-string">
							<xsl:with-param name="text"><xsl:value-of select="title"/></xsl:with-param>
							<xsl:with-param name="from">'</xsl:with-param>
							<xsl:with-param name="to">\'</xsl:with-param>
						</xsl:call-template>
						<xsl:text>';
 					</xsl:text>
						
					</xsl:for-each>
			    </script>
			</head>
			<body>
				<xsl:attribute name="dir"><xsl:value-of select="loc:getTextDirection()"/></xsl:attribute>
				<div style="margin:10px">
					<span class="portlet-section-header"><xsl:value-of select="title" disable-output-escaping="yes"/></span>
				</div>
				<div style="margin:10px;border:1px solid #808080">

					<form>
						<xsl:attribute name="name">form_<xsl:value-of select="/filters/id" /></xsl:attribute>
			
						<table>
							<tr>
								<td>		
									<span class="portlet-font"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_CUSTOM_PARAMETER_HINT')" disable-output-escaping="yes"/></span>
								</td>
							</tr>
							<xsl:for-each select="filter">
								<xsl:call-template name="doFilter">
								</xsl:call-template>
							</xsl:for-each>
							<xsl:for-each select="error">
								<xsl:value-of select="."/>
							</xsl:for-each>
							<tr>
								<td>
									<br/>
									<xsl:if test="/filters/@parameterView='false'">
										<input type="button" name="go" class="portlet-form-button">
											<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_UPDATE')"/></xsl:attribute>
											<xsl:attribute name="onClick">doRun("<xsl:value-of select="/filters/id" />", 'generatedContent?', '<xsl:value-of select="/filters/target"/>', false);</xsl:attribute>
										</input>									
									</xsl:if> 
								</td>
							</tr>
						</table>
	                    <xsl:apply-templates select="input"/>
					</form>
					</div>
				</body>
		</html>
	</xsl:template>

	<xsl:template name="doFilter">
				<xsl:variable name="messages" select="msg:getInstance()" />
				<tr>
					<td class="portlet-section-subheader">		
						<br/><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SELECT')" disable-output-escaping="yes"/><xsl:value-of select="title"/>
					</td>
				</tr>
				<tr>
					<td class="portlet-font">		
						<xsl:for-each select="control">
							<!--  this is important - it copies the definition of the input control into the HTML output -->
		                    <!--  xsl:apply-templates select="*|@*|text()"/ -->
		                    <xsl:apply-templates/>
		                </xsl:for-each>
					</td>
				</tr>
				
	</xsl:template>

</xsl:stylesheet>
