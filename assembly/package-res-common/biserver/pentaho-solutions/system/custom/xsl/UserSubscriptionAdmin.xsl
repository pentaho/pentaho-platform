<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
  	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages" 
  	exclude-result-prefixes="html msg">

  <xsl:include href="system/custom/xsl/xslUtil.xsl" />

  <xsl:output method="html" encoding="UTF-8" />

  <xsl:param name="href" select="''" />
  <xsl:param name="baseUrl" select="''" />

  <xsl:template match="commandResult">
  	<xsl:variable name="messages" select="msg:getInstance()" />
  
	<script>
		<![CDATA[

			setTimeout( 'goBack()', 3000 );
			function goBack() {
				window.history.back();
			}


		]]>
	</script>
  
    <span class="portlet-section-header">
      <xsl:choose>
        <xsl:when test="@result = 'OK'">
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.COMP_SUCCESS')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:when test="@result = 'WARNING'">
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.COMP_WARN')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:when test="@result = 'ERROR'">
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.REQ_FAILED')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.REQ_COMP')" disable-output-escaping="yes" />
        </xsl:otherwise>
      </xsl:choose>
    </span>
    <p/>
    
    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />
    <xsl:call-template name="return" />
  </xsl:template>
  
  <xsl:template name="paramErrors">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:if test="count( paramErrors/paramMissing ) > 0" >
      <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.PARAM_MISSING')" disable-output-escaping="yes" />
      <xsl:for-each select="paramErrors/paramMissing">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="text()" disable-output-escaping="yes" />
        <xsl:text>" </xsl:text>
      </xsl:for-each>
      <p/>      
    </xsl:if>
  </xsl:template>

  <xsl:template name="exceptions">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:if test="count( exception ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.PARAM_MISSING')" disable-output-escaping="yes" />
      </span>
      <br/>
      <xsl:for-each select="exception">
        <xsl:value-of select="message/text()" disable-output-escaping="yes" />
        <xsl:text> - </xsl:text>
        <br/>
        <xsl:value-of select="exceptionMessage/text()" disable-output-escaping="yes" />
        <p/>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="messages">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:if test="count( message[@result='ERROR'] ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.ERROR_COLON')" disable-output-escaping="yes" />
      </span>
      <br/>
      <xsl:for-each select="message[@result='ERROR']">
        <xsl:value-of select="text()" disable-output-escaping="yes" />
        <p/>
      </xsl:for-each>
    </xsl:if>
    
    <xsl:if test="count( message[@result='WARNING'] ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.WARNING_COLON')" disable-output-escaping="yes" />
      </span>
      <br/>
      <xsl:for-each select="message[@result='WARNING']">
        <xsl:value-of select="text()" disable-output-escaping="yes" />
        <p/>
      </xsl:for-each>
    </xsl:if>

    <xsl:for-each select="message[@result='INFO']">
      <xsl:value-of select="text()" disable-output-escaping="yes" />
      <p/>
    </xsl:for-each>

    <xsl:for-each select="message[@result='OK']">
      <xsl:value-of select="text()" disable-output-escaping="yes" />
      <p/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="return">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <a>
      <xsl:attribute name="href">
      <xsl:value-of select="$baseUrl" />
      <xsl:text>&amp;schedulerAction=listSchedules</xsl:text>
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.RETURN_ADMIN')" disable-output-escaping="yes" />
    </a>
  </xsl:template>
  
  <xsl:template match="returnURL">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$baseUrl" />
        <xsl:value-of select="text()" />
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.RETURN')" disable-output-escaping="yes" />
    </a> | 
  </xsl:template>

  <xsl:template match="returnParam">
    <xsl:for-each select="*">
      <br/><xsl:value-of select="name()"/><xsl:text> = </xsl:text><xsl:value-of select="text()"/>
    </xsl:for-each>
    <p/>
  </xsl:template>

  <xsl:template name="header">
  	<xsl:variable name="messages" select="msg:getInstance()" />
      <span class="portlet-font">
      	<xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.HELP')" disable-output-escaping="yes" />
      </span>
    <p/>
  </xsl:template>
  
  <xsl:template match="added">
    <xsl:text>Added: </xsl:text>
    <xsl:value-of select="text()" disable-output-escaping="yes" />
    <br/>
  </xsl:template>

  <xsl:template match="modified">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.MODIFIED_COLON')" disable-output-escaping="yes" />
    <xsl:value-of select="text()" disable-output-escaping="yes" />
    <br/>
  </xsl:template>
  
  <xsl:template name="content">
      <td class="portlet-table-text">
        <xsl:value-of select="actionRef" />
      </td>
  </xsl:template>
      
  <xsl:template match="listSubscriptions">
  	<xsl:variable name="messages" select="msg:getInstance()" />
    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.MANAGE_USER_SUBSCRIPTIONS')" disable-output-escaping="yes" />
    </span>
    <p/>
    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <xsl:apply-templates select="subscriptions" />
    <p/>
    
  </xsl:template>

  <xsl:template match="subscriptions">
	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:choose>
      <xsl:when test="count(subscription) &gt; 0">  
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.SUBSCRIPTIONS')" disable-output-escaping="yes" />
        </span>
          <table width="95%" border="0" cellpadding="5px" cellspacing="0">
            <tr>
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.TITLE.TITLE')" disable-output-escaping="yes" />
              </td>
    
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.ACTION_SEQUENCE')" disable-output-escaping="yes" />
              </td>
              
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.SCHEDULE')" disable-output-escaping="yes" />
              </td>
              
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.ACTION')" disable-output-escaping="yes" />
              </td>
            </tr>
            <xsl:for-each select="subscription">
              <xsl:sort select="user" />
              <xsl:sort select="actionRef" />
              <xsl:call-template name="subscription" />
            </xsl:for-each>
          </table>
      </xsl:when>
      <xsl:otherwise>      
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.NO_SUBSCRIPTIONS_DEFINED')" disable-output-escaping="yes" />
        </span>
      </xsl:otherwise>
    </xsl:choose>
    <p/>
  </xsl:template>

  <xsl:template name="subscription">
  	<xsl:variable name="messages" select="msg:getInstance()" />
    <xsl:if test="count(errorMsg) &gt; 0">
      <tr>
        <td colspan="99">
          <xsl:text>ERROR:</xsl:text>
          <xsl:value-of select="errorMsg/text()" disable-output-escaping="yes" />
        </td>
      </tr>
    </xsl:if>
    <tr>
      <td class="portlet-table-text">
        <xsl:value-of select="title" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="actionRef" />
      </td>
      <td class="portlet-table-text">
        <xsl:for-each select="schedules/schedule">
          <xsl:sort select="group" />
          <xsl:sort select="schedRef" />
          <xsl:value-of select="group"/> - <xsl:value-of select="schedRef"/><br/>
        </xsl:for-each>
      </td>

      <td class="portlet-table-text">
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$baseUrl" />
          <xsl:text>&amp;schedulerAction=doDeleteSubscription&amp;subscriptionId=</xsl:text>
          <xsl:value-of select="@subscriptionId" />
          <xsl:text>&amp;title=</xsl:text>
          <xsl:value-of select="title" />
        </xsl:attribute>
        <xsl:value-of select="msg:getXslString($messages, 'UI.SUBSCRIPTION_ADMIN.DELETE')" disable-output-escaping="yes" />
      </a>
    </td>
    </tr>
  </xsl:template>

        
</xsl:stylesheet>