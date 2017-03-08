<?xml version="1.0"?> 
<xsl:stylesheet 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40"
 exclude-result-prefixes="o x ss html">

	<xsl:template name="substituteParameters">
  		<xsl:param name="string" />
  		<xsl:param name="parameterName"/>
  		<xsl:param name="parameterValue"/>

  		<xsl:choose>
		    <xsl:when test="contains($string, $parameterName)">
      			<xsl:value-of select="substring-before($string, $parameterName)" />
      			<xsl:value-of select="$parameterValue" />
	        	<xsl:value-of select="substring(substring-after($string, $parameterName), 1)" />
    		</xsl:when>
    		<xsl:otherwise>
	      		<xsl:value-of select="$string" />
    		</xsl:otherwise>
  		</xsl:choose>
	</xsl:template>
	
<!-- reusable replace-string function -->
 <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="from"/>
    <xsl:param name="to"/>

    <xsl:choose>
      <xsl:when test="contains($text, $from)">

	<xsl:variable name="before" select="substring-before($text, $from)"/>
	<xsl:variable name="after" select="substring-after($text, $from)"/>
	<xsl:variable name="prefix" select="concat($before, $to)"/>

	<xsl:value-of select="$before"/>
	<xsl:value-of select="$to"/>
        <xsl:call-template name="replace-string">
	  <xsl:with-param name="text" select="$after"/>
	  <xsl:with-param name="from" select="$from"/>
	  <xsl:with-param name="to" select="$to"/>
	</xsl:call-template>
      </xsl:when> 
      <xsl:otherwise>
        <xsl:value-of select="$text"/>  
      </xsl:otherwise>
    </xsl:choose>            
 </xsl:template>


	<xsl:template name="breadcrumbing">
		<xsl:param name="crumb1" select="''"/>
		<xsl:param name="url1" select="''"/>
		<xsl:param name="crumb2" select="''"/>
		<xsl:param name="url2" select="''"/>
		<xsl:param name="crumb3" select="''"/>
		<xsl:param name="url3" select="''"/>
		<xsl:param name="crumb4" select="''"/>
		<xsl:param name="url4" select="''"/>

		<table width="100%" class="content_table" border="0" cellpadding="0" cellspacing="0" height="20" style="padding-left: 5px; height: 24px;">
			<tr>
				<td>
					<table>
						<tr>
							<xsl:call-template name="breadcrumb">
								<xsl:with-param name="crumb" select="$crumb1"/>
								<xsl:with-param name="url" select="$url1"/>
							</xsl:call-template>

							<xsl:call-template name="breadcrumb">
								<xsl:with-param name="crumb" select="$crumb2"/>
								<xsl:with-param name="url" select="$url2"/>
							</xsl:call-template>

							<xsl:call-template name="breadcrumb">
								<xsl:with-param name="crumb" select="$crumb3"/>
								<xsl:with-param name="url" select="$url3"/>
							</xsl:call-template>

							<xsl:call-template name="breadcrumb">
								<xsl:with-param name="crumb" select="$crumb4"/>
								<xsl:with-param name="url" select="$url4"/>
							</xsl:call-template>

						</tr>
					</table>
				</td>
  			</tr>
		</table>

	</xsl:template>
	
	<xsl:template name="breadcrumb">
		<xsl:param name="crumb" select="''"/>
		<xsl:param name="url" select="''"/>
	
		<xsl:if test="$crumb!=''">
			<td>
				<div class="icon_folder_sm">
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$url"/></xsl:attribute>
						<xsl:value-of select="$crumb" disable-output-escaping="yes"/>
					</a>
				</div>
			</td>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>

