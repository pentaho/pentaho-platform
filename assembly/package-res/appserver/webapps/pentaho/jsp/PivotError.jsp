<%@ page
  session="true"
  contentType="text/html;"
  import="org.pentaho.platform.util.messages.LocaleHelper" 
%>
<%--
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
--%>
<%
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
%><html>
<head>
  <title>Error handling JPivot request ...</title>
</head>
<body bgcolor="white" dir="<%= LocaleHelper.getSystemEncoding() %>">

  <h2>JPivot Error ...</h2>

  An error happened servicing a JPivot request. Please see the server console for more details.
</body>
</html>
