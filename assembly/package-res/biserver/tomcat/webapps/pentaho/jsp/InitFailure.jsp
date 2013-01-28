<%@ page
  session="true"
  contentType="text/html;"
  import="org.pentaho.platform.util.messages.LocaleHelper,
          org.pentaho.platform.engine.core.system.PentahoSystem,
          org.pentaho.platform.web.jsp.messages.Messages,
          java.util.List" 
%><%
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
 */
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
    response.setHeader("Pragma", "no-cache"); // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Cache-Control", "no-store, no-cache, private, must-revalidate, max-stale=0" );
    response.setHeader("Expires", "0");
  	List initializationErrorMessages = PentahoSystem.getInitializationFailureMessages();
%>
<html>
<head>
  <title>Error Initializing Pentaho</title>
</head>
<body bgcolor="white" dir="<%= LocaleHelper.getTextDirection() %>">

  <h2>Pentaho Initialization Exception</h2>
  <br />
  <div style='border:2px solid #cccccc'>
    <table width='100%' border='0'>
      <tr><td><b><%=Messages.getInstance().getString("InitFailure.USER_ERRORS_DETECTED")%></b></td></tr>
<%
  for (int i=0; i<initializationErrorMessages.size(); i++) {
%>
    <tr><td><%=initializationErrorMessages.get(i)%></td></tr>
<%
  } // end for loop
%>
    </table>
    <br />
      <%= Messages.getInstance().getString("InitFailure.USER_SEE_SERVER_CONSOLE") %>
  </div>
 </body>
</html>
