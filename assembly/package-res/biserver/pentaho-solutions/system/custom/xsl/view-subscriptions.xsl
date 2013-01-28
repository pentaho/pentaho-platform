<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:html="http://www.w3.org/TR/REC-html40" xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages" exclude-result-prefixes="html msg">

	<xsl:output method="html" />

	<xsl:template match="/subscriptions">

		<xsl:variable name="messages" select="msg:getInstance()" />

		<table border="0">

			<xsl:if test="@valid-session='true'">

				<xsl:if test="count(subscription)>0">
					<!--  this is the subscribe section of the parameter page -->
					<tr>
						<td colspan="3">
							<table width="100%">
								<tr>
									<td class="portlet-font" nowrap="true">
										<xsl:value-of select="msg:getString($messages, 'UI.VIEW_SUBS.MY_VIEWS')" />
									</td>
									<td>
										<select id="subscription">
											<xsl:for-each select="subscription">
												<xsl:call-template name="subscription" />
											</xsl:for-each>
										</select>
									</td>
									<td class="portlet-font">
										<select>
											<xsl:attribute name="id">subscription-action</xsl:attribute>
											<option value="load">Load</option>
											<option value="delete">Delete</option>
										</select>
									</td>
									<td>
										<input type="button" name="go" class="portlet-form-button">
											<xsl:attribute name="value">Go</xsl:attribute>
											<xsl:attribute name="onClick">doSubscribed()</xsl:attribute>
										</input>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</xsl:if>
			</xsl:if>
		</table>
	</xsl:template>


	<xsl:template name="subscription">
		<option>
			<xsl:attribute name="value">
				<xsl:value-of select="id" />
			</xsl:attribute>
			<xsl:value-of select="title" />
		</option>
	</xsl:template>

</xsl:stylesheet>
