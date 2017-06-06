<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40"
 xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	xmlns:str_util="http://www.w3.org/2001/10/str-util.xsl"
 exclude-result-prefixes="o x ss html msg str_util">

<xsl:output method="html" encoding="UTF-8" />

	<xsl:template name="breadcrumbs">
		<xsl:param name="names"/>
		<xsl:param name="path"/>
		<xsl:param name="level" select="1"/>

		<xsl:variable name="name">
					<xsl:value-of select="substring-before($names,'/')"/>
		</xsl:variable>	

		<xsl:variable name="solution-name">
			<xsl:choose>
				<xsl:when test="$level=1">
					<xsl:text></xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$solution"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>	

		<xsl:variable name="thispath">
			<xsl:choose>
				<xsl:when test="$level=1">
					<xsl:text></xsl:text>
				</xsl:when>
				<xsl:when test="$level=2">
					<xsl:text></xsl:text>
				</xsl:when>
				<xsl:otherwise>
		<xsl:call-template name="breakPath">
			<xsl:with-param name="path" select="$path"/>
			<xsl:with-param name="level" select="number($level)-2"/>
		</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>	

		<td>

			<div class="icon_folder_sm">
				<a>
					<xsl:attribute name="href">Navigate?solution=<xsl:value-of select="$solution-name"/>&amp;path=<xsl:value-of select="$thispath"/></xsl:attribute>

					<xsl:call-template name="removeIndex">
						<xsl:with-param name="title" select="$name"/>
					</xsl:call-template>

				</a>
			</div>
		</td>

	<xsl:variable name="tmpNames" select="substring-after($names,'/')"/>
	
	<xsl:variable name="tmpPath">
			<xsl:choose>
				<xsl:when test="$level=1">
					<xsl:value-of select="substring-after($path,'/')"/>
				</xsl:when>
				<xsl:when test="$level=2">
					<xsl:value-of select="substring-after($path,'/')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$path"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>	

	<xsl:if test="$tmpNames!=''">
		<xsl:call-template name="breadcrumbs">
			<xsl:with-param name="names" select="$tmpNames"/>
			<xsl:with-param name="path" select="$tmpPath"/>
			<xsl:with-param name="level" select="$level+1"/>
		</xsl:call-template>
	</xsl:if>

	</xsl:template>

	<xsl:template name="breakPath">
		<xsl:param name="path"/>
		<xsl:param name="level"/>
		<xsl:param name="idx" select="1"/>

			<xsl:value-of select="substring-before($path,'/')"/>

		<xsl:if test="$level &gt; $idx">	
			<xsl:text>/</xsl:text>
			<xsl:call-template name="breakPath">
			<xsl:with-param name="path" select="substring-after($path,'/')"/>
			<xsl:with-param name="level" select="$level"/>
			<xsl:with-param name="idx" select="$idx+1"/>
		</xsl:call-template>
		</xsl:if>
			
	</xsl:template>

	<xsl:template name="removeIndex">
		<xsl:param name="title"/>
		
		<xsl:choose>
			<xsl:when test="substring($title,2,1)='.' and number(substring($title,1,1))&lt;10">
				<xsl:value-of select="substring($title,3)" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:when test="substring($title,3,1)='.' and number(substring($title,1,2))&lt;100">
				<xsl:value-of select="substring($title,4)" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$title" disable-output-escaping="yes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="setupFly">
		<div id="flydiv" style="position:absolute;top:-1000px;left:-1000px;height:203px;width:502px;z-index:100" >
			<xsl:attribute name="onmouseover">
				<xsl:text>flyStay()</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="onmouseout">
				<xsl:text>hideFly()</xsl:text>
			</xsl:attribute>
			<table border="0" cellpadding='0' cellspacing='0' style="vertical-align:bottom">
      <tr>
         <td width="5" height="9"><img border="0" src="/pentaho-style/images/fly-top-left.png"/></td>
         <td colspan="2" style="background-image: url(/pentaho-style/images/fly-top.png);background-repeat: repeat-x;"></td>

         <td><img border="0" src="/pentaho-style/images/fly-top-right.png"/></td>
      </tr>
      <tr class="flyContent">
         <td valign="top" style="background-image: url(/pentaho-style/images/fly-left.png);background-repeat: repeat-y; height: 171px;" colspan="2">
			<a href="javascript:void" onclick="changeFlyTab( 1 ); return false;">
				<img id="img-t1" src="/pentaho-style/images/btn_info_active.png" alt="Info" border="0" />
			</a>
			<br />
			<a href="javascript:void" onclick="changeFlyTab( 2 ); return false;">
				<img id="img-t2" src="/pentaho-style/images/btn_actions.png" border="0" />
			</a>
		</td>
		<td>
			<table border="0" cellpadding='0' cellspacing='0' style="vertical-align:bottom">
				<tr>
					<td valign="top">
						<div style="height:150px;width:420px;overflow:auto;padding-left:3px">
							<table id="flyTable" width="100%" cellpadding='0' cellspacing='0' >
								<tr>
									<td>
										<div id="flyTab1" style="display:block;padding:0px;margin:0px">
											<table cellpadding='0' cellspacing='0' border='0' width='400'>
												<tr>
													<td valign="top">
											<div id="flyTitle1" class="flyTitle">Title1</div>
											<div id="flyDesc1" class="flyDesc">
												Desc1
												<p>xx</p>
											</div>
													</td>
													<td style="padding:5px;width:140px"><img id="flyimg" border="0" src="/pentaho-style/images/spacer.gif" width="140" height="140" style="display:block"/></td>
												</tr>
											</table>
										</div>
										<div id="flyTab2"  style="display:none">
											<div id="flyTitle2" class="flyTitle">Actions</div>
											<div id="flyDesc2" class="flyDesc">
											</div>
										</div>
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
			</table>       
		</td>

         <td style="background-image: url(/pentaho-style/images/fly-right.png);background-repeat: repeat-y;"></td>
				</tr>
      <tr>
         <td><img border="0" src="/pentaho-style/images/fly-bot-left.png"/></td>
         <td colspan="2"><img src="/pentaho-style/images/fly-bot.png" height="16" width="100%"/></td>
         <td><img border="0" src="/pentaho-style/images/fly-bot-right.png"/></td>
      </tr>
			</table>
		</div>
	</xsl:template>
	

</xsl:stylesheet>

