<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
    xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="html msg">

	<xsl:include href="system/custom/xsl/xslUtil.xsl" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:param name="href" select="''"/>
	<xsl:param name="baseUrl" select="''"/>
	<xsl:param name="onClick" select="''"/>

	<xsl:template match="isSchedulerPaused">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:call-template name="header"/>
		<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_PAUSE_STATUS')" disable-output-escaping="yes"/><xsl:value-of select="@schedulerResults"/>
		<xsl:call-template name="actions"/>
	
	</xsl:template>

	<xsl:template match="resumeScheduler">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:call-template name="header"/>
		<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_RESUME_RESULT')" disable-output-escaping="yes"/><xsl:value-of select="@schedulerResults"/>
		<xsl:call-template name="actions"/>
	
	</xsl:template>

	<xsl:template match="suspendScheduler">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:call-template name="header"/>
		<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_SUSPEND_RESULT')" disable-output-escaping="yes"/><xsl:value-of select="@schedulerResults"/>
		<xsl:call-template name="actions"/>
	
	</xsl:template>

	<xsl:template match="getJobNames">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:call-template name="header"/>

		<center>
		<table width="95%" border="0" cellpadding="5px" cellspacing="0">
		<tr>
			<td class="portlet-table-header">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_JOB_GROUP_NAME')" disable-output-escaping="yes"/>
			</td>
			
			<td class="portlet-table-header">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_GROUP_NAME')" disable-output-escaping="yes"/>
			</td>

			<td class="portlet-table-header">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_JOB_DESCRIPTION')" disable-output-escaping="yes"/>
			</td>

			<td class="portlet-table-header">
 				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_FIRE_TIME_NAME')" disable-output-escaping="yes"/>
			</td>
			
			<td class="portlet-table-header">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE')" disable-output-escaping="yes"/>
			</td>
			<td class="portlet-table-header">
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_ACTION')" disable-output-escaping="yes"/>
			</td>			
		</tr>
		<xsl:for-each select="job">
			<tr>
				<xsl:call-template name="job">
				</xsl:call-template>
			</tr>
		</xsl:for-each>
		</table>
		</center>
		<xsl:call-template name="actions"/>
	</xsl:template>

	<xsl:template name="job">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<td class="portlet-table-text">
			<xsl:value-of select="@jobGroup"/>
			<br/>
			<xsl:value-of select="@jobName"/>
		</td>
		<td class="portlet-table-text">
			<xsl:value-of select="@triggerGroup"/>
		<br/>
			<xsl:value-of select="@triggerName"/>
		</td>
		<td class="portlet-table-text">
			<xsl:value-of select="description"/>
		</td>
		<td class="portlet-table-text">
			<xsl:value-of select="@prevFireTime"/>
			<br/>
			<xsl:value-of select="@nextFireTime"/>
		</td>
		<td class="portlet-table-text">
			<xsl:choose>
				<xsl:when test = "@triggerState = 0">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_NORMAL')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:when test = "@triggerState = 1">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_PAUSED')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:when test = "@triggerState = 2">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_COMPLETE')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:when test = "@triggerState = 3">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_ERROR')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:when test = "@triggerState = 4">
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_BLOCKED')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_TRIGGER_STATE_NONE')" disable-output-escaping="yes"/>
				</xsl:otherwise>
			</xsl:choose>
		</td>
		<td class="portlet-table-text">
			<a>
				<xsl:choose>
					<xsl:when test = "@triggerState = 0">
						<xsl:attribute name="href">
							<xsl:value-of select="$baseUrl"/>				
							<xsl:text>&amp;schedulerAction=pauseJob&amp;jobName=</xsl:text>
							<xsl:value-of select="@jobName"/>
							<xsl:text>&amp;jobGroup=</xsl:text>
							<xsl:value-of select="@jobGroup"/>
						</xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_SUSPEND')" disable-output-escaping="yes"/>
					</xsl:when>
					<xsl:when test = "@triggerState = 1">
						<xsl:attribute name="href">
							<xsl:value-of select="$baseUrl"/>				
							<xsl:text>&amp;schedulerAction=resumeJob&amp;jobName=</xsl:text>
							<xsl:value-of select="@jobName"/>
							<xsl:text>&amp;jobGroup=</xsl:text>
							<xsl:value-of select="@jobGroup"/>
						</xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_RESUME')" disable-output-escaping="yes"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>-</xsl:text>
					</xsl:otherwise>
				</xsl:choose>		
			</a>
			<br/>
			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$baseUrl"/>
					<xsl:text>&amp;schedulerAction=deleteJob&amp;jobName=</xsl:text>
					<xsl:value-of select="@jobName"/>
					<xsl:text>&amp;jobGroup=</xsl:text>
					<xsl:value-of select="@jobGroup"/>
				</xsl:attribute>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_DELETE')" disable-output-escaping="yes"/>
			</a>
			<br/>
			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$baseUrl"/>
					<xsl:text>&amp;schedulerAction=executeJob&amp;jobName=</xsl:text>
					<xsl:value-of select="@jobName"/>
					<xsl:text>&amp;jobGroup=</xsl:text>
					<xsl:value-of select="@jobGroup"/>
				</xsl:attribute>
				<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_EXECUTE_JOB')" disable-output-escaping="yes"/>
			</a>			
		</td>
	</xsl:template>

	<xsl:template name="header">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:call-template name="breadcrumbing">
			<xsl:with-param name="crumb1" select="msg:getXslString($messages, 'UI.USER_ADMIN')"/>
			<xsl:with-param name="url1" select="'Admin'"/>
			<xsl:with-param name="crumb2" select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ADMINISTRATION')"/>
			<xsl:with-param name="url2" select="'javascript:void'"/>
		</xsl:call-template>
	
		<span class="portlet-font">
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ADMIN_HELP')" disable-output-escaping="yes"/>
		</span>
		<p/>
		
	</xsl:template>

	<xsl:template name="actions">
	<xsl:variable name="messages" select="msg:getInstance()" />

		<xsl:variable name="statusUrl">
			<xsl:value-of select="$baseUrl" />
			<xsl:text>&amp;schedulerAction=isSchedulerPaused</xsl:text>
		</xsl:variable>

		<xsl:variable name="resumeUrl">
			<xsl:value-of select="$baseUrl" />
			<xsl:text>&amp;schedulerAction=resumeScheduler</xsl:text>
		</xsl:variable>

		<xsl:variable name="suspendUrl">
			<xsl:value-of select="$baseUrl" />
			<xsl:text>&amp;schedulerAction=suspendScheduler</xsl:text>
		</xsl:variable>

		<xsl:variable name="jobsUrl">
			<xsl:value-of select="$baseUrl" />
			<xsl:text>&amp;schedulerAction=getJobNames</xsl:text>
		</xsl:variable>
		<p/>
		<table border="0" cellpadding="5px">
			<tr>
				<td>
					<xsl:value-of select="msg:getXslString($messages, 'UI.USER_ACTIONS')"/>
				</td>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$statusUrl" /></xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_STATUS')" disable-output-escaping="yes"/>
						|
					</a>
				</td>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$resumeUrl" /></xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_RESUME')" disable-output-escaping="yes"/>
						|
					</a>
				</td>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$suspendUrl" /></xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_SUSPEND')" disable-output-escaping="yes"/>
						|
					</a>
				</td>
				<td>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$jobsUrl" /></xsl:attribute>
						<xsl:value-of select="msg:getXslString($messages, 'UI.USER_SCHEDULER_ACTION_LIST')" disable-output-escaping="yes"/> 
					</a>
				</td>
			</tr>
		</table>
		<br/>
	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>