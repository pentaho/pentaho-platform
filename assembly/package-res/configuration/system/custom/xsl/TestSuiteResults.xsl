<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:variable name="messages" select="msg:getInstance()" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="baseUrl" select="''"/>

	<xsl:template match="test-suites">

		<input type="checkbox" id="auto-check">
			<xsl:if test="@next-suite">
				<xsl:attribute name="checked">true</xsl:attribute>
			</xsl:if>
		</input>
		 <span class="portlet-font"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_AUTO_TEST')" disable-output-escaping="yes"/></span>

		<input type="button" onclick="document.location.href=autoUrl" class="portlet-font">
			<xsl:attribute name="value"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_START')" disable-output-escaping="yes"/></xsl:attribute>		
		</input>

		<xsl:if test="properties">

		<a href="#">
			<xsl:attribute name="onclick">document.getElementById('properties').style.display='block'; return false;</xsl:attribute>
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SUBMIT')" disable-output-escaping="yes"/>
		</a>
		</xsl:if>

		<script>
			var suite="";
			
			<xsl:choose>
				<xsl:when test="@next-suite">
					var autoUrl = "TestSuite?auto=true&amp;action=run&amp;suite=<xsl:value-of select="@next-suite"/>";
				</xsl:when>
				<xsl:otherwise>
					var autoUrl = "TestSuite?auto=true&amp;action=run&amp;suite=<xsl:value-of select="/test-suites/suite[1]/@class"/>";
				</xsl:otherwise>
			</xsl:choose>
		</script>


		<div id="suitediv" style="position:absolute;top:70px;left:5px;width:490px;height:560px;overflow:auto;border:1px solid #888888;background:white">
		<table width="100%" cellpadding="0" cellspacing="0">
			<tr >
				<td colspan="1" class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_SUITE')" disable-output-escaping="yes"/>
				</td>
				<td colspan="3" class="portlet-table-header" style="text-align:center">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_NUM_PASS_FAIL')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header" style="text-align:center">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_ACTIONS')" disable-output-escaping="yes"/>
				</td>
			</tr>
			<xsl:for-each select="suite">
				<xsl:call-template name="suite"/>
			</xsl:for-each>
		</table>
		</div>
		
		<xsl:for-each select="suite">
			<div>
				<xsl:attribute name="id"><xsl:value-of select="@class"/></xsl:attribute>

				<xsl:choose>		
					<xsl:when test="/test-suites/@last-suite=@class">
						<xsl:attribute name="style">display:block;position:absolute;top:70px;left:505px;width:490px;height:560px;overflow:auto;border:1px solid #888888;background:white</xsl:attribute>
						<script>
							suite = "<xsl:value-of select="@class"/>";
						</script>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">display:none;position:absolute;top:70px;left:505px;width:490px;height:560px;overflow:auto;border:1px solid #888888;background:white</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<center>
					<table width="100%" cellpadding="0" cellspacing="0">
						<tr>
							<td class="portlet-table-header" width="100%" style="white-space: nowrap;text-align:center">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_TEST_NAME')" disable-output-escaping="yes"/>
							</td>
							<td class="portlet-table-header" width="1" style="white-space: nowrap;text-align:center">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_RUN')" disable-output-escaping="yes"/>
							</td>
							<td class="portlet-table-header" width="1" style="white-space: nowrap;text-align:center">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_PASS')" disable-output-escaping="yes"/>
							</td>
							<td class="portlet-table-header" width="1" style="white-space: nowrap;text-align:center">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_FAIL')" disable-output-escaping="yes"/>
							</td>
							<td class="portlet-table-header" width="1" style="white-space: nowrap;text-align:center">
								<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_ACTIONS')" disable-output-escaping="yes"/>
							</td>
						</tr>

						<xsl:for-each select="tests/test">
							<xsl:call-template name="test"/>
						</xsl:for-each>
					</table>
				</center>
			</div>
		</xsl:for-each>

		<xsl:if test="properties">
		<div id="properties" style="position:absolute;top:70px;left:5px;width:990px;height:560px;overflow:auto;border:1px solid #888888;background:white;display:none">
		<p/>
		<span class="portlet-subsection-header"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_ENVIRONMENT')"/></span>
		<table width="100%" cellpadding="0" cellspacing="0">
			<tr>
				<td class="portlet-table-header" style="width:230px">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_PROPERTY')" disable-output-escaping="yes"/>
				</td>
				<td class="portlet-table-header">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_VALUE')" disable-output-escaping="yes"/>
				</td>
			</tr>
			<xsl:for-each select="properties/property">
				<tr>
					<td class="portlet-table-text">
						<xsl:value-of select="@name"/>
					</td>
					<td class="portlet-table-text">
						<xsl:value-of select="@value"/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<p/>
		<span class="portlet-subsection-header"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_SUBMISSION')" disable-output-escaping="yes"/></span>
		<br/><span class="portlet-font"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_SUBMIT_HINT')" disable-output-escaping="yes"/></span>
		<p/>
		<a href="#"><xsl:value-of select="msg:getString('UI.TEST_SUITE.SEE_SUBMISSION')"/></a>
		<p/>
		<span class="portlet-font"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_PENTAHO_ID')" disable-output-escaping="yes"/><input id="userid"/></span>
		<br/>			
		<input type="button" class="portlet-font">
			<xsl:attribute name="value"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_SUBMIT')" disable-output-escaping="yes"/></xsl:attribute>
		</input>
		<input type="button" class="portlet-font">
			<xsl:attribute name="onclick">document.getElementById('properties').style.display='none'; return false;</xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="msg:getXslString($messages, 'UI.USER_CANCEL')" disable-output-escaping="yes"/></xsl:attribute>
		</input>

		</div>
		</xsl:if>

		<script type="text/javascript">var me = document.getElementById('currentsuiterow'); var top=me.offsetTop-17; if( top > (560/2) ) { document.getElementById('suitediv').scrollTop=top-(560/2); } </script>
		<!-- add auto-scrolling here ? -->


	</xsl:template>

	<xsl:template name="suite">

		<tr >
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@class=/test-suites/@last-suite">
						<xsl:attribute name="style">background:#bbffbb;font-weight:bold;position:relative</xsl:attribute>
						<xsl:attribute name="id">currentsuiterow</xsl:attribute>
					</xsl:when>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style"></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="@name"/> (<xsl:value-of select="@test-count"/><xsl:text> </xsl:text> <xsl:value-of select="msg:getXslString($messages, 'UI.USER_TEST_SUITE_TESTS')" disable-output-escaping="yes"/>)
				
			</td>
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
					<xsl:value-of select="@run-count"/> 
			</td>
			<td class="portlet-table-text" >
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right;color:green</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;color:green;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
					<xsl:value-of select="@pass-count"/> 
			</td>
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right;color:red</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;color:red;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="@fail-count"/>
			</td>
			<td class="portlet-table-text" >
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:center</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:center;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<a class="portlet-font">
					<xsl:attribute name="href">TestSuite?action=run&amp;suite=<xsl:value-of select="@class"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_RUN')" disable-output-escaping="yes"/>
				</a> 
				|
				<!-- <a class="portlet-font">
					<xsl:attribute name="href">TestSuite?action=stop&amp;suite=<xsl:value-of select="@class"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_STOP')" disable-output-escaping="yes"/>
				</a> | -->
		<a href="#">
			<xsl:attribute name="onclick">if( suite != '') document.getElementById(suite).style.display='none'; suite='<xsl:value-of select="@class"/>'; document.getElementById('<xsl:value-of select="@class"/>').style.display='block'; return false;</xsl:attribute>
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SHOW')" disable-output-escaping="yes"/>
		</a>
			</td>
		</tr>

	</xsl:template>

	<xsl:template name="test">
		<tr>
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">border-bottom:0px</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">background:#ffcccc;border-bottom:0px</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="@name"/> 
			</td>
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:value-of select="@run-count"/>
			</td>
			<td class="portlet-table-text">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right;color:white</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;color:red;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:value-of select="@pass-count"/>
			</td>
			<td class="portlet-table-text" style="text-align:right;color:red">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:right;color:red</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:right;color:red;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="@fail-count"/>
			</td>
			<td class="portlet-table-text" >
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">text-align:center;white-space: nowrap;text-align:center</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">text-align:center;background:#ffcccc;white-space: nowrap;text-align:center</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<a class="portlet-font">
					<xsl:attribute name="href">TestSuite?action=run&amp;suite=<xsl:value-of select="../../@class"/>&amp;test=<xsl:value-of select="@method"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_RUN')" disable-output-escaping="yes"/>
				</a> 
				<!-- |
				<a class="portlet-font">
					<xsl:attribute name="href">TestSuite?action=stop&amp;suite=<xsl:value-of select="../../@class"/>&amp;test=<xsl:value-of select="@method"/></xsl:attribute>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_STOP')" disable-output-escaping="yes"/>
				</a> -->
			</td>
		</tr>
		<tr>
			<td colspan="5" class="portlet-table-text" style="">
				<xsl:choose>
					<xsl:when test="@fail-count='0'">
						<xsl:attribute name="style">border-bottom:1px solid #888888</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="style">border-bottom:1px solid #888888;background:#ffcccc</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:value-of select="message"/>, 
<xsl:choose>
	<xsl:when test="@duration='-1'">
	</xsl:when>
	<xsl:when test="@last-run='unknown'">
	</xsl:when>
	<xsl:otherwise>
				<xsl:value-of select="@last-run"/>
	</xsl:otherwise>
</xsl:choose>
<xsl:choose>
	<xsl:when test="@duration='-1'">
	</xsl:when>
	<xsl:when test="@last-run='unknown'">
	</xsl:when>
	<xsl:otherwise>
		(<xsl:value-of select="@duration"/> s)
	</xsl:otherwise>
</xsl:choose>

			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>