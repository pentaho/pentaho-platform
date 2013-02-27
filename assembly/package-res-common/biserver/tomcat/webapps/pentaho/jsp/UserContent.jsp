<%@ page language="java" 
	import="java.util.ArrayList,
			org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
	        org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.util.VersionHelper,
			org.pentaho.platform.web.refactor.UserFilesComponent,
    		org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.api.engine.IBackgroundExecution,
			org.pentaho.platform.api.repository.IContentItem,
			org.quartz.JobDetail,
			org.quartz.JobDataMap,
			org.quartz.Scheduler,
			org.quartz.SchedulerException,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder" %><%

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
 * @created September, 2006
 * @author Marc Batchelor
 * 
 */

 PentahoSystem.systemEntryPoint();
 try { 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();

	IPentahoSession userSession = PentahoSessionHolder.getSession();

	String thisUrl = baseUrl + "UserContent?"; //$NON-NLS-1$
	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );

	ArrayList messages = new ArrayList();
	UserFilesComponent userFiles = PentahoSystem.get(UserFilesComponent.class, "IUserFilesComponent", userSession );
	userFiles.setUrlFactory( urlFactory );
	userFiles.setRequest( request );
	userFiles.setResponse( response );
	userFiles.setMessages( messages );
	userFiles.validate( userSession, null );
	
	String action = request.getParameter( "action" );
	if( "delete".equals( action ) ) {
		String delId = request.getParameter( "content-id" );
		userFiles.deleteContent( delId );
	}
	else if( "cancel-job".equals( action ) ) {
		String jobName = request.getParameter( "del-job-name" );
		String jobGroup = request.getParameter( "del-job-group" );
		userFiles.cancelJob( jobName, jobGroup );
	}

	// Clear the alert when this page is viewed (unless someone passes a parameter
    // of clearAlert=false
	String clearAlert = request.getParameter( "clearAlert" );
	if( !"false".equalsIgnoreCase(clearAlert) ) {
		userSession.resetBackgroundExecutionAlert();
	}

	String intro = ""; //$NON-NLS-1$
	String footer = ""; //$NON-NLS-1$
	String content = ""; //$NON-NLS-1$
	content = userFiles.getContent( "text/html" ); //$NON-NLS-1$
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template.html", "Background Execution Status", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" ); //$NON-NLS-1$
	}
	
	%>

	<%= intro %>

	<%= content %>

	<%= footer %>

<%

	} finally {
	PentahoSystem.systemExitPoint();
 }
%>

