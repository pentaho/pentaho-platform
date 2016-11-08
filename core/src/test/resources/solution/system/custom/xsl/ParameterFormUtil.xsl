<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.util.messages.Messages"
	exclude-result-prefixes="html msg">
    <xsl:include href="system/custom/xsl/html4.xsl"/>
    <xsl:include href="system/custom/xsl/xslUtil.xsl"/>

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template name="doFilters">

		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:variable name="editing">
			<xsl:if test="/filters/input[@name='subscribe-title']/@value!=''">
				<xsl:text>true</xsl:text>
			</xsl:if>
		</xsl:variable>
		
		<html>
			<head>
				<link rel='stylesheet' type='text/css' href='/pentaho-style/pentaho.css' />
				<title><xsl:value-of select="title" disable-output-escaping="yes"/></title>
				<link rel='stylesheet' type='text/css' href='/pentaho-style/active/default.css' />

				<script type="text/javascript" language="javascript" src="/pentaho/js/parameters.js"></script>
				<script type="text/javascript" language="javascript" src="/pentaho/js/subscription.js"></script>

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
				<div style="margin:10px">
					<span class="portlet-section-header"><xsl:value-of select="title" disable-output-escaping="yes"/></span>
				</div>
				<div style="margin:10px;border:1px solid #808080">

					<br/>
					<table border="0" width="100%" >
						<tr>
							<td>
								<span class="portlet-font"><xsl:value-of select="help" disable-output-escaping="yes"/></span>
							</td>
						</tr>

					<tr>
			
					<td class="portlet-font" colspan="2">
						<div style="display:block"> <!-- run2div -->
							<xsl:attribute name="id">run2div<xsl:value-of select="/filters/id" /></xsl:attribute>

							<xsl:choose>
								<xsl:when test="$editing='true'">
									<xsl:attribute name="style">display:block</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="style">display:block</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>

							<form>
								<xsl:if test="$USEPOSTFORFORMS='true'">
									<xsl:attribute name="method">post</xsl:attribute>
									<xsl:attribute name="target">_blank</xsl:attribute>
									<xsl:attribute name="action">/pentaho/ViewAction</xsl:attribute>
								</xsl:if>

								<xsl:attribute name="name">form_<xsl:value-of select="/filters/id" /></xsl:attribute>

								<xsl:call-template name="doSelections" />

								<xsl:for-each select="error">
									<xsl:value-of select="."/>
								</xsl:for-each>

							</form>
                                	</div> <!-- /run2div --> 
					</td>
				</tr>

					<xsl:call-template name="doOptions"/>
					
					</table>					
					<br/>
				</div>
				</body>
		</html>
	</xsl:template>

	<xsl:template name="doFilter">
		<tr>
              <!-- <xsl:element name="br" /> -->
		<td><b><xsl:value-of select="title"/><xsl:text>&#x20;</xsl:text></b></td></tr>
		<tr><td>
		<xsl:for-each select="control">
			<!--  this is important - it copies the definition of the input control into the HTML output -->
	                <xsl:apply-templates/>
		</xsl:for-each>
		</td>
		</tr>
	</xsl:template>

	<xsl:template name="doFilterNoTitle">
		<xsl:for-each select="control">
			<!--  this is important - it copies the definition of the input control into the HTML output -->
	                <xsl:apply-templates/>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="doFilterWithBr">
              <xsl:element name="br" />
		<b><xsl:value-of select="title"/><xsl:text>&#x20;</xsl:text></b>
              <xsl:element name="br" />
		<xsl:for-each select="control">
			<!--  this is important - it copies the definition of the input control into the HTML output -->
	                <xsl:apply-templates/>
		</xsl:for-each>
				
	</xsl:template>


</xsl:stylesheet>
