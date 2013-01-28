<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

    <xsl:include href="system/custom/xsl/html4.xsl"/>

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template match="filters">
	
		<xsl:text disable-output-escaping="yes">
			&lt;script&gt;
			  var url</xsl:text><xsl:value-of select="/filters/id" />=unescape('<xsl:value-of select="action" disable-output-escaping="yes"/><xsl:text disable-output-escaping="yes">');
			  function doForm</xsl:text><xsl:value-of select="/filters/id" /><xsl:text disable-output-escaping="yes">() {
			  
			    var submitUrl = url</xsl:text><xsl:value-of select="/filters/id" /><xsl:text disable-output-escaping="yes">;
			    var form = document.forms['form</xsl:text><xsl:value-of select="/filters/id" /><xsl:text disable-output-escaping="yes">'];
			    var elements = form.elements;
			    var i;
			    var amp;
			    if (submitUrl.indexOf("?") != -1) {	    	
			    	if ((submitUrl.lastIndexOf("?") == (submitUrl.length-1)) || (submitUrl.lastIndexOf('&amp;') == (submitUrl.length-1))) {
			    		amp = "";
			    	} else {
			    		amp = '&amp;';
			    	}
			    } else {
			    	amp = "?";
			    }
			    for( i=0; i&lt;elements.length; i++ ) {
			      if( elements[i].type == 'select-one' || elements[i].type == 'text' || elements[i].type == 'hidden') {
  			        submitUrl += amp + elements[ i ].name + '=' + escape( elements[ i ].value );
   				      amp = '&amp;';
  			      } else if( elements[i].type == 'radio' ) {
  			      	if( elements[i].checked ) {
  			          submitUrl += amp + elements[ i ].name + '=' + escape( elements[ i ].value );
   				      amp = '&amp;';
  			      	}
  			      } else if( elements[i].type == 'checkbox' ) {
  			      	if( elements[i].checked ) {
				      submitUrl += amp + elements[i].name + "=" + escape( elements[i].value );
   				      amp = '&amp;';
  			      	}
  			      } else if( elements[i].type == 'select-multiple' ) {
				    var options = elements[i].options;
				    var j;
				    for( j=0; j!=options.length; j++ ) {
				      if( options[j].selected ) {
	  			        submitUrl += amp + elements[i].name + '=' + escape( options[ j ].value );
   				      amp = '&amp;';
				      }
				    }
				  }
			    }
			    document.location.href=submitUrl;
			    return false;
			  }
			&lt;/script&gt;
		</xsl:text>

		<form>
			<xsl:attribute name="name">form<xsl:value-of select="/filters/id" /></xsl:attribute>
			<xsl:text disable-output-escaping="yes"></xsl:text>
			<table>
				<xsl:for-each select="filter">
					<xsl:call-template name="doFilter">
						<xsl:with-param name="formName" select="/filters/id"/>
					</xsl:call-template>
				</xsl:for-each>
				<xsl:for-each select="error">
					<xsl:value-of select="."/>
				</xsl:for-each>
			</table>
		</form>

	</xsl:template>

	<xsl:template name="doFilter">
		<xsl:variable name="messages" select="msg:getInstance()" />
		<xsl:param name="formName"/>

				<tr>
					<td class="portlet-section-subheader">		
                        <br/><xsl:value-of select="title" disable-output-escaping="yes"/>
					</td>
				</tr>
				<tr>
					<td class="portlet-font">		
						<xsl:for-each select="control">
							<!--  this is important - it copies the definition of the input control into the HTML output -->
		                    <xsl:apply-templates/>
		                </xsl:for-each>
					</td>
				</tr>
		
		<xsl:if test="position()=last()">
				<tr>
					<td>
						<br/>
						<input type="button" name="go" class="portlet-form-button">
							<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_UPDATE')" disable-output-escaping="yes"/></xsl:attribute>
							<xsl:attribute name="onClick">doForm<xsl:value-of select="$formName" />()</xsl:attribute>
						</input>
					</td>
				</tr>
		</xsl:if>
		
	</xsl:template>


</xsl:stylesheet>