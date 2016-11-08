<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" 
	xmlns:html="http://www.w3.org/TR/REC-html40"
    xmlns:msg="org.pentaho.platform.plugin.action.messages.Messages"
	exclude-result-prefixes="html msg">
        
	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template name="doSubscriptions">
		<xsl:variable name="messages" select="msg:getInstance()" />
	
		<xsl:variable name="editing">
			<xsl:if test="/filters/input[@name='subscribe-title']/@value!=''">
				<xsl:text>true</xsl:text>
			</xsl:if>
		</xsl:variable>
					
         <xsl:if test="/filters/subscriptions/@valid-session='true'">
		

                

					<tr>
						<td class="portlet-font" colspan="2">
                        <table cellpadding="8" border="0">
                            <tr>
                              <td class="portlet-font"  style="padding:3px" >
                               <table class="parameter_table" border="0" cellpadding="0" cellspacing="0">
                                    <tr>
                                       <td>                           
                         <fieldset class="parameter_fieldset">
                              <legend>
                                  Schedule This Report
                              </legend>

						<form>
							<xsl:attribute name="name">save_form_<xsl:value-of select="/filters/id" /></xsl:attribute>
							<xsl:if test="$USEPOSTFORFORMS='true'">
								<xsl:attribute name="method">post</xsl:attribute>
					            <input type="hidden" name="subscribe" value="save" />
                                <input type="hidden" name="_PENTAHO_ADDITIONAL_PARAMS_" />
							</xsl:if>

							<table width="100%" border="0" >

								<tr>
									<td class="portlet-font" colspan="1" style="white-space:nowrap">
										<br/><xsl:value-of select="msg:getXslString($messages, 'UI.PARAM_FORM_UTIL.REPORT_NAME')" disable-output-escaping="yes"/>
									</td>
									<td class="portlet-font" colspan="2">
										<br/>
										<xsl:element name="script">
                      <xsl:attribute name="type">text/javascript</xsl:attribute>
                      function detectEnter(e)
                        {
                          if (null == e)
                            e = window.event ;
                          if (e.keyCode == 13)  {
                            document.getElementById("savesubmit").click();
                            return false;
                          }
                        }
                    </xsl:element>
										<input name="subscribe-name" class="portlet-form-field" size="50">
											<xsl:attribute name="value"><xsl:value-of select="/filters/input[@name='subscribe-title']/@value"/></xsl:attribute>
											<xsl:attribute name="onkeypress">return detectEnter(event);</xsl:attribute>
											<xsl:attribute name="onkeyup">rptnmlimit(this,25)</xsl:attribute>
										</input>
									</td>
								</tr>
								<!-- now do any schedules -->
								<xsl:if test="count(/filters/schedules/schedule) > 0">
								<tr><td><xsl:text>&#xA0;</xsl:text></td></tr>
								<tr>
									
									<td class="portlet-font" colspan="1" style="white-space:nowrap">					
											Schedule For:<xsl:text>&#xA0;</xsl:text>
                                    </td>
                                    <td>
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
                                    <td align="left" width="100%" >
                                    
										<input type="button" name="save" id="savesubmit" class="portlet-form-button">
											<xsl:attribute name="value">Save</xsl:attribute>
											<xsl:attribute name="onClick">doSave('<xsl:value-of select="/filters/id" />', '<xsl:value-of select="/filters/action"/>', true );</xsl:attribute>
										</input>
                                    </td>
								</tr>
							</xsl:if>
							
							</table>

						</form>
                        </fieldset>
                        </td></tr>
                        </table>
					</td></tr>
                    </table>
                    </td></tr>
					<!-- /div -->
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