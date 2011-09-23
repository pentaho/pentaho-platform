<%@ page language="java"
	import="org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.api.repository.ISolutionRepository,
	        org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.util.VersionHelper,
			org.pentaho.platform.api.ui.INavigationComponent,
    		org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.engine.services.actionsequence.ActionResource,
			org.pentaho.platform.api.engine.IActionSequenceResource,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.engine.core.output.SimpleOutputHandler,
			org.pentaho.platform.engine.services.BaseRequestHandler,
			org.pentaho.platform.api.engine.IRuntimeContext,
			org.pentaho.commons.connection.IPentahoResultSet,
			org.pentaho.platform.uifoundation.chart.ChartHelper,
			org.pentaho.platform.engine.services.solution.SolutionHelper,
			java.util.ArrayList,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder
			" %><%

/*
 * Copyright 2006 - 2010 Pentaho Corporation.  All rights reserved. 
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 *
 * @created Jul 23, 2005 
 * @author James Dixon
 * 
 */
 
 /*
 * This JSP is an example of how to use Pentaho components and AJAX library to build a 
 * Google Maps dashboard.
 * This file loads customer data and displays it using Google Maps.
 * The script for this file is in js/google-demo.js
 * The Pentaho AJAX library is in js/pentaho-ajax.js
 * See the document 'Pentaho AJAX Guide' for more details
 */

	// Set the character encoding e.g. UFT-8
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 
	// Get the current Pentaho session or create a new one if needed
	IPentahoSession userSession = PentahoSessionHolder.getSession();

	// Set the default thresholds
	int topthreshold = 100000;
	int bottomthreshold = 50000;

	// Get the server and port. We use this to check for an invalid Google Maps API key
	boolean defaultKeyInvalid = false;
	String serverName = request.getServerName();
	int serverPort = request.getServerPort();
	
	// Get a templater object
	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {

		// Load a template for this web page
		String template = null;
  		try {
	  	  byte[] bytes = IOUtils.toByteArray(ActionSequenceResource.getInputStream("system/custom/template-document.html", LocaleHelper.getLocale()));
	  	  template = new String(bytes, LocaleHelper.getSystemEncoding());
    		} catch (Throwable t) {
                  t.printStackTrace();
    		}

		// Check to see if we are using the default Google Maps API key but not for localhost:8080
		String googleMapsApiKey = PentahoSystem.getSystemSetting("google/googlesettings.xml", "google_maps_api_key", null); 
		if( ( !serverName.equals( "localhost" ) || serverPort != 8080 ) && googleMapsApiKey.equals( "ABQIAAAAoGNlMo4FkTb3mcC5mj5ERRTwM0brOpm-All5BF6PoaKBxRWWERR0378zH4HL9GyjgMMHJmj_viP4PQ" ) ) {
			// the default Google Maps API key is not valid for this server and port
			defaultKeyInvalid = true;
		} else {
			// insert the Pentaho AJAX, Google Maps, and demo script references into the document header
			template = template.replaceAll( "\\{header-content\\}", "	<script language=\"javascript\" src=\"js/pentaho-ajax.js\"></script>\n<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key="+googleMapsApiKey+"\" type=\"text/javascript\"></script>\n<script language=\"javascript\" src=\"js/google-demo.js\"></script>\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			template = template.replaceAll( "\\{body-tag\\}", "onload=\"load()\" onunload=\"GUnload()\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		// Break the template into header and footer sections
		String sections[] = templater.breakTemplateString( template, "Pentaho Google Maps Dashboard", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}

		// Load the customer data. Do this by running an action sequence defined in pentaho-solutions/steel-wheels/google/map1.xaction
		SimpleParameterProvider parameters = new SimpleParameterProvider();
		ArrayList messages = new ArrayList();
		// 'results' will store the customer data
		IPentahoResultSet results = null;
        IRuntimeContext runtime = null;
        try {
			// Run the action sequence
			runtime = SolutionHelper.doAction("", "/public/pentaho-solutions/steel-wheels/google/map1.xaction", "", "Map.jsp",  parameters,  userSession,  messages,  null );
			// See if we have a valid result
            if( runtime != null ) {
				if( runtime.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
					if( runtime.getOutputNames().contains("data") ) {
						results = runtime.getOutputParameter( "data" ).getValueAsResultSet();
					}
				}
            }
        } finally {
            if (runtime != null) {
            		// Now clean up 
                	runtime.dispose();
            }
        }

		String customerNum = "";
		String customer = "";
		String city = "";
		String state = "";
		String zip = "";
		String value = "";

%>

<%= intro %>

<% 
	if( defaultKeyInvalid ) { 
		// The default key is not valid so we put out a nice message about it.
%>

The Google Maps API key that ships with the Pentaho Pre-Configured Installation will only work with a server address of 'http://localhost:8080'. 
<p/> 
To use Google Maps with this server address ( <%= serverName %>:<%= serverPort %> ) you need to apply to Google for a new key.
<p/>
Once you have the new key you need to add it to the Google settings file in the Pentaho system (.../pentaho-solutions/system/google/googlesettings.xml)
<p/>
<a target='google-map-api-key' href='http://www.google.com/apis/maps/signup.html'>Click here</a> to get a Google Maps API Key for this server.

<% 
	} else { 
		// embed the customer data into the web page
%>

    <script type="text/javascript">

    //<![CDATA[

	function addPoints() {
    		if (GBrowserIsCompatible()) {
		<%
			// Add all of the customer data into the web page
			int n = results.getRowCount();
			for( int row=0; row<n; row++ ) {
				// Get the information about the customer in the current row
				customerNum = results.getValueAt( row, 0 ).toString();
				customer = (String) results.getValueAt( row, 1 );
				city = (String) results.getValueAt( row, 2 );
				state = (String) results.getValueAt( row, 3 );
				value = results.getValueAt( row, 5 ).toString();
				// create a javascript call that passes the customer's details
				%>
				try {
					showAddress( "<%= city %>,<%= state %>", "<%= customer %>", "<%= customerNum %>", <%= value %>, false );
				} catch (e) {
					e.printStackTrace();
				}
				<%
			}
		%>
     	}
    }

    //]]>
    </script>

	<!-- create the visual elements of the page -->

	<!-- define the thresholds panel -->
    <div id="selections" style="position:absolute;width: 345px; height: 200px;top:40px; left:5px; border:0px">
    
		<table border="0" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
    				<td valign="top">
      				<table border="0" cellpadding="0" cellspacing="0" width="100%" style="margin:0px; padding:0px">
          				<tr>
            					<td style="background-image: url(/pentaho-style/images/fly-top-left1.png); height: 25px; width: 25px;">
            						&nbsp;
            					</td>
            					<td style="background-image: url(/pentaho-style/images/fly-top1.png); width: 100%; ">
								<span style="font: normal 1.1em Tahoma, 'Trebuchet MS', Arial;">Select Sales Thresholds</span>
						</td>
                                             <!-- Keep this for a backup
            					<td width="100%" style="background-image: url(/pentaho-style/images/fly-top1.png); background-repeat: repeat-x; height: 25px; width: 100%; margin:0px; padding:0px ">
								<span class="a" style="font: normal 1.1em Tahoma, 'Trebuchet MS', Arial;">Select Sales Thresholds</span>
							</td>
                                             -->
          				</tr>
          				<tr>
            					<td colspan="2" style="background-color: #e5e5e5;">
            						<table width="100%" border="0" cellspacing="1" cellpadding="0" height="100%">
									<tr>
                  						<td style="background-image: url(/pentaho-style/images/fly-left1.png); background-repeat: repeat-y; height: 10px; padding: 0px 5px 0px 0px;">
											<img border="0" src="/pentaho-style/images/fly-left1.png" />
										</td>
										<td colspan="2">
											View: <a href="javascript:void" onclick="map.setCenter( new GLatLng(35.55, -119.268 ), 6); return false;">West Coast</a> | 
											<a href="javascript:void" onclick="map.setCenter( new GLatLng(41.4263, -73.1799 ), 7); return false;">East Coast</a>

										</td>
									</tr>
			
                						<tr style="background-color: #e5e5e5;">
                  						<td style="background-image: url(/pentaho-style/images/fly-left1.png); background-repeat: repeat-y; height: 10px; padding: 0px 5px 0px 0px;">
								<img border="0" src="/pentaho-style/images/fly-left1.png" /></td>
										<td valign="top" style="padding: 0px 0px 0px 0px;">

											<table>
												<tr>
													<td>
														<img border="0" src="http://labs.google.com/ridefinder/images/mm_20_red.png"/>
													</td>
													<td>
														<select id="bottomthreshold" onchange="update(false)">
															<option value="0">0</option>
															<option value="10000">10000</option>
															<option value="20000">20000</option>
															<option value="30000">30000</option>
															<option value="40000">40000</option>
															<option value="50000" selected>50000</option>
															<option value="60000">60000</option>
															<option value="70000">70000</option>
															<option value="80000">80000</option>
															<option value="90000">90000</option>
															<option value="100000">100000</option>
															<option value="110000">110000</option>
															<option value="120000">120000</option>
															<option value="130000">130000</option>
															<option value="140000">140000</option>
															<option value="150000">150000</option>
															<option value="160000">160000</option>
															<option value="170000">170000</option>
															<option value="180000">180000</option>
															<option value="190000">190000</option>
															<option value="200000">200000</option>
														</select>
													</td>
													<td>
														<img border="0" src="http://labs.google.com/ridefinder/images/mm_20_yellow.png"/>
													</td>
													<td>
														
														<select id="topthreshold" onchange="update(true)">
															<option value="0">0</option>
															<option value="10000">10000</option>
															<option value="20000">20000</option>
															<option value="30000">30000</option>
															<option value="40000">40000</option>
															<option value="50000">50000</option>
															<option value="60000">60000</option>
															<option value="70000">70000</option>
															<option value="80000">80000</option>
															<option value="90000">90000</option>
															<option value="100000" selected>100000</option>
															<option value="110000">110000</option>
															<option value="120000">120000</option>
															<option value="130000">130000</option>
															<option value="140000">140000</option>
															<option value="150000">150000</option>
															<option value="160000">160000</option>
															<option value="170000">170000</option>
															<option value="180000">180000</option>
															<option value="190000">190000</option>
															<option value="200000">200000</option>
														</select> 
													</td>
													<td>
														<img border="0" src="http://labs.google.com/ridefinder/images/mm_20_green.png"/>
													</td>
												</tr>
											</table>
		
										</td>
                						</tr>
            						</table>
            					</td>
          				</tr>
          				<tr>
            					<td style="height: 25px; width: 25px;">
            						<img border="0" src="/pentaho-style/images/fly-bot-left1.png" /><br /> 
            					</td>

            					<td               style="background-image: url(/pentaho-style/images/fly-bot1.png); background-repeat: repeat-x"><img border="0" src="/pentaho-style/images/fly-bot1.png" /><br /></td>
          				</tr>
      				</table>
      			</td>
    				<td valign="top" style="padding: 0px 0px 0px 0px; font-size: .85em;">
				</td>
  			</tr>
		</table>
		<br/>
		<center>
			<img border="0" src="/pentaho-style/images/pentaho_googlemap_white.png" style="padding-top:5px"/>
		</center>

	</div>

	<!-- define the customer details panel -->
    <div id="details-div" style="position:absolute;width: 320px; xheight: 500px;top:135px; left:30px; border:0px;display:none;overflow: none;">

		<table border="0" cellpadding="0" cellspacing="0" width="100%" xheight="470">
  			<tr>
    				<td valign="top">
      				<table border="0" cellpadding="0" cellspacing="0" width="100%" xheight="470" style="margin:0px; padding:0px">
          				<tr>
            					<td colspan="2"  valign="top" style="background-color: #e5e5e5;">
            						<table width="100%" border="0" cellspacing="1" cellpadding="0" height="100%">
                						<tr style="background-color: #e5e5e5;">
                  						<td style="background-image: url(/pentaho-style/images/fly-left1.png); background-repeat: repeat-y; height: 10px; padding: 0px 5px 0px 0px;">
											&nbsp;
										</td>
										<td valign="top" style="padding: 0px 0px 0px 0px;">
											<div id="details-cell1" style="padding: 0px 0px 0px 0px;height: 250px; overflow: auto; ">
											</div>
										</td>
                						</tr>
               						<tr style="background-color: #e5e5e5;">
                  						<td style="background-image: url(/pentaho-style/images/fly-left1.png); background-repeat: repeat-y; padding: 0px 5px 0px 0px;">
											&nbsp;
										</td>
										<td valign="top" style="padding: 10px 0px 0px 0px;height: 113px; overflow: auto; font: normal 1.1em Tahoma, 'Trebuchet MS', Arial;">
											<center>
												Sales History
											</center>
											<div id="details-cell2" style="padding: 0px 0px 0px 0px;height: 113px; width: 85%">
											</div>
										</td>
                						</tr>
            						</table>
            					</td>
          				</tr>
          				<tr>
            					<td width="25" style="height: 25px; width: 25px;">
								<img border="0" src="/pentaho-style/images/fly-bot-left1.png" /><br /> 
							</td>
            					<td width="100%" style="background-image: url(/pentaho-style/images/fly-bot1.png); background-repeat: repeat-x">
            						<img border="0" src="/pentaho-style/images/fly-bot1.png" /><br />
            					</td>
          				</tr>
      				</table>
      			</td>
    				<td valign="top" style="padding: 0px 0px 0px 0px; font-size: .85em;">
				</td>
  			</tr>
		</table>
		<center>
			<img border="0" src="/pentaho-style/images/pentaho_googlemap_white.png" style="padding-top:5px"/>
		</center>
	</div>

	<!-- define the Google Map area -->
    <div id="map" style="position:absolute;width: 640px; height: 580px;top:40px;left:350px;border:1px solid #808080"></div>

<% } %>
