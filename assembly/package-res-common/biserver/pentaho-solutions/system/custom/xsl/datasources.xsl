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
	<xsl:param name="ds" select="''"/>
	<xsl:param name="mode" select="'list'"/>

	<xsl:template match="/">
	<xsl:variable name="messages" select="msg:getInstance()" />
	<span class="portlet-section-header"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCES_TITLE')" disable-output-escaping="yes"/></span>
	<p/>
	<xsl:if test="$message != ''">
		<span class="portlet-font"><b><xsl:value-of select="$message" disable-output-escaping="yes"/></b></span>
		<p/>
	</xsl:if>

	<xsl:apply-templates/>
	
	</xsl:template>
	
	<xsl:template match="datasources">
	
	<xsl:choose>
		<xsl:when test="$mode='list'">
			<xsl:call-template name="doList"/>
		</xsl:when>
		<xsl:when test="$mode='edit'">
			<xsl:call-template name="doEdit"/>
		</xsl:when>
	</xsl:choose>
	
	</xsl:template>
	
	<xsl:template name="doEdit">
	    <xsl:variable name="messages" select="msg:getInstance()" />
		<xsl:for-each select="container/datasource[@name=$ds]">
		<form id="datasourceedit" method="post">
		<table cellpadding="0" cellspacing="0">
			<tr>
				<td colspan="2" class="portlet-table-header">
					<xsl:value-of select="@name"/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="portlet-font">
					<p/>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_EDIT_HELP')" disable-output-escaping="yes"/>
					<br/><br/>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_URL')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-font">
					<input name="url" id="url" size="50" class="">
						<xsl:attribute name="value"><xsl:value-of select="url"/></xsl:attribute>
					</input>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_DRIVER')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-font">
					<input name="driver" id="driver" size="50" class="">
						<xsl:attribute name="value"><xsl:value-of select="driver"/></xsl:attribute>
					</input>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_TYPE')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-font">
					<xsl:value-of select="type"/>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_USER')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-font">
					<input name="user" id="user" size="50" class="">
						<xsl:attribute name="value"><xsl:value-of select="user"/></xsl:attribute>
					</input>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_PASSWORD')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-font">
					<input name="keeppassword" type="checkbox" checked="true">
						<xsl:attribute name="onclick">if(this.checked) { document.getElementById('oldpwd').disabled=true; document.getElementById('newpwd1').disabled=true; document.getElementById('newpwd2').disabled=true; } else { document.getElementById('oldpwd').disabled=false; document.getElementById('newpwd1').disabled=false; document.getElementById('newpwd2').disabled=false; } </xsl:attribute>
					</input>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SAME_PASSWORD')" disable-output-escaping="yes"/>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
				</td>
				<td class="portlet-font">
					<table>
						<tr>
							<td class="portlet-font">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_OLD_PASSWORD')" disable-output-escaping="yes"/>
							</td>
							<td>
								<input name="oldpwd" id="oldpwd" type="password" size="50" class="" disabled="true"/>
							</td>
						</tr>
						<tr>
							<td class="portlet-font">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_NEW_PASSWORD')" disable-output-escaping="yes"/>
							</td>
							<td>
								<input name="newpwd1" id="newpwd1" type="password" size="50" class="" disabled="true"/>
							</td>
						</tr>
						<tr>
							<td class="portlet-font">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_NEW_PASSWORD_AGAIN')" disable-output-escaping="yes"/>
							</td>
							<td>
								<input name="newpwd2" id="newpwd2" type="password" size="50" class="" disabled="true"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td class="portlet-font">
				</td>
				<td class="portlet-font">
					<button>
						<xsl:attribute name="onclick">document.getElementById('postaction').value='saveedit'; document.getElementById('datasourceedit').submit(); return false;</xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SAVE')" disable-output-escaping="yes"/>
					</button>
					<xsl:text> </xsl:text>
					<button>
						<xsl:attribute name="onclick">document.getElementById('postaction').value='test'; document.getElementById('datasourceedit').target='testpopup'; document.getElementById('datasourceedit').submit(); return false;</xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_TEST')" disable-output-escaping="yes"/>
					</button>
					<xsl:text> </xsl:text>
					<button>
						<xsl:attribute name="onclick">document.getElementById('postaction').value='list'; document.getElementById('datasourceedit').submit(); return false;</xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_CANCEL')" disable-output-escaping="yes"/>
					</button>
					<input type="hidden" id="postaction" name="postaction"/>
					<input type="hidden" name="dsname">
						<xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute>
					</input>
					<br/>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_TEST_HINT')" disable-output-escaping="yes"/>
				</td>
			</tr>
		</table>
		</form>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="doList">
	
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<table cellpadding="0" cellspacing="0">
			<tr>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_NAME')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_STATUS')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_URL')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_ACTIONS')" disable-output-escaping="yes"/>
				</td>
				<!--  td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_PUBLISHER_ACTION')" disable-output-escaping="yes"/>
				</td -->
			</tr>
			<xsl:for-each select="container/datasource">
				<xsl:call-template name="doDatasource"/>
			</xsl:for-each>
			
			
		</table>
		<p/>
		<button>
			<xsl:attribute name="onclick">document.location.href='<xsl:value-of select='$baseUrl'/>action=add';</xsl:attribute>
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_ADD')" disable-output-escaping="yes"/>
		</button>
		<xsl:text> </xsl:text>
		<button>
			<xsl:attribute name="onclick">document.location.href='<xsl:value-of select='$baseUrl'/>';</xsl:attribute>
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_REFRESH')" disable-output-escaping="yes"/>
		</button>


	<p/>
	</xsl:template>

	<xsl:template name="doDatasource">

		<xsl:variable name="messages" select="msg:getInstance()" />

		<tr>
			<td class="portlet-table-text">
				<xsl:value-of select="@name"/>
			</td>
			<td class="portlet-table-text">
				<xsl:call-template name="lookupWebappDatasource">
					<xsl:with-param name="dsName" select="@name"/>
				</xsl:call-template>
			</td>
			<td class="portlet-table-text">
				<xsl:value-of select="url"/>
			</td>
			<td class="portlet-table-text">
				<a class="portlet-font">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>action=edit&amp;ds=<xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_EDIT')" disable-output-escaping="yes"/>
				</a>
				<xsl:text> </xsl:text>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SEPARATOR')" disable-output-escaping="yes"/>
				<xsl:text> </xsl:text>
				<a class="portlet-font">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>action=rename&amp;ds=<xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_RENAME')" disable-output-escaping="yes"/>
				</a>
				<xsl:text> </xsl:text>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SEPARATOR')" disable-output-escaping="yes"/>
				<xsl:text> </xsl:text>
				<a class="portlet-font">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>action=delete&amp;ds=<xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_DELETE')" disable-output-escaping="yes"/>
				</a>
				<xsl:text> </xsl:text>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SEPARATOR')" disable-output-escaping="yes"/>
				<xsl:text> </xsl:text>
				<a class="portlet-font">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>action=copy&amp;ds=<xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_COPY')" disable-output-escaping="yes"/>
				</a>
				<xsl:text> </xsl:text>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_SEPARATOR')" disable-output-escaping="yes"/>
				<xsl:text> </xsl:text>
				<a class="portlet-font" target="testpopup">
					<xsl:attribute name="href"><xsl:value-of select="$baseUrl"/>action=testds&amp;ds=<xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCE_TEST')" disable-output-escaping="yes"/>
				</a>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="lookupWebappDatasource">
		<xsl:variable name="messages" select="msg:getInstance()" />

		<xsl:param name="dsName"/>
		
		<xsl:variable name="ds" select="/datasources/webapp/datasource[@name=$dsName]"/>
		<xsl:choose>
			<xsl:when test="$ds/@name = $dsName">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCES_XSL_CONFIGURED')" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_DATASOURCES_XSL_NOT_CONFIGURED')" disable-output-escaping="yes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>