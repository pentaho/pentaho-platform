<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="html">

	<xsl:template match="/">
		<xsl:for-each select="metadata/model/view">
			<xsl:call-template name="doView" />
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="doView">

		<xsl:variable name="itemsPer">
			<xsl:value-of select="ceiling(count(column) div 3)" />
		</xsl:variable>

		<div style="display:block">
			<xsl:attribute name="id">folderopen<xsl:value-of select="position()" /></xsl:attribute>

			<!--BEGIN CONTAINER FOR "AVAILABLE ITEMS"-->
			<table width="100%" cellspacing="0" cellpadding="0" class="container_inset_open">
				<xsl:attribute name="id">foldertable<xsl:value-of select="position()" /></xsl:attribute>
				<tr>
					<td class="container_head_left1">
						<img src="images/spacer.png" width="1" height="1" />
					</td>
					<td rowspan="2" nowrap="nowrap" class="container_head_right1" title="{view_description}">
						<a>
							<xsl:attribute name="onclick">
								document.getElementById('folderopen<xsl:value-of select="position()" />').style.display='none';document.getElementById('folderclosed<xsl:value-of select="position()" />').style.display='block';return false;
							</xsl:attribute>
							<img src="images/btn_close.png" width="9" height="9" />
						</a>
						<xsl:text></xsl:text>
						<xsl:value-of select="view_name" />
					</td>
					<td>
						<img src="images/spacer.png" width="1" height="1" />
					</td>
				</tr>
				<tr>
					<td class="container_head_left2">
						<img src="images/spacer.png" width="5" height="10" />
					</td>
					<td class="container_head_right2">
						<img src="images/spacer.png" width="1" height="1" />
					</td>
				</tr>
				<tr>
					<td colspan="3" class="container_content">
						<div class="container_content_scroll">
							<table width="100%">
								<tr>
									<td width="33%" valign="top">
										<!-- the style size above is for resizing the container-->
										<!-- BEGIN CONTENT AREA -->
										<!-- if the total number of items is greater than 0, create a table to hold them -->
										<xsl:if test="count(column) &gt; 0">
											<table id='columnsContainer' name='columnsContainer' width="100%" border='0' cellspacing="0" cellpadding="0" style="font: normal .85em Tahoma, 'trebuchet ms', arial;">
												<xsl:for-each select="column">
													<!-- add the items at index 0 through number of items per column -->
													<xsl:if test="position() &lt;= $itemsPer">
														<xsl:call-template name="createColumn" />
													</xsl:if>
												</xsl:for-each>
											</table>
										</xsl:if>
									</td>
									<td width="33%" valign="top">
										<!-- if the total number of items is greater than the number of items per column, create a table to hold them -->
										<xsl:if test="count(column) &gt; $itemsPer">
											<table id='columnsContainer' name='columnsContainer' width="100%" border="0" cellspacing="0" cellpadding="0" style="font: normal .85em Tahoma, 'trebuchet ms', arial;">
												<xsl:for-each select="column">
													<!-- add the items at index (number of items per column) through ( 2 times number of items per column) -->
													<xsl:if test="position() &gt; $itemsPer">
														<xsl:if test="position() &lt;= ($itemsPer * 2 )">
															<xsl:call-template name="createColumn" />
														</xsl:if>
													</xsl:if>
												</xsl:for-each>
											</table>
										</xsl:if>
									</td>
									<td width="34%" valign="top">
										<!-- if the total number of items is greater than 2 times the number of items per column, create a table to hold them -->
										<xsl:if test="count(column) &gt; ($itemsPer * 2 )">
											<table id='columnsContainer' name='columnsContainer' width="100%" border="0" cellspacing="0" cellpadding="0" style="font: normal .85em Tahoma, 'trebuchet ms', arial;">
												<xsl:for-each select="column">
													<!-- add the items at index (2 times number of items per column) through the total number of items -->
													<xsl:if test="position() &gt; ($itemsPer * 2 )">
														<xsl:call-template name="createColumn" />
													</xsl:if>
												</xsl:for-each>
											</table>
										</xsl:if>
									</td>
								</tr>
							</table>


							<!-- END CONTENT AREA -->
						</div>
					</td>
				</tr>
			</table>
		</div>

		<div style="display:none">
			<xsl:attribute name="id">folderclosed<xsl:value-of select="position()" /></xsl:attribute>

			<table width="100%" border="0" cellspacing="0" cellpadding="0" class="container_inset_closed">
				<tr>
					<td class="container_head_left1">
						<img src="images/spacer.png" width="1" height="1" />
					</td>
					<td rowspan="2" nowrap="nowrap" class="container_head_right1">
						<a>
							<xsl:attribute name="onclick">
								document.getElementById('folderclosed<xsl:value-of select="position()" />').style.display='none';document.getElementById('folderopen<xsl:value-of select="position()" />').style.display='block';return false;
							</xsl:attribute>
							<img src="images/btn_open.png" width="9" height="9" />
						</a>
						<xsl:text></xsl:text>
						<xsl:value-of select="view_name" />
					</td>
					<td>
						<img src="images/spacer.png" width="1" height="1" />
					</td>
				</tr>
				<tr>
					<td class="container_head_left2" style="border-left: 0px solid #bdbcbc;">
						<img src="images/spacer.png" width="5" height="10" />
					</td>
					<td class="container_head_right2" style="border-right: 0px solid #bdbcbc;">
						<img src="images/spacer.png" width="1" height="1" />
					</td>
				</tr>
			</table>
		</div>
		<!--END CONTAINER FOR "AVAILABLE ITEMS"-->

	</xsl:template>

	<xsl:template name="createColumn">
		<tr id="{column_id}" title='{column_description}' class="unselectedItem" onmousedown="gCtrlr.wiz.getPg( 1 ).getAvailableItemsCtrl().handleItemMouseDown( this, event, '{../view_id}', '{column_id}' );" onmouseup="gCtrlr.wiz.getPg( 1 ).getAvailableItemsCtrl().handleItemMouseUp( this, event, '{../view_id}', '{column_id}' );">
			<td>
				<xsl:value-of select="column_name" />
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
