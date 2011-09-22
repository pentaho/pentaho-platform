<%@ page language="java"
import="org.pentaho.platform.web.jsp.messages.Messages"%>

<%
/*
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * 
 */
 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title><%=Messages.getInstance().getString( "UI.WEB_HELP_TITLE" )%></title>
	<style>
	  body, html{
	    padding: 0px;
	    margin: 0px;
  	  height: 100%;
	  }
	  #footer{
	    position:absolute;
	    width: 100%;
	    height: 20px;
	    background-color: #555;
	    position: fixed;
	    clear:both;
	    bottom: 0px;
	    padding: 4px 0px;
	  }
	  button{
	    float:right;
	  }
	</style>
</head>
<body>
  <div id="footer">
    <button onclick="window.close()"><%=Messages.getInstance().getString( "UI.WEB_HELP_CLOSE" )%></button>
    <button onclick="window.print()"><%=Messages.getInstance().getString( "UI.WEB_HELP_PRINT" )%></button>
  </div>
  <iframe src="<%=request.getContextPath()+"/"+request.getParameter("topic")%>" style="height:100%; width:100%; border:none"/>

</body>
</html>
