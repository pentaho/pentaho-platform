<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
    xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:output method="html" encoding="UTF-8" />
	<xsl:param name="baseUrl" select="''"/>
	
	<xsl:template match="input-page">

		<xsl:variable name="messages" select="msg:getInstance()" />

		<xsl:variable name="untitled">
			<xsl:value-of select="msg:getString($messages, 'PropertiesPanelUIComponent.USER_UNTITLED')"/>
		</xsl:variable>

		<div id="itemdiv" style="border-right:1px solid #808080;position:absolute;top:0px;left:0px;width:600px;height:520px;overflow:default;padding-left:5px;padding-right:5px">
		<form method="post" id="propform">
			<xsl:attribute name="action"><xsl:value-of select="$baseUrl" />PropertiesPanel?path=<xsl:value-of select="./file-path/text()"/>&amp;action=update</xsl:attribute>
						
			<span class="portlet-subsection-header" style="font-size:0.85em"><xsl:value-of select="msg:getString($messages, 'UI.PROPS_PANEL.FILE')"/> <xsl:value-of select="./display-path/text()"/></span>

			<table width="100%" border="0" cellspacing="0" cellpadding="0" style="padding-top:5px;width:100%">
				<tr>
					<td class="portlet-table-header"><xsl:value-of select="msg:getString($messages, 'UI.PROPS_PANEL.DELETE')"/></td>
					<td class="portlet-table-header"><xsl:value-of select="msg:getString($messages, 'UI.PROPS_PANEL.ROLE')"/></td>
				
					<xsl:for-each select="./permission-names/name">
						<td class="portlet-table-header"><xsl:value-of select="./text()"/></td>
					</xsl:for-each>
				</tr>
				<xsl:for-each select="./ac-list/access-control">
					<xsl:variable name="recipientName" select="./recipient/text()"/>
					<xsl:variable name="recipientType" select="./recipient/@type" />
					<xsl:variable name="pos" select="position()"/>
				    <tr>
				    	<td class="portlet-table-text" style="text-align:center">
				    		<INPUT type="checkbox">
				    			<xsl:attribute name="name">delete_<xsl:value-of select="$recipientName"/></xsl:attribute>
				    		</INPUT>
				    	</td>
				    	<td class="portlet-table-text">
									<xsl:value-of select="$recipientName"/>
									<input type="hidden">
										<xsl:attribute name="name"><xsl:value-of select="$recipientType"/>_<xsl:value-of select="$recipientName"/>_<xsl:value-of select="$pos"/></xsl:attribute>
										<xsl:attribute name="value"><xsl:value-of select="$recipientName"/></xsl:attribute>
									</input>
				    	</td>
				    	<xsl:for-each select="./permission">
				    		<td class="portlet-table-text" style="text-align:center">
							
				    			<INPUT type="checkbox">
				    				<xsl:attribute name="name">perm_<xsl:value-of select="./name/text()"/>_<xsl:value-of select="$pos"/></xsl:attribute>
				    				<xsl:if test="./permitted/text()='true'" >
				    					<xsl:attribute name="checked" />
				    				</xsl:if>
								<!-- xsl:if test="$disable='true'">
									<xsl:attribute name="disabled"><xsl:value-of select="$disable"/></xsl:attribute>
								</xsl:if -->
				    			</INPUT>

				    		</td>
				    	</xsl:for-each>
				    </tr>
		    	</xsl:for-each>

				<tr><td colspan="10">
	    	<xsl:if test="count(./ac-list/access-control) &gt; 0">
				<input name="updateBtn2" id="props-update" type="button">
					<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_UPDATE')"/></xsl:attribute>
					<xsl:attribute name="onclick">document.getElementById('updateBtn').value="yes"; document.forms['propform'].submit() ;return false;</xsl:attribute>
				</input>
				<input name="resetBtn" type="reset">		
					<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_RESET')"/></xsl:attribute>
				</input>
			</xsl:if>
			<input name="addBtn" type="button">
				<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_ADD')"/></xsl:attribute>
				<xsl:attribute name="onclick">document.getElementById('adddiv').style.display='block';</xsl:attribute>
			</input>

			<br />
			<!-- 
			<xsl:if test="./is-directory/text()='true'" >
				<INPUT type="checkbox" name="appy-recursively"/><span class="text"><xsl:value-of select="msg:getString($messages, 'UI.PROPS_PANEL.RECURSE')"/></span>
			</xsl:if>
			-->
				</td></tr>

		</table>
			<input type="hidden" name="updateBtn" id="updateBtn" value=""/>
    	</form>
		</div>

				<xsl:call-template name="addContent">
				</xsl:call-template>

	</xsl:template>

	<xsl:template name="addContent">
	
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<div id="adddiv" style="padding-left:5px;position:absolute;top:0px;left:610px;width:420px;height:500px;overflow:default;display:none">
		
		<form method="post" id="propaddform">

			<span class="portlet-subsection-header" style="font-size:0.85em"><xsl:value-of select="msg:getString($messages, 'UI.USER_PERMISSION_ADD_NEW')"/></span>

		<table width="100%" border="0" style="padding-top:5px">

		<tr>
			<td class="portlet-table-header" colspan="10"><xsl:value-of select="msg:getString($messages, 'UI.PROPS_PANEL.ROLE')"/></td>
		</tr>
		<tr>
			<td colspan="10">
					    	<SELECT class="text" size="23" style="width:410px" multiple="true">
					    		<xsl:attribute name="name">add_name</xsl:attribute>
					    		<xsl:attribute name="id">add_name</xsl:attribute>
								
								<OPTGROUP label="Roles" />
					    		<xsl:for-each select="//recipients/role">
								
					    			<xsl:variable name="aRole" select="./text()"/>
									<xsl:variable name="ok">
										<xsl:for-each select="../../ac-list/access-control">
											<xsl:if test="$aRole=./recipient/text()">no</xsl:if>
										</xsl:for-each>
									</xsl:variable>
									<xsl:if test="$ok=''">
						    			<OPTION>
						    				<xsl:attribute name="value">role_<xsl:value-of select="$aRole" /></xsl:attribute>
						    				<xsl:value-of select="$aRole"/>
						    			</OPTION>
									</xsl:if>
					    		</xsl:for-each>
					    		<OPTGROUP label="Users" />
					    		<xsl:for-each select="//recipients/user">
					    			<xsl:variable name="aUser" select="./text()"/>
									<xsl:variable name="ok">
										<xsl:for-each select="../../ac-list/access-control">
											<xsl:if test="$aUser=./recipient/text()">no</xsl:if>
										</xsl:for-each>
									</xsl:variable>
									<xsl:if test="$ok=''">
						    			<OPTION>
						    				<xsl:attribute name="value">user_<xsl:value-of select="$aUser" /></xsl:attribute>
						    				<xsl:value-of select="$aUser"/>
						    			</OPTION>
									</xsl:if>
					    		</xsl:for-each>
					    	</SELECT>

			</td>
		</tr>
		<tr>
				
			<xsl:for-each select="./permission-names/name">
				<td class="portlet-table-header"><xsl:value-of select="./text()"/></td>
			</xsl:for-each>
		</tr>

		<tr>
					
			<xsl:for-each select="./permission-names/name">
						<td align="center" valign="top">
							<INPUT type="checkbox">
			    				<xsl:attribute name="name">perm_Untitled-0#<xsl:value-of select="./text()"/></xsl:attribute>
							</INPUT>
						</td>
			</xsl:for-each>

		</tr>

		<tr>
			<td colspan="10">

				<xsl:variable name="permCount" select="count(//ac-list/access-control[position()=1]/permission)"/>

			<input name="addBtn2" type="submit">
				<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_ADD')"/></xsl:attribute>
				<xsl:attribute name="onclick">if(document.getElementById('add_name').selectedIndex==-1) { return false; } document.getElementById('name_Untitled-0').value = document.getElementById('add_name').value; return true;</xsl:attribute>
			</input>

				<input name="cancelBtn" type="button">
					<xsl:attribute name="value"><xsl:value-of select="msg:getString($messages, 'UI.USER_CANCEL')"/></xsl:attribute>
					<xsl:attribute name="onclick">document.getElementById('adddiv').style.display='none';</xsl:attribute>
					<!-- xsl:attribute name="onclick">document.getElementById('delete_<xsl:value-of select="$addpos"/>').disabled = false; document.getElementById('delete_<xsl:value-of select="$addpos"/>').value = 'on'; document.getElementById('updateBtn').value="yes"; document.forms['propform'].submit() ;return false;</xsl:attribute -->
				</input>
			</td>
		</tr>
		
				</table>
				<input type="hidden" name="name_Untitled-0" id="name_Untitled-0" value=""/>
				<input type="hidden" name="action" value="update"/>
				<input type="hidden" name="addBtn" value="Add"/>
    	</form>

			</div>
		
	</xsl:template>
	
	<!-- place the text of the error-span-text parameter in a span element with a red-ish background color -->
	<xsl:template name="set-error-span-text">
		<xsl:param name="error-span-text"/>
		<span style='background-color:#FFAAAA'><xsl:value-of select="$error-span-text"/></span>
	</xsl:template>	
	
	<xsl:template match="no-file-path">
		<xsl:call-template name="set-error-span-text">
			<xsl:with-param name="error-span-text"><xsl:value-of select="./text()"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>	
	
	<xsl:template match="set-permissions-denied">
		<xsl:call-template name="set-error-span-text">
			<xsl:with-param name="error-span-text"><xsl:value-of select="./text()"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>	
  
   <xsl:template match="no-acls">
		<xsl:call-template name="set-error-span-text">
			<xsl:with-param name="error-span-text"><xsl:value-of select="./text()"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>