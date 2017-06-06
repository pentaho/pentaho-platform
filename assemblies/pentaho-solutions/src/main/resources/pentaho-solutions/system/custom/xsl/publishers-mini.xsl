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

	<xsl:template match="publishers">
	
	<xsl:variable name="messages" select="msg:getInstance()" />
	
	<xsl:text disable-output-escaping="yes"><![CDATA[
		<script type="text/javascript">
		
		function publishPopup( href, popup ) {
			if( popup ) {
				if( !confirm(']]></xsl:text><xsl:value-of select="msg:getXslString($messages, 'UI.FILES.CONFIRM')" disable-output-escaping="yes"/><xsl:text disable-output-escaping="yes"><![CDATA[') ){
					return;
				}
			}
			var opts = "";
			if( popup ) {
				opts = "width=350,height=200,toolbar=no,scrollbars=yes,status=no,resizable=no";
			} else {
				opts = "width=780,height=380,toolbar=no,status=no,resizable=yes";
			}
			window.open( href, "publish_popup"+popup, opts );
		}
		
		</script>
	]]></xsl:text>
	
	<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0">

		<tr>
			<td width="100%"><xsl:value-of select="msg:getString($messages, 'UI.PUBLISHERS-MINI.REFRESH')"/></td>
			<td>
				<div class="btn_header">
					<a href="javascript:void">
					<xsl:attribute name="onclick">publishPopup('<xsl:value-of select="$baseUrl"/>Publish', false);return false;</xsl:attribute>
						<xsl:value-of select="msg:getString($messages, 'UI.PUBLISHERS-MINI.MORE')"/>
					</a>
				</div>
			</td>
		</tr>
	</table>

		<xsl:variable name="columns">2</xsl:variable>

	<table border="0" width="100%" class="content_container2" cellpadding="0" cellspacing="0">
		<tr>
			<td class="content_body">
				<table width="100%" border="0" cellpadding="0" cellspacing="0" style='padding-top: 5px;'>	
				
					<xsl:for-each select="publisher">
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

		<xsl:variable name="iconUrl">
			<xsl:choose>
				<xsl:when test="icon!=''">
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
                <xsl:when test="class='org.pentaho.platform.repository.subscription.SubscriptionPublisher'">
                    <xsl:text>/pentaho-style/images/btn_refreshmetadata.png</xsl:text>
                </xsl:when>
				<xsl:otherwise>
					<xsl:text>/pentaho-style/images/btn_systemsettings.png</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

			<td width="50%">
				<div>
					<xsl:attribute name="style">height:32px;padding:5px 5px 0px 5px; background-image: url(<xsl:value-of select="$iconUrl"/>);background-repeat: no-repeat;background-position: 0px 0px;</xsl:attribute>
					<xsl:attribute name="onmouseover">this.style.backgroundPosition="0px -32px"</xsl:attribute>
					<xsl:attribute name="onmouseout">this.style.backgroundPosition="0px -0px"</xsl:attribute>

				<a class="home_btn" href="#">
					<table border="0" cellpadding="0" cellspacing="0" height="32">
						<tr>
							<td width="30">
					<xsl:attribute name="onclick">publishPopup('<xsl:value-of select="$baseUrl"/>Publish?publish=now&amp;style=popup&amp;class=<xsl:value-of select="class"/>', true);return false;</xsl:attribute>
							</td>
							<td>
					<xsl:attribute name="onclick">publishPopup('<xsl:value-of select="$baseUrl"/>Publish?publish=now&amp;style=popup&amp;class=<xsl:value-of select="class"/>', true);return false;</xsl:attribute>
								<xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
								<xsl:value-of select="name" disable-output-escaping="yes"/>
							</td>
						</tr>
					</table>
				</a>
				</div>
			</td>

	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>