<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:o="urn:schemas-microsoft-com:office:office"
 	xmlns:x="urn:schemas-microsoft-com:office:excel"
 	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 	xmlns:html="http://www.w3.org/TR/REC-html40"
 	xmlns:msg="org.pentaho.platform.web.portal.messages.Messages"
	xmlns:str_util="http://www.w3.org/2001/10/str-util.xsl"
 	exclude-result-prefixes="o x ss html msg str_util">

	<xsl:import href="system/custom/xsl/str-util.xsl" />

<xsl:include href="system/custom/xsl/xslUtil.xsl"/>

<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="href" select="''" />
	<xsl:param name="baseUrl" select="''" />
	<xsl:param name="onClick" select="''" />
	<xsl:param name="options" select="''" />
	<xsl:param name="navigate" select="'true'" />
	<xsl:param name="solution" select="''" />
	<xsl:param name="solutionParam" select="'solution'" />
	<xsl:param name="pathParam" select="'path'" />
	<xsl:param name="path" select="''" />
	<xsl:param name="levels" select="2" />


	<xsl:template match="files">

		<div id="flydiv" style="position:absolute;top:-200px;left:-200px">
			<table border="0" cellpadding='0' cellspacing='0'>
				<tr>
					<td width='5' height='9'><img border='0' src="/pentaho-style/images/fly-top-left.png" width='5' height='9'/></td>
					<td colspan='2' style='background-image: url(/pentaho-style/images/fly-top.png);background-repeat: repeat-x;'></td>
					<td><img border='0' src="/pentaho-style/images/fly-top-right.png"/></td>
				</tr>
				<tr>
					<td style='background-image: url(/pentaho-style/images/fly-left.png);background-repeat: repeat-y;'></td>
					<td colspan='2' ><img id="flyimg" border="0" src="/pentaho-style/images/icon_folder_sm.png"/></td>
					<td style='background-image: url(/pentaho-style/images/fly-right.png);background-repeat: repeat-y;'></td>
				</tr>
				<tr>
					<td><img border='0' src="/pentaho-style/images/fly-bot-left.png"/></td>
					<td width='15'><img border='0' src="/pentaho-style/images/fly-bot-2.png"/></td>
					<!-- td style='background-image: url(/pentaho-style/images/fly-bot.png);background-repeat: repeat-x;'></td -->
					<td><img src='/pentaho-style/images/fly-bot.png' height='21' width='100%'/></td>
					<td><img border='0' src="/pentaho-style/images/fly-bot-right.png"/></td>
				</tr>
			</table>
		</div>
		
		<xsl:variable name="title">
			<xsl:value-of select="/files/file/title"/>
		</xsl:variable>

		<xsl:variable name="description">
			<xsl:value-of select="/files/file/description"/>
		</xsl:variable>

		<xsl:variable name="displayType">
			<xsl:choose>
				<xsl:when test="/files/file/@displaytype">
					<xsl:value-of select="/files/file/@displaytype"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>icons</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="columns">
			<xsl:choose>
				<xsl:when test="$displayType='icons'">3</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<table width="980" border="0" cellpadding="0" cellspacing="0">
  			<tr>
    			<td colspan="2" class="portlet-section-header">Name</td>
				<td class="portlet-section-header">Author</td>
	  		</tr>
			<xsl:for-each select="/files/file">
				<xsl:sort select="title" />

				<xsl:if test="@visible='true'">
					<xsl:call-template name="doFolder">
						<xsl:with-param name="columns" select="$columns" />
						<xsl:with-param name="displayType" select="$displayType" />
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
		</table>

	</xsl:template>

	<xsl:template name="doFolder">
		<xsl:param name="columns" />
		<xsl:param name="displayType" />

		<xsl:if test="@type='FILE.FOLDER'">

			<xsl:for-each select="file[@visible='true']">
				<xsl:sort select="title" />

				<xsl:if test="((position()-1) mod number($columns)) = 0">
					<xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
				</xsl:if>

				<xsl:call-template name="doEntry">
					<xsl:with-param name="level" select="1" />
					<xsl:with-param name="columns" select="$columns" />
					<xsl:with-param name="displayType" select="$displayType" />
				</xsl:call-template>

				<xsl:if test="((position()-1) mod number($columns)) = (number($columns))-1">
					<xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
				</xsl:if>
			</xsl:for-each>

		</xsl:if>

	</xsl:template>

	<xsl:template name="doEntry">
		<xsl:param name="level" />
		<xsl:param name="columns" />
		<xsl:param name="displayType" />

		<xsl:choose>
			<xsl:when
				test="@type='FILE.FOLDER' and @visible='true' and $level!=$levels">
				<xsl:variable name="url">
					<xsl:value-of select="$baseUrl" />
					<xsl:value-of select="$solutionParam" />
					<xsl:text>=</xsl:text>
					<xsl:value-of select="solution" />
					<xsl:text>&amp;</xsl:text>
					<xsl:value-of select="$pathParam" />
					<xsl:text>=</xsl:text>
					<xsl:value-of select="path" />
					<xsl:text>&amp;action=</xsl:text>
					<xsl:value-of select="filename" />
				</xsl:variable>
				<xsl:variable name="icon">
					<xsl:if test="icon">
						<xsl:value-of select="$href" />
						<xsl:value-of select="icon" />
					</xsl:if>
				</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="rollovericon">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="rollovericon">
			<xsl:if test="$hasRollover">
				<xsl:value-of select="$href" />
				<xsl:value-of select="rollovericon" />
			</xsl:if>
		</xsl:variable>

			<xsl:call-template name="doItem">
				<xsl:with-param name="title" select="title"/>
				<xsl:with-param name="description" select="description"/>
				<xsl:with-param name="author" select="''"/>
				<xsl:with-param name="url" select="$url"/>
				<xsl:with-param name="target" select="'.'"/>
				<xsl:with-param name="icon" select="$icon"/>
				<xsl:with-param name="rollovericon" select="$rollovericon"/>
				<xsl:with-param name="displayType" select="$displayType"/>
			</xsl:call-template>

			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="doFile">
					<xsl:with-param name="displayType" select="$displayType" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="doFile">
		<xsl:param name="displayType" />

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>

		<xsl:variable name="url">
			<xsl:choose>
    	        <xsl:when test="@type='FILE.URL'">
					<xsl:value-of select="url" />
		    	</xsl:when>
			    <xsl:otherwise>
					<xsl:value-of select="$href" />
					<xsl:text>ViewAction?</xsl:text>
					<xsl:text>solution=</xsl:text>
					<xsl:value-of select="solution" />
					<xsl:text>&amp;path=</xsl:text>
					<xsl:value-of select="path" />
					<xsl:text>&amp;action=</xsl:text>
					<xsl:value-of select="filename" />
			    </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="rollovericon">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="rollovericon">
			<xsl:if test="$hasRollover='true'">
				<xsl:value-of select="$href" />
				<xsl:value-of select="rollovericon" />
			</xsl:if>
		</xsl:variable>

		<xsl:variable name="icon">
			<xsl:choose>
				<xsl:when test="icon">
					<xsl:value-of select="$href" />
					<xsl:value-of select="icon" />
				</xsl:when>
				<xsl:when test="@displaytype='process'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>process.png</xsl:text>
				</xsl:when>
				<xsl:when test="@displaytype='report'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>report.png</xsl:text>
				</xsl:when>
				<xsl:when test="@displaytype='view'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>view.png</xsl:text>
				</xsl:when>
				<xsl:when test="@displaytype='rule'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>rules.png</xsl:text>
				</xsl:when>
				<xsl:when test="@displaytype='url'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>url.png</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$styleBase" />
					<xsl:text>blank-file-type.png</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="target">
			<xsl:choose>
				<xsl:when test="target">
					<xsl:value-of select="target" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>pentaho_action</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:call-template name="doItem">
			<xsl:with-param name="title" select="title"/>
			<xsl:with-param name="description" select="description"/>
			<xsl:with-param name="author" select="author"/>
			<xsl:with-param name="url" select="$url"/>
			<xsl:with-param name="target" select="$target"/>
			<xsl:with-param name="icon" select="$icon"/>
			<xsl:with-param name="rollovericon" select="$rollovericon"/>
			<xsl:with-param name="displayType" select="$displayType"/>
		</xsl:call-template>
		
	</xsl:template>

	<xsl:template name="doItem">
		<xsl:param name="title" />
		<xsl:param name="description" />
		<xsl:param name="author" />
		<xsl:param name="url" />
		<xsl:param name="target" />
		<xsl:param name="icon" />
		<xsl:param name="rollovericon" />
		<xsl:param name="displayType" />

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="$rollovericon=''">false</xsl:when>
				<xsl:otherwise>true</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

	  			<tr>
					<td class="portlet-table-cell" width="43" valign="top">
						<a style="position:relative;">
							<xsl:if test="$target!='.'">
								<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
							</xsl:if>
							<xsl:attribute name="href">
								<xsl:value-of select="$url" />
							</xsl:attribute>
							<img border="0">
								<xsl:attribute name="src">
									<xsl:value-of select="$icon" />
								</xsl:attribute>
							<xsl:if test="$hasRollover='true'">
								<xsl:attribute name="onmouseover">
									<xsl:text>var left=event.clientX+30; var top=event.clientY-this.height-80; var div=document.getElementById('flydiv'); div.style.top=''+top+'px'; div.style.left=''+left+'px'; var img=document.getElementById('flyimg');  img.src='</xsl:text><xsl:value-of select="$rollovericon"/><xsl:text>'</xsl:text>
								</xsl:attribute>
								<xsl:attribute name="onmouseout">
									<xsl:text>var div=document.getElementById('flydiv'); div.style.top='-100px'; div.style.left='-100px'; </xsl:text>
								</xsl:attribute>
							</xsl:if>
							</img>
						</a>
					</td>
  					<td class="portlet-table-cell" >
						<a style="position:relative;">
							<xsl:if test="$target!='.'">
								<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
							</xsl:if>
							<xsl:attribute name="href">
								<xsl:value-of select="$url" />
							</xsl:attribute>
							<xsl:value-of select="$title" disable-output-escaping="yes" />
						</a>
						<div class="list_description">
							<xsl:value-of select="$description" disable-output-escaping="yes" />
						</div>
					</td>
  					<td class="portlet-table-cell"><xsl:value-of select="$author" /></td>
  				</tr>
	</xsl:template>

  	<xsl:template match="text()"/>

</xsl:stylesheet>

