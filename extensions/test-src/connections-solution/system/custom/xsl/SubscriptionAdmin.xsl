<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
  	xmlns:msg="org.pentaho.messages.Messages" 
  	exclude-result-prefixes="html msg">

  <xsl:include href="system/custom/xsl/xslUtil.xsl" />

  <xsl:output method="html" encoding="UTF-8" />

  <xsl:param name="href" select="''" />
  <xsl:param name="baseUrl" select="''" />

  <xsl:template match="commandResult">
  
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
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.COMP_SUCCESS')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:when test="@result = 'WARNING'">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.COMP_WARN')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:when test="@result = 'ERROR'">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.REQ_FAILED')" disable-output-escaping="yes" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.REQ_COMP')" disable-output-escaping="yes" />
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
    <xsl:if test="count( paramErrors/paramMissing ) > 0" >
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.PARAM_MISSING')" disable-output-escaping="yes" />
      <xsl:for-each select="paramErrors/paramMissing">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="text()" disable-output-escaping="yes" />
        <xsl:text>" </xsl:text>
      </xsl:for-each>
      <p/>      
    </xsl:if>
  </xsl:template>

  <xsl:template name="exceptions">
    <xsl:if test="count( exception ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.PARAM_MISSING')" disable-output-escaping="yes" />
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
    <xsl:if test="count( message[@result='ERROR'] ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ERROR_COLON')" disable-output-escaping="yes" />
      </span>
      <br/>
      <xsl:for-each select="message[@result='ERROR']">
        <xsl:value-of select="text()" disable-output-escaping="yes" />
        <p/>
      </xsl:for-each>
    </xsl:if>
    
    <xsl:if test="count( message[@result='WARNING'] ) > 0" >
      <span class="portlet-section-subheader">
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.WARNING_COLON')" disable-output-escaping="yes" />
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
    <a>
      <xsl:attribute name="href">
      <xsl:value-of select="$baseUrl" />
      <xsl:text>&amp;schedulerAction=listSchedules</xsl:text>
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.RETURN_ADMIN')" disable-output-escaping="yes" />
    </a>
  </xsl:template>
  
  <xsl:template match="returnURL">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$baseUrl" />
        <xsl:value-of select="text()" />
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.RETURN')" disable-output-escaping="yes" />
    </a> | 
  </xsl:template>

  <xsl:template match="returnParam">
    <xsl:for-each select="*">
      <br/><xsl:value-of select="name()"/><xsl:text> = </xsl:text><xsl:value-of select="text()"/>
    </xsl:for-each>
    <p/>
  </xsl:template>

   
  <xsl:template match="subscriptionAdmin">
    <xsl:call-template name="header" />
    <xsl:call-template name="messages" />
    <xsl:call-template name="exceptions" />    
    <xsl:apply-templates select="schedulerStatus" />
    <xsl:apply-templates select="listSchedules" />
    <xsl:apply-templates select="listContent" >
      <xsl:with-param name="title" select="'Subscription Content'" />
      <xsl:with-param name="actions" select="'content-actions'" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="showImport">
		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_IMPORT')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>
  
      <br/>
    <span class="portlet-font">
      <xsl:text>
          Use this page to select and upload an XML file containing the subscription schedules and content to import.  
          There is a sample file named SubscriptionSchedules.xml located in the system directory of the pentaho-pro-solutions 
          demo that can be used as a template. 
      </xsl:text>
      <p/>
      <xsl:text>    
          The ref attribute of the schedule node must be unique for each schedule.
          If there is an existing schedule with the same ref name, it will be modified and any existing subscriptions that reference the 
          schedule will use the new values.  Schedules can be added and modified via the import but can not be deleted.
      </xsl:text>
    </span>
    <p/>
    <xsl:call-template name="messages" />
    <xsl:apply-templates select="importResults" />
    <form method="post" enctype="multipart/form-data">
      <xsl:attribute name="action">
        <xsl:value-of select="$baseUrl" />
        <xsl:text>&amp;schedulerAction=doImport</xsl:text>
      </xsl:attribute>
      Select file:<input type="file" name="scheduleFile" size="75" accept="text/xml" /> 
      <br/>
      <input type="SUBMIT" value="Upload"/>
    </form>
    <p/>
  </xsl:template>

  <xsl:template name="header">
  
  		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="''"/>
		</xsl:call-template>

      <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.HELP')" disable-output-escaping="yes" />
    </span>
    <p/>
  </xsl:template>
  
  <xsl:template name="importResults">
    <xsl:call-template name="messages" />
    <xsl:apply-templates select="added" />
    <xsl:apply-templates select="modified" />
  </xsl:template>

  <xsl:template match="added">
    <xsl:text>Added: </xsl:text>
    <xsl:value-of select="text()" disable-output-escaping="yes" />
    <br/>
  </xsl:template>

  <xsl:template match="modified">
    <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.MODIFIED_COLON')" disable-output-escaping="yes" />
    <xsl:value-of select="text()" disable-output-escaping="yes" />
    <br/>
  </xsl:template>

  <xsl:template match="listSchedules">
    <xsl:call-template name="messages" />
    <xsl:apply-templates select="scheduledJobs" />
    <xsl:apply-templates select="unScheduledJobs" />
    <xsl:apply-templates select="extraScheduledJobs" />
  </xsl:template>

  <xsl:template match="scheduledJobs">
  
  	<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td width="100%">
				<xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_SUBS')" disable-output-escaping="yes" />
			</td>
		</tr>
	</table>

	<table border="0" class="content_container2" width="95%" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td class="portlet-table-font">
			</td>
		</tr>
	</table>

    <span class="portlet-font">
      <xsl:value-of select="message/text()" disable-output-escaping="yes" />
    </span>
    <center>
      <table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.JOB_NAME_TITLE')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DESCRIPTION')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CRON_EXP')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_FIRE_TIME_NAME')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NUM_SUBS')" disable-output-escaping="yes" />
          </td>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE')" disable-output-escaping="yes" />
          </td>
          <td colspan="2" class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_ACTION')" disable-output-escaping="yes" />
          </td>
        </tr>
        <xsl:for-each select="job">
          <xsl:sort select="group" />
          <xsl:sort select="jobId" />
          <xsl:call-template name="job" />
        </xsl:for-each>
      </table>
    </center>
    <p/>
  </xsl:template>


  <xsl:template match="unScheduledJobs">
    <span class="portlet-section-subheader">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUBS_WO_JOBS')" disable-output-escaping="yes" />
    </span>
    <span class="portlet-font">
      <br/>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUBS_WO_JOBS_HELP')" disable-output-escaping="yes" />
      <br/>
      <xsl:value-of select="message/text()" disable-output-escaping="yes" />
    </span>
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$baseUrl" />
        <xsl:text>&amp;schedulerAction=scheduleAll</xsl:text>
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_ALL')" disable-output-escaping="yes" />
    </a>
    <center>
      <table width="95%" border="0" cellpadding="5px" cellspacing="0">
        <tr>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.JOB_NAME_TITLE')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DESCRIPTION')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CRON_EXP')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_FIRE_TIME_NAME')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NUM_SUBS')" disable-output-escaping="yes" />
          </td>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE')" disable-output-escaping="yes" />
          </td>
          <td colspan="2" class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_ACTION')" disable-output-escaping="yes" />
          </td>
        </tr>
        <xsl:for-each select="job">
          <xsl:sort select="group" />
          <xsl:sort select="jobId" />
          <xsl:call-template name="job" />
        </xsl:for-each>
      </table>
    </center>
    <p/>
  </xsl:template>

  <xsl:template match="extraScheduledJobs">
    <span class="portlet-section-subheader">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUBS_WO_JOBS')" disable-output-escaping="yes" />
    </span>
    <span class="portlet-font">
      <br/>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.JOB_WO_SUB')" disable-output-escaping="yes" />
      <br/>
      <xsl:value-of select="message/text()" disable-output-escaping="yes" />
    </span>
    <center>
      <table width="95%" border="0" cellpadding="5px" cellspacing="0">
        <tr>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.JOB_NAME_TITLE')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DESCRIPTION')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CRON_EXP')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_FIRE_TIME_NAME')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NUM_SUBS')" disable-output-escaping="yes" />
          </td>

          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE')" disable-output-escaping="yes" />
          </td>
          <td colspan="2" class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_ACTION')" disable-output-escaping="yes" />
          </td>
        </tr>
        <xsl:for-each select="job">
          <xsl:sort select="group" />
          <xsl:sort select="jobId" />
          <xsl:call-template name="job" />
        </xsl:for-each>
      </table>
    </center>
    <p/>
  </xsl:template>

  <xsl:template name="job">
    <xsl:if test="count(errorMsg) &gt; 0">
      <tr>
        <td colspan="99">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ERROR_COLON')" disable-output-escaping="yes" />
          <xsl:value-of select="errorMsg/text()" disable-output-escaping="yes" />
        </td>
      </tr>
    </xsl:if>
    <tr>
      <td class="portlet-table-text">
        <xsl:value-of select="schedRef/text()" />
        <br/>
        <xsl:value-of select="title/text()" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="desc/text()" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="cron/text()" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="prevFireTime/text()" />
        <br/>
        <xsl:value-of select="nextFireTime/text()" />
      </td>

      <td class="portlet-table-text">
        <xsl:value-of select="@subscriberCount" />
      </td>

      <td class="portlet-table-text">
        <xsl:choose>
          <xsl:when test="@triggerState = 0">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_NORMAL')" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:when test="@triggerState = 1">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_PAUSED')" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:when test="@triggerState = 2">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_COMPLETE')" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:when test="@triggerState = 3">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_ERROR')" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:when test="@triggerState = 4">
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_BLOCKED')" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_NONE')" disable-output-escaping="yes" />
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <xsl:call-template name="job-actions" />
    </tr>
  </xsl:template>

  <xsl:template name="job-actions">
    <td class="portlet-table-text">
      <xsl:choose>
        <xsl:when test="count(jobId) &gt; 0">
          <a>
            <xsl:choose>
              <xsl:when test="@triggerState = 0">
                <xsl:attribute name="href">
                  <xsl:value-of select="$baseUrl" />
                  <xsl:text>&amp;schedulerAction=doPauseJob&amp;jobId=</xsl:text>
                  <xsl:value-of select="jobId" />
                </xsl:attribute>
                <xsl:value-of select="msg:getXslString('UI.USER_SCHEDULER_ACTION_SUSPEND')" disable-output-escaping="yes" />
              </xsl:when>
              <xsl:when test="@triggerState = 1">
                <xsl:attribute name="href">
                  <xsl:value-of select="$baseUrl" />
                  <xsl:text>&amp;schedulerAction=doResumeJob&amp;jobId=</xsl:text>
                  <xsl:value-of select="jobId" />
                </xsl:attribute>
                <xsl:value-of select="msg:getXslString('UI.USER_SCHEDULER_ACTION_RESUME')" disable-output-escaping="yes" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>-</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </a>
          <br/>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=doExecuteJob&amp;jobId=</xsl:text>
              <xsl:value-of select="jobId" />
              <xsl:text>&amp;</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.RUN_NOW')" disable-output-escaping="yes" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=doScheduleJob&amp;schedId=</xsl:text>
              <xsl:value-of select="schedId" />
              <xsl:text>&amp;schedRef=</xsl:text>
              <xsl:value-of select="schedRef" />
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE')" disable-output-escaping="yes" />
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td class="portlet-table-text">
      <xsl:choose>
        <xsl:when test="count(schedId) &gt; 0">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=editSchedule&amp;schedId=</xsl:text>
              <xsl:value-of select="schedId" />
              <xsl:text>&amp;jobId=</xsl:text>
              <xsl:value-of select="jobId" />
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.EDIT')" disable-output-escaping="yes" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=doDeleteJob&amp;jobId=</xsl:text>
              <xsl:value-of select="jobId" />
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DELETE')" disable-output-escaping="yes" />
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </td>
  </xsl:template>

  <xsl:template match="schedulerStatus">
    <p/>
	<table border="0" width="100%" class="content_header" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td width="100%"><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_STAT_COLON')" disable-output-escaping="yes" /></td>
		</tr>
	</table>

	<table border="0" class="content_container2" width="95%" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td class="portlet-table-font">
				<xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_STAT_COLON')" disable-output-escaping="yes" />
           <xsl:choose>
            <xsl:when test="@state = 0">
              <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_NORMAL')" disable-output-escaping="yes" />
            </xsl:when>
            <xsl:when test="@state = 1">
              <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_PAUSED')" disable-output-escaping="yes" />
            </xsl:when>
            <xsl:when test="@state = 3">
              <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_ERROR')" disable-output-escaping="yes" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="msg:getXslString('UI.USER_TRIGGER_STATE_NONE')" disable-output-escaping="yes" />
            </xsl:otherwise>
          </xsl:choose>
			</td>
		</tr>

   <tr>
      <td class="content_body">

    <span class="portlet-section-subheader">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTIONS')" disable-output-escaping="yes" />
    </span>

    <table border="0" cellpadding="5px" cellspacing="0">
      <tr>
        
        <td class="portlet-table-font">
          <a>
            <xsl:choose>
              <xsl:when test="@state = 0"> <!-- NORMAL -->
                <xsl:attribute name="href">
                  <xsl:value-of select="$baseUrl" />
                  <xsl:text>&amp;schedulerAction=doSuspendScheduler</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="msg:getXslString('UI.USER_SCHEDULER_ACTION_SUSPEND')" disable-output-escaping="yes" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="href">
                  <xsl:value-of select="$baseUrl" />
                  <xsl:text>&amp;schedulerAction=doResumeScheduler</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="msg:getXslString('UI.USER_SCHEDULER_ACTION_RESUME')" disable-output-escaping="yes" />
              </xsl:otherwise>
            </xsl:choose>
          </a>          
        </td>
        <td class="portlet-table-font">|</td>
        <td class="portlet-table-font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=addSchedule</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ADD_SCHEDULE')" disable-output-escaping="yes" />
          </a>
        </td>
        <td class="portlet-table-font">|</td>
        <td class="portlet-table-font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=addContent</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_CONTENT')" disable-output-escaping="yes" />
          </a>
        </td>
        <td class="portlet-table-font">|</td>
        <td class="portlet-table-font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=showImport</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.IMPORT_SCHEDS_CONTENT')" disable-output-escaping="yes" />
          </a>
        </td>
        <td class="portlet-table-font">|</td>
        <td class="portlet-table-font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=listSchedules</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.REFRESH')" disable-output-escaping="yes" />
          </a>
        </td>
        <td class="portlet-table-font">|</td>
        <td class="portlet-table-font">
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="$baseUrl" />
              <xsl:text>&amp;schedulerAction=listSubscriptions</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SHOW_SUBS')" disable-output-escaping="yes" />
          </a>
        </td>
      </tr>
    </table>

      </td>
   </tr>
</table>


    <br/>
  </xsl:template>
  
  <xsl:template match="listContent">
    <xsl:param name="title" />
    <xsl:param name="actions" />

    <xsl:call-template name="messages" />

	<table border="0" width="95%" class="content_header" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td width="100%"><xsl:value-of select="$title" disable-output-escaping="yes" /></td>
		</tr>
	</table>

	<table border="0" class="content_container2" width="95%" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
		<tr>
			<td>
			</td>
		</tr>
	</table>

    <span class="portlet-font">
      <xsl:value-of select="message/text()" disable-output-escaping="yes" />
    </span>
      <table width="100%" border="0" cellpadding="0" cellspacing="0" style="padding-left:10px;padding-right:10px">
        <tr>
          <td class="portlet-table-header">
            <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION_SEQUENCE')" disable-output-escaping="yes" />
          </td>
          <xsl:if test="$actions != ''" >          
            <td colspan="2" class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION')" disable-output-escaping="yes" />
             </td>
          </xsl:if>
        </tr>
        <xsl:for-each select="content">
          <tr>
          <xsl:call-template name="content" />
          <xsl:if test="$actions = 'content-actions'" >
             <xsl:call-template name="content-actions"/>
          </xsl:if>
          <xsl:if test="$actions = 'schedule-content-actions'" >
             <xsl:call-template name="schedule-content-actions"/>
          </xsl:if>
          </tr>
        </xsl:for-each>
      </table>
    <p/>
  </xsl:template>

  <xsl:template name="content">
      <td class="portlet-table-text">
        <xsl:value-of select="actionRef" />
      </td>
  </xsl:template>

  <xsl:template name="content-actions" >
    <td class="portlet-table-text">
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$baseUrl" />
          <xsl:text>&amp;schedulerAction=editContent&amp;contentId=</xsl:text>
          <xsl:value-of select="@contentId" />
          <xsl:text>&amp;actionRef=</xsl:text>
          <xsl:value-of select="actionRef" />
        </xsl:attribute>
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.EDIT')" disable-output-escaping="yes" />
      </a> 
	  |
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$baseUrl" />
          <xsl:text>&amp;schedulerAction=doDeleteContent&amp;contentId=</xsl:text>
          <xsl:value-of select="@contentId" />
          <xsl:text>&amp;actionRef=</xsl:text>
          <xsl:value-of select="actionRef" />
        </xsl:attribute>
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DELETE')" disable-output-escaping="yes" />
      </a>
    </td>
  </xsl:template>
        
  <xsl:template name="schedule-content-actions" >
    <td class="portlet-table-text">
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$baseUrl" />
          <xsl:text>&amp;schedulerAction=doDeleteContentForSchedule&amp;contentId=</xsl:text>
          <xsl:value-of select="@contentId" />
          <xsl:text>&amp;schedId=</xsl:text>
          <xsl:value-of select="schedId" />
          <xsl:text>&amp;schedRef=</xsl:text>
          <xsl:value-of select="schedRef" />
          <xsl:text>&amp;actionRef=</xsl:text>
          <xsl:value-of select="actionRef" />
        </xsl:attribute>
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.REMOVE_CONTENT')" disable-output-escaping="yes" />
      </a>
    </td>
  </xsl:template>
        
  <xsl:template match="addContent">
  
    		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_CONTENT')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>

    <br/>
    <span class="portlet-font">
      <xsl:text>
          Use this page to select subscription content.  Action Sequences that are checked will be subscribable.
      </xsl:text>
    </span>
    <p/>

    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <table>
      <form method="post">
        <xsl:attribute name="action"><xsl:value-of select="$baseUrl" /></xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="SetContent" /></xsl:attribute>
      
        <table width="100%" border="0" cellpadding="5px" cellspacing="0" style="padding-left:5px;padding-right:5px">
		          
		  <tr>
			<td>
		<xsl:call-template name="list-folders" />

			</td>
		</tr>
       </table>
    
      <tr><td></td><td><input type="SUBMIT" value="Set Content"/></td></tr>
      <input name="schedulerAction" type="hidden" value="doSetContent" />
    </form>
    </table>
    <p/>
  </xsl:template>

	<xsl:template name="list-folders">

    <xsl:for-each select="listContent/folder">



<img border="0" src="/pentaho-style/images/btn_plus.png" style="padding-right:5px">
        <xsl:attribute name="onclick">var div=document.getElementById('div<xsl:value-of select="position()"/>'); if(div.style.display=='none') { div.style.display='block'; this.src='/pentaho-style/images/btn_minus.png'; } else { div.style.display='none'; this.src='/pentaho-style/images/btn_plus.png'; } return false;</xsl:attribute>
      </img>
      <xsl:value-of select="./@name"/>
      <br/>
      <xsl:text disable-output-escaping="yes">&lt;div id="div</xsl:text><xsl:value-of select="position()"/><xsl:text disable-output-escaping="yes">" style="border:1px solid #bbbbbb;display:none;position:relative;left:12px;">&lt;table></xsl:text>

    <xsl:call-template name="list-files-in-folder" />

    <xsl:text disable-output-escaping="yes">&lt;/table>&lt;/div></xsl:text>

    </xsl:for-each>


  </xsl:template>

<xsl:template name="list-files-in-folder">

<xsl:for-each select="./content">

    <tr>
      <td>
              <input type="checkbox" name="actionRef" >
                <xsl:attribute name="value">
                    <xsl:if test="../@name != ''">
                      <xsl:value-of select="../@name" /><xsl:text>/</xsl:text>
                    </xsl:if>
                      <xsl:value-of select="./text()" />
                  
                </xsl:attribute>
                <xsl:if test="./@selected='true'">
                  <xsl:attribute name="checked">
                    <xsl:value-of select="./@selected" />
                  </xsl:attribute>
                </xsl:if>
              </input>
            </td>
            <td>
      <xsl:call-template name="getName">
        <xsl:with-param name="actionRef" select="./text()"/>
      </xsl:call-template>
      </td>
    </tr>

</xsl:for-each>

</xsl:template>


	<xsl:template name="getPath">
		<xsl:param name="actionRef"/>
	
		<xsl:if test="contains( $actionRef, '/' )">
			<xsl:value-of select="substring-before($actionRef,'/')"/> : 
		</xsl:if>
		<xsl:if test="contains( $actionRef, '/' )">
			<xsl:call-template name="getPath">
				<xsl:with-param name="actionRef" select="substring-after($actionRef,'/')"/>
			</xsl:call-template>
		</xsl:if>
				
	</xsl:template>	

	<xsl:template name="getName">
		<xsl:param name="actionRef"/>
	
		<xsl:choose>
			<xsl:when test="contains( $actionRef, '/' )">
			</xsl:when>
			<xsl:otherwise>
			<xsl:value-of select="$actionRef"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="contains( $actionRef, '/' )">
			<xsl:call-template name="getName">
				<xsl:with-param name="actionRef" select="substring-after($actionRef,'/')"/>
			</xsl:call-template>
		</xsl:if>
				
	</xsl:template>	

  <xsl:template match="editSchedule">

  		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE_EDITOR')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>

    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USE_THIS_PAGE')" disable-output-escaping="yes" />
    </span>
    <p/>
    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <table>
    <form method="post">
      <xsl:call-template name="schedule-form" />
      <tr><td></td><td>
      <input type="SUBMIT" name="editModify" value="Modify"/>
      <input type="SUBMIT" name="editDelete" value="Delete"/>
      <input type="SUBMIT" name="editAdd" value="Add New"/>
      </td></tr>
      <input name="schedulerAction" type="hidden" value="doEditSchedule" />
    </form>
    </table>
    
    <br/>    
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$baseUrl" />
        <xsl:text>&amp;schedulerAction=addContentForSchedule&amp;schedId=</xsl:text>
        <xsl:value-of select="schedId" />
        <xsl:text>&amp;schedRef=</xsl:text>
        <xsl:value-of select="schedRef" />
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_CONTENT_FOR_SCHEDULE')" disable-output-escaping="yes" />
    </a> 
       
      <p/><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.LAST_TRIGGER_COLON')" disable-output-escaping="yes" />
      <xsl:value-of select="prevFireTime" disable-output-escaping="yes" />

      <br/><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NEXT_TRIGGER_COLON')" disable-output-escaping="yes" />
      <xsl:value-of select="nextFireTime" disable-output-escaping="yes" />

    <p/>
    <xsl:apply-templates select="subscriptions" />
    <p/>
    

    <xsl:choose>
      <xsl:when test="listContent/@count = '0'">
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NO_CONTENT_ALLOWED')" disable-output-escaping="yes" />
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="listContent" >
          <xsl:with-param name="title" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CONTENT_ALLOWED')" />
          <xsl:with-param name="actions" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE_CONTENT_ACTION')" />
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
    <p/>
  </xsl:template>

  <xsl:template match="addSchedule">
  
		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ADD_NEW_SCHEDULE')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>

    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USE_PAGE_TO_ADD_NEW_SCHEDULE')" disable-output-escaping="yes" />
    </span>
    <p/>

    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <table>
    <form method="post">
      <xsl:call-template name="schedule-form" />
      <tr><td></td><td><input type="SUBMIT" value="Add"/></td></tr>
      <input name="schedulerAction" type="hidden" value="doAddSchedule" />
    </form>
    </table>
  </xsl:template>

  <xsl:template match="addContentForSchedule">
  
		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE_EDITOR')"/>
			<xsl:with-param name="url3">
				<xsl:text>SubscriptionAdmin?schedulerAction=editSchedule&amp;schedId=</xsl:text><xsl:value-of select="schedId"/><xsl:text>&amp;jobId=</xsl:text><xsl:value-of select="schedRef"/>
			</xsl:with-param>
			<xsl:with-param name="crumb4">
				<xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_CONTENT_FOR_DASH')" disable-output-escaping="yes" />
				<xsl:value-of select="schedRef" disable-output-escaping="yes" />
			</xsl:with-param>
			<xsl:with-param name="url4" select="''"/>
		</xsl:call-template>
  
    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USE_PAGE_TO_SELECT_CONTENT')" disable-output-escaping="yes" />
    </span>
    <p/>

    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />
    
    <span class="portlet-section-subheader">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CHECK_CONTENT')" disable-output-escaping="yes" />
    </span>
   <form method="post">
     <xsl:attribute name="action"><xsl:value-of select="$baseUrl" /></xsl:attribute>
     <xsl:attribute name="name"><xsl:value-of select="schedId" /></xsl:attribute>
      
        <table width="95%" border="0" cellpadding="5px" cellspacing="0">
          <tr>  
            <td class="portlet-table-header">
              <xsl:value-of select="''" disable-output-escaping="yes" />
            </td>
            <td class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION_SEQUENCE')" disable-output-escaping="yes" />
            </td>
          </tr>
          
          <xsl:for-each select="listContent/content">
            <tr>
              <td>
                <input type="checkbox" name="contentId" >
                  <xsl:attribute name="value">
                    <xsl:value-of select="@contentId" />
                  </xsl:attribute>
                  <xsl:if test="@selected='true'">
                    <xsl:attribute name="checked">
                      <xsl:value-of select="@selected" />
                    </xsl:attribute>
                  </xsl:if>
                </input>
              </td>
              <td><xsl:value-of select="actionRef" /></td>
            </tr>
          </xsl:for-each>
        </table>


      <tr><td></td><td><input type="SUBMIT" value="Submit"/></td></tr>
      <input name="schedId" type="hidden">
        <xsl:attribute name="value"><xsl:value-of select="schedId" /></xsl:attribute>  
      </input>
      <input name="schedRef" type="hidden">
        <xsl:attribute name="value"><xsl:value-of select="schedRef" /></xsl:attribute>  
      </input>
      <input name="schedulerAction" type="hidden" value="doAddContentForSchedule" />
    </form>
 
  </xsl:template>

  <xsl:template name="schedule-form">
    <xsl:attribute name="action"><xsl:value-of select="$baseUrl" /></xsl:attribute>
    <xsl:attribute name="name"><xsl:value-of select="schedId" /></xsl:attribute>
      
    <input name="schedId" type="hidden">
      <xsl:attribute name="value"><xsl:value-of select="schedId" /></xsl:attribute>  
    </input>
     
    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.GROUP_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="group" type="text">
      <xsl:attribute name="value"><xsl:value-of select="group" /></xsl:attribute>
    </input></td></tr>
      
    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NAME_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="schedRef" type="text" >
      <xsl:attribute name="value"><xsl:value-of select="schedRef" /></xsl:attribute>
    </input></td>
    </tr>

    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="title" type="text" >
      <xsl:attribute name="value"><xsl:value-of select="title" /></xsl:attribute>
    </input></td></tr>

    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DESCRIPTION_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="desc" type="text">
      <xsl:attribute name="value"><xsl:value-of select="desc" /></xsl:attribute>
    </input></td></tr>
      
    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CRON_EXP_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="cron" type="text">
      <xsl:attribute name="value"><xsl:value-of select="cron" /></xsl:attribute>
    </input></td></tr>
      
  </xsl:template>
  
  <xsl:template match="editContent">
  
  		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUB_CONTENT_EDITOR')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>
  
    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USE_PAGE_TO_EDIT')" disable-output-escaping="yes" />
    </span>
    <p/>
    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <table>
    <form method="post">
      <xsl:call-template name="content-form" />
      <tr><td></td><td>
      <input type="SUBMIT" name="editModify" value="Modify"/>
      <input type="SUBMIT" name="editDelete" value="Delete"/>
      <input type="SUBMIT" name="editAdd" value="Add New"/>
      </td></tr>
      <input name="schedulerAction" type="hidden" value="doEditContent" />
    </form>
    </table>

    <br/>    
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$baseUrl" />
        <xsl:text>&amp;schedulerAction=addScheduleForContent&amp;contentId=</xsl:text>
        <xsl:value-of select="@contentId" />
        <xsl:text>&amp;actionRef=</xsl:text>
        <xsl:value-of select="actionRef" />
      </xsl:attribute>
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_SCHED_FOR_CONTENT')" disable-output-escaping="yes" />
    </a>

    <p/>
    <xsl:apply-templates select="schedules" />
    
  </xsl:template>
  
  <xsl:template match="schedules">
    <xsl:choose>
      <xsl:when test="@count &gt; 0">  
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHED_FOR_CONTENT')" disable-output-escaping="yes" />
        </span>
        <center>
          <table width="95%" border="0" cellpadding="5px" cellspacing="0">
            <tr>
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.GROUP')" disable-output-escaping="yes" />
              </td>
    
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NAME')" disable-output-escaping="yes" />
              </td>
    
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE.TITLE')" disable-output-escaping="yes" />
              </td>
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DESCRIPTION')" disable-output-escaping="yes" />
              </td>
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION')" disable-output-escaping="yes" />
              </td>
            </tr>
            <xsl:for-each select="schedule">
              <xsl:sort select="group" />
              <xsl:sort select="schedRef" />
              <xsl:call-template name="schedule" />
            </xsl:for-each>
          </table>
        </center>
      </xsl:when>
      <xsl:otherwise>      
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NO_CONTENT_SCHED')" disable-output-escaping="yes" />
        </span>
      </xsl:otherwise>
    </xsl:choose>
    <p/>
  </xsl:template>
  
  <xsl:template name="schedule">
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
        <xsl:value-of select="group" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="schedRef" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="title" />
      </td>
      <td class="portlet-table-text">
        <xsl:value-of select="desc" />
      </td>
      <td class="portlet-table-text">
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="$baseUrl" />
          <xsl:text>&amp;schedulerAction=doDeleteContentForSchedule&amp;contentId=</xsl:text>
          <xsl:value-of select="contentId" />
          <xsl:text>&amp;schedId=</xsl:text>
          <xsl:value-of select="schedId" />
          <xsl:text>&amp;schedRef=</xsl:text>
          <xsl:value-of select="schedRef" />
          <xsl:text>&amp;actionRef=</xsl:text>
          <xsl:value-of select="actionRef" />
        </xsl:attribute>
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.REMOVE_THIS_SCHEDULE')" disable-output-escaping="yes" />
      </a>
    </td>
    </tr>
  </xsl:template>

  <xsl:template match="addScheduleForContent">
  
		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUB_CONTENT_EDITOR')"/>
			<xsl:with-param name="url3">
				<xsl:text>SubscriptionAdmin?schedulerAction=editContent&amp;contentId=</xsl:text><xsl:value-of select="contentId"/><xsl:text>&amp;actionRef=</xsl:text><xsl:value-of select="actionRef"/>
			</xsl:with-param>
			<xsl:with-param name="crumb4">
				<xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_SCHED_CONTENT_DASH')" disable-output-escaping="yes" />
				<xsl:value-of select="schedRef" disable-output-escaping="yes" />
			</xsl:with-param>
			<xsl:with-param name="url4" select="''"/>
		</xsl:call-template>
  
	<br/>
    <span class="portlet-section-header">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SET_SCHED_CONTENT_DASH')" disable-output-escaping="yes" />
      <xsl:value-of select="actionRef" disable-output-escaping="yes" />
    </span>
    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USE_PAGE_SCHED_AS')" disable-output-escaping="yes" />
    </span>
    <p/>

    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />
    
    <span class="portlet-section-subheader">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.CHECK_SCHED_FOR_AS')" disable-output-escaping="yes" />
    </span>
   <form method="post">
     <xsl:attribute name="action"><xsl:value-of select="$baseUrl" /></xsl:attribute>
     <xsl:attribute name="name"><xsl:value-of select="contentId" /></xsl:attribute>
      
        <table width="95%" border="0" cellpadding="5px" cellspacing="0">
          <tr>  
            <td class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE')" disable-output-escaping="yes" />
            </td>
            <td class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.GROUP')" disable-output-escaping="yes" />
            </td>
  
            <td class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NAME')" disable-output-escaping="yes" />
            </td>
  
            <td class="portlet-table-header">
              <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE.TITLE')" disable-output-escaping="yes" />
            </td>
          </tr>
          
          <xsl:for-each select="listSchedules/schedule">
            <tr>
              <td>
                <input type="checkbox" name="schedId" >
                  <xsl:attribute name="value">
                    <xsl:value-of select="schedId" />
                  </xsl:attribute>
                  <xsl:if test="@selected='true'">
                    <xsl:attribute name="checked">
                      <xsl:value-of select="@selected" />
                    </xsl:attribute>
                  </xsl:if>
                </input>
              </td>
              <td><xsl:value-of select="group" /></td>
              <td><xsl:value-of select="schedRef" /></td>
              <td><xsl:value-of select="title" /></td>
            </tr>
          </xsl:for-each>
        </table>


      <tr><td></td><td><input type="SUBMIT" value="Submit"/></td></tr>
      <input name="contentId" type="hidden">
        <xsl:attribute name="value"><xsl:value-of select="contentId" /></xsl:attribute>  
      </input>
      <input name="actionRef" type="hidden">
        <xsl:attribute name="value"><xsl:value-of select="actionRef" /></xsl:attribute>  
      </input>
      <input name="schedulerAction" type="hidden" value="doAddScheduleForContent" />
    </form>
 
  </xsl:template>

  <xsl:template name="content-form">
    <xsl:attribute name="action"><xsl:value-of select="$baseUrl" /></xsl:attribute>
    <xsl:attribute name="name"><xsl:value-of select="contentId" /></xsl:attribute>
      
    <input name="contentId" type="hidden">
      <xsl:attribute name="value"><xsl:value-of select="@contentId" /></xsl:attribute>  
    </input>
      
    <tr><td><xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION_SEQUENCE_COLON')" disable-output-escaping="yes" /></td>
    <td><input name="actionRef" type="text" size="75">
      <xsl:attribute name="value"><xsl:value-of select="actionRef" /></xsl:attribute>
    </input></td></tr>
  </xsl:template>

  <xsl:template match="listSubscriptions">
  
  		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString('UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE')"/>
			<xsl:with-param name="url2" select="'SubscriptionAdmin?schedulerAction=listSchedules'"/>
			
			<xsl:with-param name="crumb3" select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUBSCRIPTIONS')"/>
			<xsl:with-param name="url3" select="''"/>
		</xsl:call-template>

    <br/>
    <span class="portlet-font">
      <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.MANAGE_USER_SUBSCRIPTIONS')" disable-output-escaping="yes" />
    </span>
    <p/>
    <xsl:call-template name="messages" />
    <xsl:call-template name="paramErrors" />
    <xsl:call-template name="exceptions" />

    <xsl:apply-templates select="subscriptions" />
    <p/>
    
  </xsl:template>

  <xsl:template match="subscriptions">
    <xsl:choose>
      <xsl:when test="count(subscription) &gt; 0">  
        <span class="portlet-section-subheader">
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SUBSCRIPTIONS')" disable-output-escaping="yes" />
        </span>
          <table width="95%" border="0" cellpadding="5px" cellspacing="0">
            <tr>
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.USER')" disable-output-escaping="yes" />
              </td>
    
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.TITLE.TITLE')" disable-output-escaping="yes" />
              </td>
    
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION_SEQUENCE')" disable-output-escaping="yes" />
              </td>
              
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.SCHEDULE')" disable-output-escaping="yes" />
              </td>
              
              <td class="portlet-table-header">
                <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.ACTION')" disable-output-escaping="yes" />
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
          <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.NO_SUBSCRIPTIONS_DEFINED')" disable-output-escaping="yes" />
        </span>
      </xsl:otherwise>
    </xsl:choose>
    <p/>
  </xsl:template>

  <xsl:template name="subscription">
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
        <xsl:value-of select="user" />
      </td>
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
        <xsl:value-of select="msg:getXslString('UI.SUBSCRIPTION_ADMIN.DELETE')" disable-output-escaping="yes" />
      </a>
    </td>
    </tr>
  </xsl:template>

        
</xsl:stylesheet>
