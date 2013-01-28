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

<xsl:include href="system/custom/xsl/xslUtil.xsl"/>
<xsl:include href="system/custom/xsl/files-util.xsl"/>

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

	<table width="100%">
		<tr>
			<td style="text-align:right">
				<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.VIEW')" disable-output-escaping="yes"/>&#160;
				<a>
					<xsl:attribute name="href">Navigate?view=files-icons.xsl&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.ICONS')" disable-output-escaping="yes"/>
				</a> | 
				<a>
					<xsl:attribute name="href">Navigate?view=files-list.xsl&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.LIST')" disable-output-escaping="yes"/>
				</a> | 
				<a>
					<xsl:attribute name="href">Navigate?view=default&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.DEFAULT')" disable-output-escaping="yes"/>
				</a>
			</td>
		</tr>
	</table>
	
		<xsl:variable name="description">
			<xsl:value-of select="description"/>
		</xsl:variable>

	<xsl:variable name="columns">1</xsl:variable>

	<xsl:call-template name="script"/>

		<xsl:call-template name="setupFly"/>

		<div style="margin:10px">

		<xsl:variable name="title">
			<xsl:value-of select="title"/>
		</xsl:variable>

	<table border="0" width="100%" cellpadding="0" cellspacing="0" style="padding:1px" >
		<tr>
			<td width="100%" class="content_header" style="padding-left:5px"><xsl:value-of select="$title"
										disable-output-escaping="yes" /></td>
		</tr>
		<tr>
			<td width="100%" style="padding-left:5px"><xsl:value-of select="$description"
										disable-output-escaping="yes" /></td>
		</tr>
	</table>

		<xsl:call-template name="doHeading">
			<xsl:with-param name="title"><xsl:value-of select="msg:getXslString($messages, 'UI.FILES.BROWSE')" disable-output-escaping="yes"/>&#160;<xsl:value-of select="count(file[@visible='true'][@type='FILE.FOLDER'])"/>&#160;<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.SOLUTIONS')" disable-output-escaping="yes"/></xsl:with-param>
		</xsl:call-template> 

			<div class="navigation_table" id="content_home_scroll" style="width:100%;left:0px;margin-left:0px;">
				<table border="0" cellpadding="0" cellspacing="0" style="width:100%;width:expression( document.documentElement.offsetWidth - 58)">	

		<xsl:for-each select="file">
			<xsl:if test="@visible='true'">
				<xsl:if test="@type='FILE.FOLDER'">

					<tr style="cursor:pointer">
							<xsl:attribute name="onclick">var id='contentdiv2<xsl:value-of select="position()"/>'; var e=document.getElementById(id); if(e.style.display=='none') { e.style.display='block'; document.getElementById('contentimg<xsl:value-of select="position()"/>').src='/pentaho-style/images/btn_minus.png'; } else { e.style.display='none'; document.getElementById('contentimg<xsl:value-of select="position()"/>').src='/pentaho-style/images/btn_plus.png'; } return false;</xsl:attribute>
						<td class="fileFolderDefault" >
							<xsl:attribute name="onmouseover">this.className='fileFolderHover'</xsl:attribute>
							<xsl:attribute name="onmouseout">this.className='fileFolderDefault'</xsl:attribute>
							<img src="/pentaho-style/images/btn_minus.png" border="0">
								<xsl:attribute name="id">contentimg<xsl:value-of select="position()"/></xsl:attribute>
							</img><xsl:text disable-output-escaping="yes"></xsl:text>
							<a href="#"><xsl:value-of select="title"/></a>
						</td>
					</tr>
				
					<tr>

						<td colspan="2" style="padding-left:0px">
							<div>
								<xsl:attribute name="id">contentdiv2<xsl:value-of select="position()"/></xsl:attribute>
								<xsl:attribute name="style">display:block</xsl:attribute>

		<table width="100%" class="list_tablexx" border="0" cellpadding="0" cellspacing="0">
  			<tr>
    			<td colspan="2" class="list_colHead_first">Name</td>
				<td class="list_colHead_fill">Author</td>
	  		</tr>
			<xsl:for-each select="file[@visible='true']">
				<xsl:sort select="title" />
				<xsl:if test="@visible='true'">
					<xsl:call-template name="doRootFolder">
						<xsl:with-param name="columns" select="$columns" />
					</xsl:call-template>
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

	</div>

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

		<xsl:variable name="messages" select="msg:getInstance()" />

		<xsl:call-template name="setupFly"/>
		
		<xsl:variable name="title">
			<xsl:value-of select="/files/file/title"/>
		</xsl:variable>

		<xsl:variable name="description">
			<xsl:value-of select="/files/file/description"/>
		</xsl:variable>

		<table width="100%" class="content_table" border="0" cellpadding="0" cellspacing="0" height="20" style="padding-left: 5px; height: 24px;">
			<tr>
				<td>
					<table>
						<tr>
							<xsl:choose>
								<xsl:when test="$path!=''">
									<xsl:call-template name="breadcrumbs">
										<xsl:with-param name="names" select="location"/>
										<xsl:with-param name="path">/<xsl:value-of select="$solution"/>/<xsl:value-of select="$path"/>/</xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="breadcrumbs">
										<xsl:with-param name="names" select="location"/>
										<xsl:with-param name="path">/<xsl:value-of select="$solution"/>/</xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>
						</tr>
					</table>
				</td>
									
			<td style="text-align:right">
				<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.VIEW')" disable-output-escaping="yes"/>&#160;
				<a>
					<xsl:attribute name="href">Navigate?view=files-icons.xsl&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.ICONS')" disable-output-escaping="yes"/>
				</a> | 
				<a>
					<xsl:attribute name="href">Navigate?view=files-list.xsl&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.LIST')" disable-output-escaping="yes"/>
				</a> | 
				<a>
					<xsl:attribute name="href">Navigate?view=default&amp;solution=<xsl:value-of select="$solution"/>&amp;path=<xsl:value-of select="$path"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.FILES.DEFAULT')" disable-output-escaping="yes"/>
				</a>
			</td>

  			</tr>
		</table>
		<p/>

		<xsl:variable name="columns">1</xsl:variable>

		<table width="100%" class="list_table" border="0" cellpadding="0" cellspacing="0">
  			<tr>
    			<td colspan="2" class="list_colHead_first">Name</td>
				<td class="list_colHead_fill">Author</td>
	  		</tr>
			<xsl:for-each select="/files/file">
				<xsl:sort select="title" />

				<xsl:if test="@visible='true'">
					<xsl:call-template name="doFolder">
						<xsl:with-param name="columns" select="$columns" />
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
		</table>

	</xsl:template>

	<xsl:template name="doRootFolder">
		<xsl:param name="columns" />

		<xsl:if test="((position()-1) mod number($columns)) = 0">
			<xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
		</xsl:if>

        <xsl:choose>
			<xsl:when test="@type='FILE.FOLDER'">
				<xsl:call-template name="doEntry">
					<xsl:with-param name="level" select="1" />
					<xsl:with-param name="columns" select="$columns" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="@type='FILE.URL'">
				<xsl:call-template name="doEntry">
					<xsl:with-param name="level" select="1" />
					<xsl:with-param name="columns" select="$columns" />
				</xsl:call-template>
			</xsl:when>
		
			<xsl:otherwise>
				<xsl:call-template name="doEntry">
					<xsl:with-param name="level" select="1" />
					<xsl:with-param name="columns" select="$columns" />
				</xsl:call-template>
			</xsl:otherwise>
        </xsl:choose>
    
        <xsl:if test="((position()-1) mod number($columns)) = (number($columns))-1">
			<xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="doFolder">
		<xsl:param name="columns" />

		<xsl:if test="@type='FILE.FOLDER'">

			<xsl:for-each select="file[@visible='true']">
				<xsl:sort select="title" />

				<xsl:if test="((position()-1) mod number($columns)) = 0">
					<xsl:text disable-output-escaping="yes">&lt;tr&gt;</xsl:text>
				</xsl:if>

				<xsl:call-template name="doEntry">
					<xsl:with-param name="level" select="1" />
					<xsl:with-param name="columns" select="$columns" />
				</xsl:call-template>

				<xsl:if test="((position()-1) mod number($columns)) = (number($columns))-1">
					<xsl:text disable-output-escaping="yes">&lt;/tr&gt;</xsl:text>
				</xsl:if>
			</xsl:for-each>

		</xsl:if>

	</xsl:template>

	<xsl:template name="doEntry">
		<xsl:param name="level" />
		<xsl:param name="columns" />

		<xsl:choose>
			<xsl:when
				test="@type='FILE.FOLDER' and @visible='true' and $level!=$levels">
				<xsl:variable name="url">
					<xsl:value-of select="$baseUrl" />
					<xsl:value-of select="$solutionParam" />
					<xsl:text>=</xsl:text>
					<xsl:value-of select="solution" />
					<xsl:text>&amp;</xsl:text>
					<xsl:value-of select="$pathParam" />
					<xsl:text>=</xsl:text>
					<xsl:value-of select="path" />
					<xsl:text>&amp;action=</xsl:text>
					<xsl:value-of select="filename" />
				</xsl:variable>
				<xsl:variable name="icon">
					<xsl:if test="icon">
						<xsl:value-of select="$href" />
						<xsl:value-of select="icon" />
					</xsl:if>
				</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="rollovericon">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="rollovericon">
			<xsl:if test="$hasRollover">
				<xsl:value-of select="$href" />
				<xsl:value-of select="rollovericon" />
			</xsl:if>
		</xsl:variable>

			<xsl:call-template name="doItem">
				<xsl:with-param name="title" select="title"/>
				<xsl:with-param name="description" select="description"/>
				<xsl:with-param name="author" select="''"/>
				<xsl:with-param name="url" select="$url"/>
				<xsl:with-param name="target" select="'.'"/>
				<xsl:with-param name="icon" select="$icon"/>
				<xsl:with-param name="rollovericon" select="$rollovericon"/>
			</xsl:call-template>

			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="doFile">
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="doFile">

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>

		<xsl:variable name="extension"><xsl:call-template name="keep-after-last">
			<xsl:with-param name="string"><xsl:value-of select="filename" /></xsl:with-param>
			<xsl:with-param name="delimiter">.</xsl:with-param>
		</xsl:call-template></xsl:variable>

		<!--  disabled for now... -->
		<!-- xsl:variable name="defaulthandler"><xsl:value-of select="plugins:getContentGeneratorIdForType( $extension )"/></xsl:variable>
		<xsl:variable name="defaulturl"><xsl:value-of select="plugins:getContentGeneratorUrlForType( $extension )"/></xsl:variable -->
		<xsl:variable name="defaulthandler"></xsl:variable>
		<xsl:variable name="defaulturl"></xsl:variable>

		<xsl:variable name="url">
			<xsl:choose>
    	        <xsl:when test="@type='FILE.URL'">
					<xsl:value-of select="url" />
		    	</xsl:when>
			    <xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$defaulturl!=''">
							<xsl:value-of select="$href" />
							<xsl:value-of select="$defaulturl" />
							<xsl:text>?solution=</xsl:text>
							<xsl:value-of select="solution" />
							<xsl:text>&amp;path=</xsl:text>
							<xsl:value-of select="path" />
							<xsl:text>&amp;action=</xsl:text>
							<xsl:value-of select="filename" />
						</xsl:when>
						<xsl:when test="$defaulthandler!=''">
							<xsl:value-of select="$href" />
							<xsl:text>content/</xsl:text>
							<xsl:value-of select="$defaulthandler" />
							<xsl:text>?solution=</xsl:text>
							<xsl:value-of select="solution" />
							<xsl:text>&amp;path=</xsl:text>
							<xsl:value-of select="path" />
							<xsl:text>&amp;action=</xsl:text>
							<xsl:value-of select="filename" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$href" />
							<xsl:text>ViewAction?</xsl:text>
							<xsl:text>solution=</xsl:text>
							<xsl:value-of select="solution" />
							<xsl:text>&amp;path=</xsl:text>
							<xsl:value-of select="path" />
							<xsl:text>&amp;action=</xsl:text>
							<xsl:value-of select="filename" />
						</xsl:otherwise>
					</xsl:choose>
			    </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="rollovericon">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="rollovericon">
			<xsl:if test="$hasRollover='true'">
				<xsl:value-of select="$href" />
				<xsl:value-of select="rollovericon" />
			</xsl:if>
		</xsl:variable>

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
				<xsl:when test="@displaytype='url'">
					<xsl:value-of select="$styleBase" />
					<xsl:text>url.png</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$styleBase" />
					<xsl:text>blank-file-type.png</xsl:text>
				</xsl:otherwise>
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
		
		<xsl:call-template name="doItem">
			<xsl:with-param name="title" select="title"/>
			<xsl:with-param name="description" select="description"/>
			<xsl:with-param name="author" select="author"/>
			<xsl:with-param name="url" select="$url"/>
			<xsl:with-param name="target" select="$target"/>
			<xsl:with-param name="icon" select="$icon"/>
			<xsl:with-param name="rollovericon" select="$rollovericon"/>
		</xsl:call-template>
		
	</xsl:template>

	<xsl:template name="doItem">
		<xsl:param name="title" />
		<xsl:param name="description" />
		<xsl:param name="author" />
		<xsl:param name="url" />
		<xsl:param name="target" />
		<xsl:param name="icon" />
		<xsl:param name="rollovericon" />

		<xsl:variable name="styleBase">/pentaho-style/active/</xsl:variable>

		<xsl:variable name="hasRollover">
			<xsl:choose>
				<xsl:when test="$rollovericon=''">false</xsl:when>
				<xsl:otherwise>true</xsl:otherwise>
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
						<xsl:when test="@type='FILE.FOLDER'">/pentaho-style/images/folder_active.png</xsl:when>
						<xsl:otherwise>/pentaho-style/images/file_active.png</xsl:otherwise>
					</xsl:choose>
		</xsl:variable>
		<xsl:variable name="hoverIconUrl">
			<xsl:choose>
				<xsl:when test="rollovericon!=''">
					<xsl:value-of select="rollovericon"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="rolloverIconUrl">
					<xsl:choose>
						<xsl:when test="@type='FILE.FOLDER'">/pentaho-style/images/folder_rollover.png</xsl:when>
						<xsl:otherwise>/pentaho-style/images/file_rollover.png</xsl:otherwise>
					</xsl:choose>
		</xsl:variable>

		<xsl:variable name="safeTitle">
			<xsl:call-template name="replace-string">
				<xsl:with-param name="text">
					<xsl:call-template name="removeIndex">
						<xsl:with-param name="title" select="title"/>
					</xsl:call-template>
				</xsl:with-param>
				<xsl:with-param name="from">"</xsl:with-param>
				<xsl:with-param name="to">'</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="safeDesc">
			<xsl:call-template name="replace-string">
				<xsl:with-param name="text" select="$description"/>
				<xsl:with-param name="from">"</xsl:with-param>
				<xsl:with-param name="to">'</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name="aclModifyAcl">
			<xsl:choose>
				<xsl:when test="@aclModifyAcl">
					<xsl:value-of select="@aclModifyAcl" disable-output-escaping="yes" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>false</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

	  			<tr class="listrow_file" >
			<td class="list_col_fill" width="40" >
				<xsl:attribute name="height"><xsl:value-of select="$height"/></xsl:attribute>
				<div>
					<xsl:attribute name="style">padding:5px 5px 5px 0px;margin: 4px 0px 0px 0px; vertical-align: bottom</xsl:attribute>
				<a class="home_btn" href="#">
					<xsl:attribute name="id">a-r<xsl:value-of select="position()"/>-c1</xsl:attribute>
					<xsl:if test="$target!='.'">
						<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
					</xsl:if>
					<xsl:attribute name="href">
						<xsl:value-of select="$url" />
					</xsl:attribute>
					<xsl:attribute name="onclick">
						<xsl:text>hideFly(  ); return true;</xsl:text>
					</xsl:attribute>
					<img border="0">
						<xsl:attribute name="id">img-r<xsl:value-of select="position()"/>-c1</xsl:attribute>
						<xsl:attribute name="src"><xsl:value-of select="$iconUrl"/></xsl:attribute>
						<xsl:attribute name="onmouseover">
						this.src = '<xsl:value-of select="$rolloverIconUrl"/><xsl:text>'; showFly( this, event, 600, 205, "</xsl:text><xsl:value-of select='$safeTitle'/><xsl:text>","</xsl:text><xsl:value-of select='$safeDesc'/><xsl:text>", "</xsl:text><xsl:value-of select="$icon"/><xsl:text>", "</xsl:text><xsl:value-of select='$target'/><xsl:text>", "</xsl:text><xsl:value-of select='solution' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='path' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='filename' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='$url' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select="properties" disable-output-escaping="yes" /><xsl:text>", </xsl:text><xsl:value-of select='$aclModifyAcl' /><xsl:text> );</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="onmouseout">
							<xsl:text>this.src='</xsl:text><xsl:value-of select="$iconUrl"/><xsl:text>'; hideFly(  ); </xsl:text>
						</xsl:attribute>
					</img>
				</a>
					</div>
			</td>
			<td class="list_col_first">
				<a class="home_btn" href="#">
					<xsl:if test="$target!='.'">
						<xsl:attribute name="target"><xsl:value-of select="$target"/></xsl:attribute>
					</xsl:if>
					<xsl:attribute name="href">
						<xsl:value-of select="$url" />
					</xsl:attribute>
					<xsl:attribute name="onclick">
						<xsl:text>hideFly(  ); return true;</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="onmouseover">
					this.src = '<xsl:value-of select="$rolloverIconUrl"/><xsl:text>'; showFly( this, event, 600, 205, "</xsl:text><xsl:value-of select='$safeTitle'/><xsl:text>","</xsl:text><xsl:value-of select='$safeDesc'/><xsl:text>", "</xsl:text><xsl:value-of select="$icon"/><xsl:text>", "</xsl:text><xsl:value-of select='$target'/><xsl:text>", "</xsl:text><xsl:value-of select='solution' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='path' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='filename' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select='$url' disable-output-escaping="yes" /><xsl:text>", "</xsl:text><xsl:value-of select="properties" disable-output-escaping="yes" /><xsl:text>", </xsl:text><xsl:value-of select='$aclModifyAcl' /><xsl:text> );</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="onmouseout">
						<xsl:text>this.src='</xsl:text><xsl:value-of select="$iconUrl"/><xsl:text>'; hideFly(  ); </xsl:text>
					</xsl:attribute>
					<xsl:call-template name="removeIndex">
						<xsl:with-param name="title" select="title"/>
					</xsl:call-template>
				</a>
						<div class="list_description">
							<xsl:value-of select="$description" disable-output-escaping="yes" />
						</div>
			</td>
  					<td class="list_col_fill"><xsl:value-of select="$author" /><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
</tr>
	</xsl:template>

  	<xsl:template match="text()"/>

</xsl:stylesheet>

