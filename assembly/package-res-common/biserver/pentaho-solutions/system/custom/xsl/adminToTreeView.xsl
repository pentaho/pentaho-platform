<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" xmlns:o="urn:schemas-microsoft-com:office:office"
	xmlns:x="urn:schemas-microsoft-com:office:excel"
	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	xmlns:str_util="http://www.w3.org/2001/10/str-util.xsl"
	exclude-result-prefixes="o x ss html msg str_util">

	<xsl:import href="system/custom/xsl/str-util.xsl" />

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="href" select="''" />
	<xsl:param name="baseUrl" select="''" />
	<xsl:param name="onClick" select="''" />
	<xsl:param name="options" select="''" />
	<xsl:param name="navigate" select="'true'" />
	<xsl:param name="solution" select="''" />
	<xsl:param name="solutionParam" select="'solution'" />
	<xsl:param name="pathParam" select="'path'" />
	<xsl:param name="columns" select="3" />
	<xsl:param name="path" select="''" />
	<xsl:param name="prevPath" select="''" />
	<xsl:param name="levels" select="2" />

	<xsl:template match="files">

		<xsl:variable name="url">
			<xsl:value-of select="$baseUrl" />
			<xsl:text>
				&amp;command=select_solution&amp;solutionid=
			</xsl:text>
		</xsl:variable>
			<table width="99%" border="0">
				<tr>
					<td width="99%">
							<table width="100%" border="0">
								<tr>
									<xsl:for-each
										select="/files/file">
										<xsl:sort select="title" />

										<xsl:if
											test="@visible='true'">
											<xsl:call-template
												name="doTopFolder">
												<xsl:with-param
													name="folder" select="." />
												<xsl:with-param
													name="indent" select="0" />
											</xsl:call-template>
										</xsl:if>
									</xsl:for-each>
								</tr>
							</table>
					</td>
				</tr>
			</table>

	</xsl:template>


	<xsl:template name="doTopFolder">
		<xsl:param name="folder" />
		<xsl:param name="indent" />

		<xsl:if test="@type='FILE.FOLDER' and @visible='true'">

			<tr>
				<td colspan="2">
					<center>
						<table>

							<xsl:for-each
								select="file[@type='FILE.FOLDER' and @visible='true']">
								<xsl:sort select="title" />

								<tr>

								<xsl:call-template name="doEntry">
									<xsl:with-param name="folder"
										select="." />
									<xsl:with-param name="indent"
										select="1" />
									<xsl:with-param name="level"
										select="1" />
								</xsl:call-template>

								</tr>

							</xsl:for-each>
							<xsl:for-each
								select="file[(@type='FILE.ACTIVITY' or @type='FILE.URL') and @visible='true']">
								<xsl:sort select="title" />

								<tr>

								<xsl:call-template name="doEntry">
									<xsl:with-param name="folder"
										select="." />
									<xsl:with-param name="indent"
										select="1" />
									<xsl:with-param name="level"
										select="1" />
								</xsl:call-template>

								</tr>

							</xsl:for-each>
						</table>
					</center>
				</td>
			</tr>

		</xsl:if>

	</xsl:template>

	<xsl:template name="doEntry">
		<xsl:param name="folder" />
		<xsl:param name="indent" />
		<xsl:param name="level" />

		<xsl:choose>
			<xsl:when
				test="@type='FILE.FOLDER' and @visible='true' and $level!=$levels">
			</xsl:when>
			<xsl:when test="@type='FILE.URL'">
				<xsl:call-template name="doUrl">
					<xsl:with-param name="file" select="." />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="doFile">
					<xsl:with-param name="file" select="." />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="doUrl">
		<xsl:param name="file" />
		<!--  xsl:if test="@visible='true'" -->

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>

		<xsl:variable name="icon">
			<xsl:choose>
				<xsl:when test="icon">
					<xsl:value-of select="$href" /><xsl:value-of select="icon" />
				</xsl:when>
				<xsl:when test="@displaytype='url'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>url.png</xsl:text>
				</xsl:when>
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

		<td valign="top">
			<table>
				<tr>
					<td>
						<table>
							<tr>
								<td width="100%">
									<table width="280" cellpadding="4"
										style="border-bottom: 1px solid #cccccc">
										<tr>
											<xsl:if test="icon">
												<td rowspan="2"
													width="60" style="border: 0px solid #cecece"
													valign="top">
													<a>
														<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
														<xsl:attribute
															name="href">
															<xsl:value-of
																select="url" />
														</xsl:attribute>
														<img width="50" height="50"
															border="0">
															<xsl:attribute
																name="src">
																<xsl:value-of
																	select="$icon" />
															</xsl:attribute>
														</img>
													</a>
												</td>
											</xsl:if>
											<td
												style="border: 0px solid #cecece;height:10px;">
												<a class="portlet-section-subheader">
													<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
													<xsl:attribute
														name="href">
														<xsl:value-of
															select="url" />
													</xsl:attribute>
													<xsl:value-of
														select="title" disable-output-escaping="yes" />
												</a>
											</td>
										</tr>
										<tr>
											<td height="100%" class="portlet-font">
												<xsl:value-of
													select="description" disable-output-escaping="yes" />
											</td>
										</tr>
									</table>
								</td>

							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
		<!--  /xsl:if -->
	</xsl:template>

	<xsl:template name="doFile">
		<xsl:param name="file" />
		<!--  xsl:if test="@visible='true'" -->

		<xsl:variable name="popupurl">
			<xsl:value-of select="$href" />
			<xsl:text>ViewAction?</xsl:text>
			<xsl:text>solution=</xsl:text>
			<xsl:value-of select="solution" />
			<xsl:text>&amp;path=</xsl:text>
			<xsl:value-of select="path" />
			<xsl:text>&amp;action=</xsl:text>
			<xsl:value-of select="filename" />
		</xsl:variable>

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>
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

				<xsl:otherwise>
					<xsl:value-of select="$styleBase" />
					<xsl:text>blank-file-type.png</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>


		<td valign="top">
			<table>
				<tr>
					<td>
						<table>
							<tr>
								<td width="100%">
									<table width="280" cellpadding="4"
										style="border-bottom: 1px solid #cccccc">
										<tr>
											<xsl:if test="icon">
												<td rowspan="2"
													width="60" style="border: 0px solid #cecece"
													valign="top">
													<a
														target="pentaho_action">
														<xsl:attribute
															name="href">
															<xsl:value-of
																select="$popupurl" />
														</xsl:attribute>
														<img width="50" height="50"
															border="0">
															<xsl:attribute
																name="src">
																<xsl:value-of
																	select="$icon" />
															</xsl:attribute>
														</img>
													</a>
												</td>
											</xsl:if>
											<td
												style="border: 0px solid #cecece;height:10px;">
												<a
													class="portlet-section-subheader" target="pentaho_action">
													<xsl:attribute
														name="href">
														<xsl:value-of
															select="$popupurl" />
													</xsl:attribute>
													<xsl:value-of
														select="title" disable-output-escaping="yes" />
												</a>
											</td>
										</tr>
										<tr>
											<td height="100%" class="portlet-font">
												<xsl:value-of
													select="description" disable-output-escaping="yes" />
											</td>
										</tr>
									</table>
								</td>

							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
		<!--  /xsl:if -->
	</xsl:template>


	<xsl:template match="text()" />

</xsl:stylesheet>

