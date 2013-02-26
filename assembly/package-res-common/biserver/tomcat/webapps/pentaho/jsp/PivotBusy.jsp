<%@ page
  session="true"
  contentType="text/html;" 
  import="org.pentaho.platform.util.messages.LocaleHelper" %><%
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
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
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" --%><html>
<head>
  <title>JPivot is busy ...</title>
  <meta http-equiv="refresh" content="1; URL=<c:out value="${requestSynchronizer.resultURI}"/>">
</head>
<body bgcolor="white" dir="<%= LocaleHelper.getSystemEncoding() %>">

  <h2>JPivot is busy ...</h2>

  Please wait until your results are computed. Click
  <a href="<c:out value="${requestSynchronizer.resultURI}"/>">here</a>
  if your browser does not support redirects.

</body>
</html>
