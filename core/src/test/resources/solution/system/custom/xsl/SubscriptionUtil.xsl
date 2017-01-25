<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
    xmlns:msg="org.pentaho.platform.util.messages.Messages"
	exclude-result-prefixes="html msg">
        
	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template name="doSubscriptions">

		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:variable name="editing">
			<xsl:if test="/filters/input[@name='subscribe-title']/@value!=''">
				<xsl:text>true</xsl:text>
			</xsl:if>
		</xsl:variable>
		
					<tr>
					<td class="portlet-font" colspan="2">
						<div> <!-- divSchMsg -->
							<xsl:attribute name="id">divSchMsg<xsl:value-of select="/filters/id" /></xsl:attribute>
							<xsl:attribute name="style">display:block</xsl:attribute>

							<!--Changes by Marc to highlight the scheduling options-->
							<xsl:if test="/filters/subscriptions/@valid-session='true'">
                            <!-- <xsl:if test="count(/filters/schedules/schedule) > 0"> -->
              


			<table width="100%" border="0" style="margin-top:10px;">
			  <tr>
			    <td class="portlet-section-subheader">Schedule This Report</td>
        </tr>
			  <tr>
				<td>
					<input type="button" name="subscribe1button" class="portlet-form-button">
						<xsl:attribute name="value">Show Scheduling Options</xsl:attribute>
						<xsl:attribute name="onClick">showSubscribe('<xsl:value-of select="/filters/id" />')</xsl:attribute>
						<xsl:attribute name="id">subscribe1button<xsl:value-of select="/filters/id" /></xsl:attribute>
  						<xsl:choose>
								<xsl:when test="$editing='true'">
									<xsl:attribute name="style">display:none</xsl:attribute>
								</xsl:when>
                </xsl:choose>
					</input>
				</td>
			  </tr>
	</table>

				
			</xsl:if>
			<!--End of changes for scheduling options-->
<!-- old end run3div -->
</div> <!-- /divSchMsg -->

						</td>
					</tr>


					<xsl:if test="/filters/subscriptions/@valid-session='true'">

					<tr>
						<td class="portlet-font" colspan="2">
						<div style="display:none"> <!-- subscribe-div -->
						<xsl:attribute name="id">subscribe-div<xsl:value-of select="/filters/id" /></xsl:attribute>
							<xsl:choose>
								<xsl:when test="$editing='true'">
									<xsl:attribute name="style">display:block</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="style">display:none</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>

						<form>
							<xsl:attribute name="name">save_form_<xsl:value-of select="/filters/id" /></xsl:attribute>
							<xsl:if test="$USEPOSTFORFORMS='true'">
								<xsl:attribute name="method">post</xsl:attribute>
					            <input type="hidden" name="subscribe" value="save" />
                                <input type="hidden" name="_PENTAHO_ADDITIONAL_PARAMS_" />
							</xsl:if>

							<table width="100%" border="0" >

								<tr>
									<td class="portlet-font" colspan="1">
										<br/><xsl:value-of select="msg:getXslString($messages, 'UI.PARAM_FORM_UTIL.REPORT_NAME')" disable-output-escaping="yes"/>
									</td>
									<td class="portlet-font" colspan="1">
										<br/>
										<input name="subscribe-name" class="portlet-form-field" size="50">
											<xsl:attribute name="value"><xsl:value-of select="/filters/input[@name='subscribe-title']/@value"/></xsl:attribute>
											<xsl:attribute name="onkeyup">rptnmlimit(this,25)</xsl:attribute>
										</input>
									</td>
								</tr>
<!-- 
								<tr>
									<td class="portlet-font" colspan="1">

										<br/><xsl:text>Send this report to </xsl:text>
									</td>
									<td class="portlet-font" colspan="1">
										<br/>
										<input name="destination" class="portlet-form-field" size="50">
											<xsl:attribute name="value"><xsl:value-of select="/filters/input[@name='destination']/@value"/></xsl:attribute>
										</input>
									</td>
								</tr>
-->
								<!-- now do any schedules -->
								<xsl:if test="count(/filters/schedules/schedule) > 0">
								<tr><td><xsl:text>&#xA0;</xsl:text></td></tr>
								<tr>
									<td class="portlet-font" colspan="1">
										Run this report:
									</td>
									
									<td class="portlet-font" colspan="1">					
											Schedule For:<xsl:text>&#xA0;</xsl:text>
							
											<select>
												<xsl:attribute name="id">monthly-schedule-selection-<xsl:value-of select="/filters/id" /></xsl:attribute>
												<option value="noMonthly">Schedule Options...</option>
												<xsl:for-each select="/filters/schedules/schedule">
                                                    <xsl:sort select="group" />
                                                    <xsl:sort select="schedRef" />
													<!-- xsl:if test="starts-with( title, 'Month' )" -->
													<!-- <xsl:if test="starts-with( translate( title, 'L123456789', 'XXXXXXXXXX'), 'X' )"> -->
														<option>
															<xsl:attribute name="value">schedule-<xsl:value-of select="id"/></xsl:attribute>
															<xsl:variable name="selected">
																<xsl:call-template name="schedule-selected"/>
															</xsl:variable>
															<xsl:if test="$selected='true'">
																<xsl:attribute name="selected">true</xsl:attribute>
															</xsl:if>
															<xsl:value-of select="title"/>
														</option>
													<!-- </xsl:if> -->
												</xsl:for-each>
											</select>
											
									</td>
								</tr>
							</xsl:if>
							
								<tr>
									<td class="portlet-font" colspan="1">
									</td>
									<td class="portlet-font" colspan="1">
<xsl:choose>
									<xsl:when test="$editing='true'">
										<input type="button" name="save" class="portlet-form-button">
											<xsl:attribute name="value">Save</xsl:attribute>
											<xsl:attribute name="onClick">doSave('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>', false );</xsl:attribute>
										</input>

										<input type="button" name="save" class="portlet-form-button">
											<xsl:attribute name="value">Save As</xsl:attribute>
											<xsl:attribute name="onClick">doSave('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>', true );</xsl:attribute>
										</input>
							                    <input type="button" name="cancel" class="portlet-form-button">
                      							<xsl:attribute name="value">Cancel Editing</xsl:attribute>
										<xsl:attribute name="onClick">doCancelScheduling( '<xsl:value-of select="/filters/id" />', true)</xsl:attribute>
										<xsl:attribute name="id">cancel2button<xsl:value-of select="/filters/id" /></xsl:attribute>
										</input>
									</xsl:when>
									<xsl:otherwise>
										<input type="button" name="save" class="portlet-form-button">
											<xsl:attribute name="value">Save</xsl:attribute>
											<xsl:attribute name="onClick">doSave('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>', true );</xsl:attribute>
										</input>
							<input type="button" name="cancel" class="portlet-form-button">
											<xsl:attribute name="value">Close Scheduling Options</xsl:attribute>
											<xsl:attribute name="onClick">doCancelScheduling( '<xsl:value-of select="/filters/id" />', false)</xsl:attribute>
								<xsl:attribute name="id">cancel2button<xsl:value-of select="/filters/id" /></xsl:attribute>
							</input>
									</xsl:otherwise>
          </xsl:choose>
									</td>
								</tr>
							</table>

						</form></div>
					</td></tr>
					<!-- /div -->

					<xsl:if test="count(/filters/subscriptions/subscription)>0"> 
					<!--  this is the subscribe section of the parameter page -->
					<tr>
						<td colspan="3">
					<div style="display:block;border-top:0px solid #cccccc">

						<xsl:attribute name="id">subs1div<xsl:value-of select="/filters/id" /></xsl:attribute>
						<!--Div for rounded Corners-->
						<div class="roundcont">
						<div class="roundtop">
							<img src="/images/tlrpts.gif" alt="" width="15" height="15" class="corner" style="display: none" />
   						</div>
							<table width="50%" border="0">

								<tr>
									<td colspan="3">
										<h1>My Scheduled Reports</h1>
										View or manage your scheduled reports below.
									</td>
									<td>
										<xsl:text>&#x20;</xsl:text>
									</td>									
								</tr>
								<tr>
									<td class="portlet-font" nowrap="true"><b>Schedule Name</b>
									</td>
									<td class="portlet-font" nowrap="true" valign="top"  colspan="2"><b>Action</b>										
									</td>
								</tr>
								<tr>								
									<td>
										<select>
											<xsl:attribute name="id">subscription<xsl:value-of select="/filters/id" /></xsl:attribute>

											<xsl:for-each select="/filters/subscriptions/subscription">
												<xsl:call-template name="subscription"/>
											</xsl:for-each>
										</select>
									</td>
									<td class="portlet-font">
										<select >
											<xsl:attribute name="id">subscription-action<xsl:value-of select="/filters/id" /></xsl:attribute>
											<option value="run">Run Now</option>
											<option value="archive">Run and Archive</option>
											<option value="edit">Edit</option>
											<option value="delete">Delete</option>
										</select>
									</td>
									<td align="left">
										<input type="button" name="go" class="portlet-form-button">
											<xsl:attribute name="value">Go</xsl:attribute>
											<xsl:attribute name="onClick">doSubscribed('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>', '<xsl:value-of select="/filters/display"/>')</xsl:attribute>
										</input>
									</td>

								</tr>
								<xsl:if test="count(/filters/subscriptions/subscription/archives/archive)&gt;0">
								<tr>
									<td colspan="3">
										<h1>My Archived Reports</h1>
										View or manage your archived reports below.									
									</td>
									<td>
										<xsl:text>&#x20;</xsl:text>
									</td>
								</tr>
								<tr>
									<td class="portlet-font" nowrap="true" valign="top">
										<b>Report Name</b>
									</td>
									<td class="portlet-font" nowrap="true" valign="top" colspan="2">
										<b>Action</b>
									</td>
								</tr>
								<tr>		
									<td>
										<select>
											<xsl:attribute name="id">subscription-archive<xsl:value-of select="/filters/id" /></xsl:attribute>

											<xsl:for-each select="/filters/subscriptions/subscription">
												<xsl:call-template name="archive"/>
											</xsl:for-each>
										</select>
									</td>
									<td class="portlet-font">
										<select >
											<xsl:attribute name="id">subscription-archive-action<xsl:value-of select="/filters/id" /></xsl:attribute>
											<option value="view">View</option>
											<option value="delete">Delete</option>
										</select>
									</td>
									<td>
										<input type="button" name="go" class="portlet-form-button">
											<xsl:attribute name="value">Go</xsl:attribute>
											<xsl:attribute name="onClick">doSubscribedArchive('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>')</xsl:attribute>
										</input>
									</td>
								</tr>
									</xsl:if>
							</table>
							
									<!--Closing Div for rounded corners-->
									<div class="roundbottom">
											<img src="/images/blrpts.gif" alt="" width="15" height="15" class="corner" style="display: none" />
										</div>
					</div>
							
					</div>
					</td></tr>
					</xsl:if>
					</xsl:if>
	</xsl:template>

	<xsl:template name="schedule-selected">
		<xsl:variable name="target">
			<xsl:text>schedule-</xsl:text><xsl:value-of select="id"/>
		</xsl:variable>
		<xsl:value-of select="/filters/input[@name=$target]/@value"/>
	</xsl:template>

	<xsl:template name="subscription">
		<option>
			<xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
			<xsl:value-of select="title"/>
		</option>
	</xsl:template>

	<xsl:template name="archive">
		<xsl:for-each select="archives/archive">
		<option>
			<xsl:attribute name="value"><xsl:value-of select="../../id"/>:<xsl:value-of select="id"/></xsl:attribute>
			<xsl:value-of select="../../title"/> : <xsl:value-of select="date"/>
		</option>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>