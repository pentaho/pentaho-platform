<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:o="urn:schemas-microsoft-com:office:office"
 	xmlns:x="urn:schemas-microsoft-com:office:excel"
 	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 	xmlns:html="http://www.w3.org/TR/REC-html40"
 	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
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
	
	<xsl:text disable-output-escaping="yes"><![CDATA[
		<script type="text/javascript">
		
		function adminPopup( href, popup, target ) {
			/* WARNING the following code is a HUGE hack... we need to come up with a way for a URL/XACTION to carry it's
			   own prompt/error/sucess messages
			*/		
			if (href.indexOf("/ResetRepository") != -1 ) {
				confirmMsg = 'Warning: Are you sure you want to restore the RDBMS repository ' +
				'from the server and reset all permissions? Any permissions that have been defined for folders ' + 
				'on the server will be deleted, along with any files that were published after the last repository ' +
				'load. This cannot be undone.'
				if (!confirm(confirmMsg)) {
					return;
				}
			}
		    /* end WARNING */
		    
			if( popup ) {
				if( !confirm('Do you really want to do this?') ){
					return;
				}
			}
			var opts = "";
			if( popup ) {
				opts = "width=350,height=200,toolbar=no,scrollbars=yes,status=no,resizable=no";
			} else {
				opts = "width=780,height=380,toolbar=no,scrollbars=yes,status=no,resizable=yes";
			}
			opts = "";
			if( target == '.' ) {
				window.location.href = href;
			} else {
				window.open( href, "admin_popup"+popup, opts );
			}
		}
		
		</script>
	]]></xsl:text>
	
	<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">

		<tr>
			<td width="100%"><xsl:value-of select="/files/file/title"/></td>
		</tr>
	</table>

		<xsl:variable name="columns">2</xsl:variable>

	<table border="0" width="100%" class="content_container2" cellpadding="0" cellspacing="0">
		<tr>
			<td class="content_body">
				<table width="100%" border="0" cellpadding="0" cellspacing="0">	
				
					<xsl:for-each select="file/file[@visible='true']">
						<xsl:sort select="title" />					
							
						<xsl:if test="((position()-1) mod number($columns)) = 0">
							<xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
						</xsl:if>
						<xsl:call-template name="doPublisher"/>
						<xsl:if test="((position()-1) mod number($columns)) = (number($columns))-1">
							<xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
						</xsl:if>
					</xsl:for-each>
				
				</table>
			</td>
		</tr>
	</table>
	</xsl:template>

	<xsl:template name="doPublisher">
	
		<xsl:variable name="url">
			<xsl:choose>
				<xsl:when test="url!=''">
					<xsl:value-of select="url"/>
				</xsl:when>
				<xsl:when test="@type='FILE.FOLDER'">Navigate?solution=<xsl:value-of select="solution"/>&amp;path=<xsl:value-of select="path"/></xsl:when>
				
				<xsl:otherwise>ViewAction?solution=<xsl:value-of select="solution"/>&amp;path=&amp;action=<xsl:value-of select="filename"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xsl:variable name="height">
			<xsl:choose>
				<xsl:when test="@type='FILE.FOLDER'">40</xsl:when>
				<xsl:otherwise>32</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
		<xsl:variable name="target">
			<xsl:choose>
				<xsl:when test="@type='FILE.FOLDER'">.</xsl:when>
				<xsl:when test="target!=''"><xsl:value-of select="target"/></xsl:when>
				<xsl:otherwise>new</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="rolloverY">
			<xsl:choose>
				<xsl:when test="@type='FILE.FOLDER'">463</xsl:when>
				<xsl:otherwise>32</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="iconUrl">
			<xsl:choose>
				<xsl:when test="icon!=''">
					<xsl:value-of select="icon"/>
				</xsl:when>
				<xsl:when test="class='org.pentaho.plugin.shark.SharkPublisher'">
					<xsl:text>/pentaho-style/images/btn_shark.png</xsl:text>
				</xsl:when>
				<xsl:when test="class='org.pentaho.platform.engine.services.solution.SolutionPublisher'">
					<xsl:text>/pentaho-style/images/btn_solutionrepos.png</xsl:text>
				</xsl:when>
				<xsl:when test="class='org.pentaho.platform.engine.core.system.SettingsPublisher'">
					<xsl:text>/pentaho-style/images/btn_systemsettings.png</xsl:text>
				</xsl:when>
				<xsl:when test="class='org.pentaho.platform.engine.core.system.GlobalListsPublisher'">
					<xsl:text>/pentaho-style/images/btn_globalactions.png</xsl:text>
				</xsl:when>
                <xsl:when test="class='org.pentaho.platform.engine.services.metadata.MetadataPublisher'">
                    <xsl:text>/pentaho-style/images/btn_refreshmetadata.png</xsl:text>
                </xsl:when>
				<xsl:otherwise>
					<xsl:text>/pentaho-style/images/btn_systemsettings.png</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

			<!-- td width="50%">
				<xsl:attribute name="height"><xsl:value-of select="$height"/></xsl:attribute>
				<a class="home_btn" href="#">
				<div>
					<xsl:attribute name="style">
 cursor: pointer;height:24px;padding:10px 5px 0px 43px;margin: 4px 0px 0px 0px; vertical-align: bottom;background-image: url(<xsl:value-of select="$iconUrl"/>);background-repeat: no-repeat;background-position: 0px 0px;white-space: nowrap;</xsl:attribute>
					<xsl:attribute name="onmouseover">this.style.backgroundPosition="0px -<xsl:value-of select="$rolloverY"/>px"</xsl:attribute>
					<xsl:attribute name="onmouseout">this.style.backgroundPosition="0px -0px"</xsl:attribute>
					<xsl:attribute name="onclick">adminPopup('<xsl:value-of select="$url"/>', false, '<xsl:value-of select="$target"/>');return false;</xsl:attribute>
					<xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
					<xsl:value-of select="title" disable-output-escaping="yes"/>
				</div>
				</a>
			</td -->

			<td width="50%">
				<div>
					<xsl:attribute name="style">
 border:0px;height:29px;overflow:none;padding:2px 0px 0px 6px;margin: 4px 0px 0px 0px; vertical-align: middle;background-image: url(<xsl:value-of select="$iconUrl"/>);background-repeat: no-repeat;background-position: 0px 0px;white-space: nowrap;</xsl:attribute>
					<xsl:attribute name="onmouseover">this.style.backgroundPosition="0px -32px"</xsl:attribute>
					<xsl:attribute name="onmouseout">this.style.backgroundPosition="0px -0px"</xsl:attribute>

				<a class="home_btn" href="#">
					<table border="0" cellpadding="0" cellspacing="0" height="30">
						<tr>
							<td width="30">
								<xsl:attribute name="onclick">adminPopup('<xsl:value-of select="$url"/>', false, '<xsl:value-of select="$target"/>');return false;</xsl:attribute>
							</td>
							<td>
								<xsl:attribute name="onclick">adminPopup('<xsl:value-of select="$url"/>', false, '<xsl:value-of select="$target"/>');return false;</xsl:attribute>
								<xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
								<xsl:value-of select="title" disable-output-escaping="yes"/>
							</td>
						</tr>
					</table>
				</a>
				</div>
			</td>

	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>

