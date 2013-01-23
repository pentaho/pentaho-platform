<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
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

				<script type="text/javascript" language="javascript" src="../../../js/parameters.js"></script>
				<script type="text/javascript" language="javascript" src="../../../js/pentaho-ajax.js"></script>

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
		      function initialStartup_form_<xsl:value-of select="/filters/id"/>() {
            // Now, focus on first visible input control...
            var form = document.forms['form_<xsl:value-of select="/filters/id" />'];
            if (form) {
              for (i=0; i &lt; form.elements.length; i++) {
                var anElement = form.elements[i];
                if (anElement &amp;&amp; anElement.type &amp;&amp; anElement.type != 'hidden' &amp;&amp; !anElement.disabled) {
                  setTimeout(function(){anElement.focus();}, 5);
                  break;
                }
              }
            }
          }
          			</xsl:for-each>
                    pentaho_optionalParams.push('form_<xsl:value-of select="id"/>.run_as_background');
			    </script>
	    </head>
		<body>
        
				<div style="margin:5px;">

					<table border="0" width="525" >
						
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
									<xsl:attribute name="action">generatedContent?</xsl:attribute>
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
        <script>
		  if (typeof initialStartup_form_<xsl:value-of select="/filters/id"/> !== 'undefined') {
			initialStartup_form_<xsl:value-of select="/filters/id"/>();
		  }
        </script>
				</body>
		</html>
	</xsl:template>

	<xsl:template name="doFilter">
		
		<tr><td style="padding:3px;">
        <table class="parameter_table" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td>
              <fieldset class="parameter_fieldset">
                  <legend>
                      <xsl:value-of select="title"/>
                  </legend>

              
              		<xsl:for-each select="control">
              			<!--  this is important - it copies the definition of the input control into the HTML output -->
              	                <xsl:apply-templates/>
              		</xsl:for-each>

              </fieldset>
            </td>
          </tr>
        </table>
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
