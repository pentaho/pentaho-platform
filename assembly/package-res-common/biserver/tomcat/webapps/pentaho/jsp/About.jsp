<%@ page language="java"
	import="org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.api.repository.ISolutionRepository,
	        org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.api.util.IVersionHelper,
    		org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.engine.services.actionsequence.ActionResource,
			org.pentaho.actionsequence.dom.IActionResource,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder"%>
<%

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
 * @created Jul 23, 2005 
 * @author James Dixon
 * 
 */
 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();

	IPentahoSession userSession = PentahoSessionHolder.getSession();

	StringBuffer sb = new StringBuffer();
  IVersionHelper versionHelper = PentahoSystem.get(IVersionHelper.class, null);
	String header = Messages.getInstance().getString( "UI.USER_ABOUT_TITLE", versionHelper.getVersionInformation(PentahoSystem.class) );

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {

		// Load a template for this web page
		String template = null;
  		try {
  		  String templateName = request.getParameter("template"); //$NON-NLS-1$
  		  if (templateName == null) {
  		    templateName = "system/custom/template-dialog.html"; //$NON-NLS-1$
  		  }
	  	  byte[] bytes = IOUtils.toByteArray(ActionSequenceResource.getInputStream(templateName, LocaleHelper.getLocale()));
	  	  template = String(bytes, LocaleHelper.getSystemEncoding());
    		} catch (Throwable t) {
    		  // TODO we need to do something here, like log at the very least!
    		  // catching Throwable is likely not optimal either.
    		}

		// Break the template into header and footer sections
		String sections[] = templater.breakTemplateString( template, header, userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}

%>

<%= intro %>

<table class='content_table' border='0' cellpadding='0' cellspacing='0'
	height='100%'>
	<tr>
		<td height='100%' class='contentcell_half_left'>
		<%
				String copyright = Messages.getInstance().getString( "UI.USER_COPYRIGHT" );
				String aboutText = Messages.getInstance().getString( "UI.USER_ABOUT_TEXT", copyright );
				%> <%= aboutText %> <a href='javascript:void(0);'
			onclick='javascript:window.open( "http://community.pentaho.org/contributors/" );'><%=Messages.getInstance().getString( "UI.USER_SPECIAL_THANKS" )%></a>
		</td>
	</tr>
</table>

<%= footer %>
