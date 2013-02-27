<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:template match="/">

		<xsl:for-each select="//repository">
			<table border="0">
				<xsl:for-each select="file">
					<xsl:if test="@visible='true'">
						<xsl:call-template name="doSolution">
						</xsl:call-template>
					</xsl:if>
				</xsl:for-each>
			</table>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="doSolution">

		<xsl:if test="@type='FILE.FOLDER'">
					<xsl:if test="@visible='true'">
			<tr>
				<td style="padding:0px">
					<span style="font: normal 0.85em Tahoma, 'Trebuchet MS', Arial;">
						<xsl:value-of select="title"/>
					</span>
				</td>
			</tr>
			<tr>
				<td style="padding-top:0px;padding-left:20px">
					<table border="0" style="border:0px solid green">
			<xsl:for-each select="file">
					<xsl:if test="@visible='true'">
				<xsl:call-template name="doFile">
				</xsl:call-template>
					</xsl:if>
			</xsl:for-each>
					</table>
				</td>
			</tr>

		</xsl:if>
		</xsl:if>

	</xsl:template>

	<xsl:template name="doFile">

		<xsl:if test="@type='FILE.FOLDER'">
					<xsl:if test="@visible='true'">
			<tr>
				<td style="padding:0px">
					<a title="{solution}/{path}">
						<xsl:attribute name="href">javascript:RepositoryBrowser.selectPath('<xsl:value-of select="solution"/>','<xsl:value-of select="path"/>')</xsl:attribute>

						<xsl:value-of select="title"/>

					</a>
				</td>
			</tr>
			<xsl:if test="count(file[@type='FILE.FOLDER'][@visible='true'])&gt;0">
			<tr>
				<td style="padding-top:0px;padding-left:20px">
					<table border="0" style="border:0px solid red">
			<xsl:for-each select="file">
					<xsl:if test="@visible='true'">
				<xsl:call-template name="doFile">
					<xsl:with-param name="solution" select="solution"/>
				</xsl:call-template>
					</xsl:if>
			</xsl:for-each>
					</table>
				</td>
			</tr>
		</xsl:if>
		</xsl:if>

		</xsl:if>

	</xsl:template>
</xsl:stylesheet>

