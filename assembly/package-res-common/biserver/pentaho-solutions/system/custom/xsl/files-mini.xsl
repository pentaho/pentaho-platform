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

	<xsl:template match="repository">

	<xsl:variable name="messages" select="msg:getInstance()" />

	<xsl:call-template name="script"/>

		<xsl:variable name="columns">2</xsl:variable>

		<xsl:call-template name="doHeading">
			<xsl:with-param name="title"><xsl:value-of select="msg:getXslString($messages, 'UI.FILES.BROWSE')" disable-output-escaping="yes"/>&#160;<xsl:value-of select="count(file[@visible='true'][@type='FILE.FOLDER'])"/>&#160;<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.SOLUTIONS')" disable-output-escaping="yes"/></xsl:with-param>
		</xsl:call-template> 
 
	<table border="0" width="100%" class="content_container" cellpadding="0" cellspacing="0">
		<tr>
			<td class="content_body_scroll"  style="margin-left:0px;padding-left:0px"><div class="content_home_scroll" style="left:0px;margin-left:0px">
				<table width="100%" border="0" cellpadding="0" cellspacing="0">	

		<xsl:for-each select="file">
			<xsl:if test="@visible='true'">
				<xsl:if test="@type='FILE.FOLDER'">

					<tr style="cursor:pointer">
							<xsl:attribute name="onclick">var id='contentdiv<xsl:value-of select="position()"/>'; var e=document.getElementById(id); if(e.style.display=='none') { e.style.display='block'; document.getElementById('contentimg<xsl:value-of select="position()"/>').src='/pentaho-style/images/btn_minus.png'; } else { e.style.display='none'; document.getElementById('contentimg<xsl:value-of select="position()"/>').src='/pentaho-style/images/btn_plus.png'; } return false;</xsl:attribute>
						<td class="fileFolderDefault">
							<xsl:attribute name="onmouseover">this.className='fileFolderHover'</xsl:attribute>
							<xsl:attribute name="onmouseout">this.className='fileFolderDefault'</xsl:attribute>
							<img src="/pentaho-style/images/btn_minus.png" border="0">
								<xsl:attribute name="id">contentimg<xsl:value-of select="position()"/></xsl:attribute>
							</img><xsl:text disable-output-escaping="yes"></xsl:text>
							<a href="#"><xsl:value-of select="title"/></a>
						</td>
					</tr>
				
					<tr>
						<td colspan="2" style="padding-left:5px">
							<div>
								<xsl:attribute name="id">contentdiv<xsl:value-of select="position()"/></xsl:attribute>
								<xsl:attribute name="style">display:block</xsl:attribute>

								<table width="100%">

					<xsl:for-each select="file[@visible='true']">
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
							</div>
						</td>
					</tr>

				</xsl:if>
			</xsl:if>
		</xsl:for-each>

				</table></div>
			</td>
		</tr>
	</table>

	</xsl:template>

	<xsl:template name="script">

	<xsl:variable name="messages" select="msg:getInstance()" />

	<xsl:text disable-output-escaping="yes"><![CDATA[
		<script type="text/javascript">
		
		function adminPopup( href, popup, target ) {
			if( popup ) {
				if( !confirm(']]></xsl:text><xsl:value-of select="msg:getXslString($messages, 'UI.FILES.CONFIRM')" disable-output-escaping="yes"/><xsl:text disable-output-escaping="yes"><![CDATA[') ){
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

	</xsl:template>

	<xsl:template name="doHeading">
		<xsl:param name="title"/>

		<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">

			<tr>
				<td width="100%"><xsl:value-of select="$title"/></td>
			</tr>
		</table>

	</xsl:template>
	
	<xsl:template match="files">
	
		<xsl:call-template name="script"/>
	
		<xsl:call-template name="doHeading">
			<xsl:with-param name="title" select="/files/file/title"/>
		</xsl:call-template> 

		<xsl:variable name="columns">2</xsl:variable>

	<table border="0" width="100%" class="content_container" cellpadding="0" cellspacing="0">
		<tr>
			<td class="content_body_scroll"><div class="content_home_scroll">
				<table width="95%" border="0" cellpadding="0" cellspacing="0">	
				
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
				
				</table></div>
			</td>
		</tr>
	</table>
	</xsl:template>

	<xsl:template name="doPublisher">

	<xsl:variable name="title">
		<xsl:choose>
			<xsl:when test="substring(title,2,1)='.' and number(substring(title,1,1))&lt;10">
				<xsl:value-of select="substring(title,3)"/>
			</xsl:when>
			<xsl:when test="substring(title,3,1)='.' and number(substring(title,1,2))&lt;100">
				<xsl:value-of select="substring(title,4)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="title"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
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
				<xsl:otherwise>
					<xsl:text>/pentaho-style/images/btn_systemsettings.png</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

			<td>
				<xsl:attribute name="height"><xsl:value-of select="$height"/></xsl:attribute>
				<a class="home_btn" href="#">
				<div>
					<xsl:attribute name="style">
 						height:32px;padding:5px 5px 0px 43px;margin: 4px 0px 0px 0px; vertical-align: bottom;background-image: url(<xsl:value-of select="$iconUrl"/>);background-repeat: no-repeat;background-position: 0px 0px;</xsl:attribute>
					<xsl:attribute name="onmouseover">this.style.backgroundPosition="0px -<xsl:value-of select="$rolloverY"/>px"</xsl:attribute>
					<xsl:attribute name="onmouseout">this.style.backgroundPosition="0px -0px"</xsl:attribute>
					<xsl:attribute name="onclick">adminPopup('<xsl:value-of select="$url"/>', false, '<xsl:value-of select="$target"/>');return false;</xsl:attribute>
					<xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
					<xsl:value-of select="$title" disable-output-escaping="yes"/>
				</div>
				</a>
			</td>

	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>

