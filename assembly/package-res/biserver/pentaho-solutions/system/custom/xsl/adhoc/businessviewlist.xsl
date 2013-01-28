<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	exclude-result-prefixes="html">

    <xsl:variable name="vLowercaseChars_CONST" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="vUppercaseChars_CONST" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

	<xsl:template match="/">
	
		<xsl:for-each select="metadata/message">
			<xsl:value-of select="."/>
		</xsl:for-each>
		
		<xsl:if test="count(metadata/models/model) != 0">
			<table id='businessViewList' width="100%" cellpadding="0" cellspacing="0">
			
				<xsl:for-each select="metadata/models/model">
					<xsl:sort select="translate(./model_name, $vLowercaseChars_CONST , $vUppercaseChars_CONST)"/>
					<xsl:call-template name="doView"/>
				</xsl:for-each>
			
			</table>
		</xsl:if>
	</xsl:template>
                
	<xsl:template name="doView">
		<tr id='{domain_id}_{model_id}' class="unselectedItem" onmousedown="javascript:gCtrlr.handleSelectBusinessView('{domain_id}', '{model_id}');">
			<td>
				<xsl:value-of select="model_name"/>
			</td>
		</tr>
	</xsl:template>
				
</xsl:stylesheet>
