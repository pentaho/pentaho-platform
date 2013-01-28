<%@ page language="java" 
  import="java.util.ArrayList,
    java.util.Locale,
    org.pentaho.platform.util.web.SimpleUrlFactory,
    org.pentaho.platform.engine.core.system.PentahoSystem,
    org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
    org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
    org.pentaho.platform.api.engine.IPentahoSession,
    org.pentaho.platform.web.jsp.messages.Messages,
    org.pentaho.platform.web.http.WebTemplateHelper,
    org.pentaho.platform.api.engine.IUITemplater,
	org.pentaho.platform.util.VersionHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.api.ui.INavigationComponent,
	org.pentaho.platform.api.repository.ISolutionRepository,
  org.pentaho.platform.engine.core.system.PentahoSessionHolder,
  org.pentaho.platform.api.engine.IMessageFormatter"
	 %><%/*
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
*/

	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
	HttpSession httpSession = request.getSession();

	boolean doSplash = httpSession.getAttribute( "dopentahosplash" ) == null || "true".equals( request.getParameter("splash") );
	doSplash = false;
	httpSession.setAttribute( "dopentahosplash", "false" );
	 
	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

	IPentahoSession userSession = PentahoSessionHolder.getSession();
		
	HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );
	String hrefUrl = baseUrl; //$NON-NLS-1$
	String onClick = ""; //$NON-NLS-1$
	String thisUrl = baseUrl + "Navigate?"; //$NON-NLS-1$

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();

	String solution = request.getParameter( "solution" ); //$NON-NLS-1$
	if( "".equals( solution ) ) { //$NON-NLS-1$
		solution = null;
	}
	boolean allowBackNavigation = solution != null;

	INavigationComponent navigate = PentahoSystem.get(INavigationComponent.class, userSession);
	navigate.setHrefUrl(hrefUrl);
	navigate.setOnClick(onClick);
	navigate.setSolutionParamName("solution");
	navigate.setPathParamName("path");
	navigate.setAllowNavigation(new Boolean(allowBackNavigation));
	navigate.setOptions("");
	navigate.setUrlFactory(urlFactory);
	navigate.setMessages(messages);
	// This line will override the default setting of the navigate component
	// to allow debugging of the generated HTML.
	// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
	navigate.validate( userSession, null );
	navigate.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
	navigate.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
	
	String xsl = null;
	String view = request.getParameter("view");//$NON-NLS-1$
	if( view != null ) {
		if( "default".equals( view ) ) { //$NON-NLS-1$
			userSession.removeAttribute( "pentaho-ui-folder-style" ); //$NON-NLS-1$
		} else {
			userSession.setAttribute( "pentaho-ui-folder-style", view );
			navigate.setXsl( "text/html", view ); //$NON-NLS-1$
		}
	} else {
		view = (String) userSession.getAttribute( "pentaho-ui-folder-style" );
		if( view != null ) {
			navigate.setXsl( "text/html", view ); //$NON-NLS-1$
		}
	}
	
	String content = navigate.getContent( "text/html" ); //$NON-NLS-1$
	if( content == null ) {
		StringBuffer buffer = new StringBuffer();
		PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", Messages.getInstance().getErrorString( "NAVIGATE.ERROR_0001_NAVIGATE_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
		content = buffer.toString();
	}

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}%><%= intro %>
<%= content %>
<%= footer %>

<% if( doSplash ) { %>

<div id="splash" style="width:100%;height:340px;position:absolute;top:150px;z-index:10;display:block">
  <center>
  <div style="width:480px;height:320px;background-color:white;background-image: url(/pentaho-style/splash.png);border:1px solid #FF6113">
    <img src="/pentaho-style/active/logo.png" border="0"/>
    <p/>
    <table width="470">
      <tr>
        <td>
    
          <span style="font-weight:bold">Featuring:</span>
	 	  <p/>Business Intelligence Platform - by Pentaho
		  <br/>HTML, PDF, XLS reporting - by JFreeReport
		  <br/>Dashboards - By Pentaho
		  <br/>OLAP Database Server - by Mondrian
		  <br/>Slice and Dice Analysis - by JPivot
		  <br/>Workflow Engine - By Enhydra Shark
		  <br/>Scheduling - By Quartz
		</td>
	  </tr>
	</table>
  </div>
  </center>
</div>

	<script type="text/javascript">
		setTimeout( "hideSplash()", 4500 );
		function hideSplash() {
			document.getElementById("splash").style.display="none";
		}
	</script>
<% } %>

