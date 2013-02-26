<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
  	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>
	<xsl:param name="message" select="''"/>

	<xsl:template match="user-files">
	
	<xsl:variable name="messages" select="msg:getInstance()" />
	
	<!--span class="portlet-section-header" style="border: 1px solid blue;"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_TITLE')" disable-output-escaping="yes"/></span-->

		<br/>
		<center>
		<table class='content_table' border='0' cellpadding='0' cellspacing='0' height='100%' style="width:99%">
			<tr>
				<td style="text-align:left">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_NEW_CONTENT_INTRO')" disable-output-escaping="yes"/>
					<p/>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_ACTIONS')" disable-output-escaping="yes"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="UserContent?clearAlert=true"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_NEW_CONTENT_CLEAR_ALERT')" disable-output-escaping="yes"/></a>
				</td>
			</tr>
		</table>

       <xsl:for-each select="error">
		<xsl:value-of select="error-message" disable-output-escaping="yes"/>
		<br />
       </xsl:for-each>

		<table class='content_table' border='0' cellpadding='0' cellspacing='0' height='100%' style="width:99%">
			<xsl:apply-templates/>
		</table>

		</center>

	</xsl:template>

	<xsl:template match="scheduled">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<tr>
			<td colspan="10">
				<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">
					<tr>
						<td width="100%" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULED')" disable-output-escaping="yes"/></td>
					</tr>

				</table>
			</td>
		</tr>
		<tr>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_NAME')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SIZE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TYPE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_ACTIONS')" disable-output-escaping="yes"/></td>
		</tr>
		
		<xsl:for-each select="file">
			<tr>
				<td style="text-align:left">
					<xsl:value-of select="name" disable-output-escaping="yes"/>
				</td>
				<td style="text-align:left">
					<xsl:value-of select="timestamp" disable-output-escaping="yes"/>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td style="text-align:left">
					<a>
						<xsl:attribute name="href">
							<xsl:text>UserContent?</xsl:text>
							<xsl:for-each select="actions/action[1]/params/param"><xsl:value-of select="param-name"/>=<xsl:value-of select="param-value"/><xsl:text>&amp;</xsl:text></xsl:for-each>
						</xsl:attribute>
						<xsl:value-of select="actions/action[1]/title" disable-output-escaping="yes"/>
					</a>
				</td>
			</tr>
		</xsl:for-each>

	</xsl:template>

	<xsl:template match="executed">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<tr>
			<td colspan="10">
				<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">
					<tr>
						<td width="100%" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_COMPLETE')" disable-output-escaping="yes"/></td>
					</tr>

				</table>
			</td>
		</tr>
		<tr>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_NAME')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SIZE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TYPE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_ACTIONS')" disable-output-escaping="yes"/></td>
		</tr>
		
		<xsl:for-each select="file">
			<tr>
				<td style="text-align:left">
					<xsl:value-of select="name" disable-output-escaping="yes"/>
				</td>
				<td style="text-align:left">
					<xsl:value-of select="timestamp" disable-output-escaping="yes"/>
				</td>
				<td style="text-align:left">
					<xsl:value-of select="round( number(size) div number('1024') )" disable-output-escaping="yes"/>kb
				</td>
				<td style="text-align:left">
					<xsl:value-of select="mimetype" disable-output-escaping="yes"/>
				</td>
				<td style="text-align:left">
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>GetContent?</xsl:text>
							<xsl:for-each select="actions/action[1]/params/param"><xsl:value-of select="param-name"/>=<xsl:value-of select="param-value"/><xsl:text>&amp;</xsl:text></xsl:for-each>
						</xsl:attribute>
						<xsl:value-of select="actions/action[1]/title" disable-output-escaping="yes"/>
					</a>
					|
					<a>
						<xsl:attribute name="href">
							<xsl:text>UserContent?</xsl:text>
							<xsl:for-each select="actions/action[2]/params/param"><xsl:value-of select="param-name"/>=<xsl:value-of select="param-value"/><xsl:text>&amp;</xsl:text></xsl:for-each>
						</xsl:attribute>
						<xsl:value-of select="actions/action[2]/title" disable-output-escaping="yes"/>
					</a>
				</td>
			</tr>
		</xsl:for-each>

	</xsl:template>
	
	<xsl:template match="text()" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template match="listSubscriptions">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<tr>
			<td colspan="10">
				<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">
					<tr>
						<td width="100%" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.SUBSCRIPTIONS')" disable-output-escaping="yes"/></td>
					</tr>

				</table>
			</td>
		</tr>
		<tr>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_NAME')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left">Schedule/<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SIZE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TYPE')" disable-output-escaping="yes"/></td>
			<td class="portlet-table-header" style="text-align:left"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_ACTIONS')" disable-output-escaping="yes"/></td>
		</tr>
		
		<xsl:for-each select="subscriptions/subscription">
			<tr>
				<td style="text-align:left">
					<xsl:value-of select="action-title" disable-output-escaping="yes"/>
					<br/>
					<xsl:value-of select="title" disable-output-escaping="yes"/>
				</td>
				<td style="text-align:left">
					<xsl:for-each select="schedules/schedule">
						<xsl:value-of select="title" disable-output-escaping="yes"/>
						<br/>
					</xsl:for-each>
				</td>
				<td style="text-align:left">
					---
				</td>
				<td style="text-align:left">
					---
				</td>
				<td style="text-align:left">
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=run&amp;subscribe-name=</xsl:text><xsl:value-of select="@subscriptionId"/>
						</xsl:attribute>
						Run Now
					</a>
					|
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=archive&amp;subscribe-name=</xsl:text><xsl:value-of select="@subscriptionId"/>
						</xsl:attribute>
						Run and Archive
					</a>
					|
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=edit&amp;subscribe-name=</xsl:text><xsl:value-of select="@subscriptionId"/>
						</xsl:attribute>
						Edit
					</a>
					|
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=delete&amp;subscribe-name=</xsl:text><xsl:value-of select="@subscriptionId"/>
						</xsl:attribute>
						Delete
					</a>
				</td>
			</tr>
			
			<xsl:for-each select="subscription/archives/archive">
				<tr>
					<td></td>
					<td style="text-align:left">
						<xsl:value-of select="date" disable-output-escaping="yes"/>
					</td>
					<td style="text-align:right">
                        <xsl:value-of select="size"/>
                    </td>
					<td style="text-align:left"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                        <xsl:value-of select="mimetype"/>
                    </td>
					<td style="text-align:left">
					
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=archived&amp;subscribe-name=</xsl:text><xsl:value-of select="../../../@subscriptionId"/>%3A<xsl:value-of select="id"/>
						</xsl:attribute>
						View
					</a>
					|
					<a target="new">
						<xsl:attribute name="href">
							<xsl:text>ViewAction?subscribe=delete-archived&amp;subscribe-name=</xsl:text><xsl:value-of select="../../../@subscriptionId"/>%3A<xsl:value-of select="id"/>
						</xsl:attribute>
						Delete
					</a>
					
					</td>
				</tr>
			</xsl:for-each>
			
		</xsl:for-each>

	</xsl:template>
	
	<xsl:template match="text()" />

</xsl:stylesheet>