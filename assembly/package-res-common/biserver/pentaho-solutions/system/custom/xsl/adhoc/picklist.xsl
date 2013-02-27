<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:template match="/">

			<table border="0">
				<xsl:for-each select="metadata/data/COLUMN-HDR-ROW">
					<tr>
						<xsl:for-each select="COLUMN-HDR-ITEM">
							<td style="border-bottom:2px solid #808080;font-weight:bold;font-style:italic">
								<xsl:value-of select="."/>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
				<xsl:for-each select="metadata/data/DATA-ROW">
					<tr>
						<xsl:for-each select="DATA-ITEM">
							<td style="border-bottom:1px solid #cccccc">
								<xsl:choose>
									<xsl:when test=".=''">
										<a>
											<xsl:attribute name="href">javascript:ColumnValuePickList.setValue('')</xsl:attribute>
											[missing]
										</a>
									</xsl:when>
									<xsl:otherwise>
										<a>
											<xsl:attribute name="href">javascript:ColumnValuePickList.setValue('<xsl:value-of select="."/>')</xsl:attribute>
											<xsl:value-of select="."/>
										</a>
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</table>

	</xsl:template>

</xsl:stylesheet>

