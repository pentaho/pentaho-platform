<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@
    page
  language="java"
  import="java.io.InputStream,
      java.util.Map,
      java.util.HashMap,
      java.util.List,
      java.util.ArrayList,
      java.util.Locale,
      java.util.PropertyResourceBundle,
      java.util.ResourceBundle,
      java.util.regex.Pattern,
      java.util.regex.Matcher,
      org.pentaho.platform.util.messages.LocaleHelper,
      org.pentaho.platform.api.engine.IPentahoSession,
      org.pentaho.platform.api.engine.IPluginManager,
      org.pentaho.platform.api.repository.ISolutionRepository,
      org.pentaho.platform.engine.core.system.PentahoSystem,
      org.pentaho.platform.engine.core.system.StandaloneSession,
      org.pentaho.platform.util.logging.Logger,
      org.pentaho.platform.web.jsp.messages.Messages,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder,
      org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource,
      org.apache.commons.lang.StringEscapeUtils"%>
<%
  /*
   * Copyright 2006 Pentaho Corporation.  All rights reserved.
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
   * @created Jul 23, 2005
   * @author James Dixon
   *
   */
%>
<%@page import="org.pentaho.ui.xul.XulOverlay"%>
<%@page import="org.pentaho.platform.api.engine.IPluginManager"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Messages.getInstance().getString("UI.PUC.LAUNCH.TITLE")%></title>
<style type="text/css">

body{
	text-align: center;
}

#wrapper {
	margin-right: auto;
	margin-left: auto;
	margin-top: 10%;
	width: 684px;
	text-align: center;
}

</style>


<script language="javascript" type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>
<script language="javascript" type="text/javascript" src="../../js/pentaho-ajax.js"></script>
<script type="text/javascript">
  
<%!
  private static ResourceBundle getBundle(String messageUri) {
    Locale locale = LocaleHelper.getLocale();
    IPentahoSession session = new StandaloneSession( "dashboards messages" ); //$NON-NLS-1$
    try {
        if (messageUri.startsWith("content/")) {
          messageUri = "system/" + messageUri.substring(8); //$NON-NLS-1$
        }
       InputStream in = ActionSequenceResource.getInputStream(messageUri, locale);
      return new PropertyResourceBundle( in );
    } catch (Exception e) {
      Logger.error( Messages.class.getName(), "Could not get localization bundle", e ); //$NON-NLS-1$
    }
    return null;
  }
%>

  var actionToCmdMap = [];
  actionToCmdMap['launch_WAQR'] = 'openWAQR()';
  actionToCmdMap['launch_new_datasource'] = 'newDatasource()';
  actionToCmdMap['launch_manage_datasources'] = 'manageDatasources()'
  
<% 
  boolean isCE = true;
  boolean hasAnalyzer = false;
  boolean hasIteractiveReporting = false;
  boolean hasDashboards = false;
  IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); 
  if (pluginManager != null) {
    for(XulOverlay overlayObj : pluginManager.getOverlays()) {
      if (overlayObj.getId() != null && overlayObj.getId().equals("launch")) { //$NON-NLS-1$
        ResourceBundle bundle = getBundle(overlayObj.getResourceBundleUri());
        // replace I18N parameters
        Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}"); //$NON-NLS-1$
        Matcher m = p.matcher(overlayObj.getOverlayXml());
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
          String param = m.group(1);
          m.appendReplacement(sb, bundle.getString(param));
        }
        m.appendTail(sb);
        String overlay = sb.toString();
        
        String actionName = null;
        int id = overlay.indexOf("id=\""); //$NON-NLS-1$
        if (id >= 0) {
          actionName = overlay.substring(id + 4, overlay.indexOf("\"", id + 4)); //$NON-NLS-1$
        }
        if (actionName != null) {	  
          int startCommand = overlay.indexOf("command=\""); //$NON-NLS-1$
          int endCommand = overlay.indexOf("\"", startCommand + 9); //$NON-NLS-1$
          String actionCommand = overlay.substring(startCommand + 9, endCommand);
		  %>
		  actionToCmdMap['<%=actionName%>'] = "<%= actionCommand%>";
		  <%
        }
      }
    }
  	hasAnalyzer = pluginManager.getRegisteredPlugins().contains("analyzer");
  	hasIteractiveReporting = pluginManager.getRegisteredPlugins().contains("pentaho-interactive-reporting");
  	hasDashboards = pluginManager.getRegisteredPlugins().contains("dashboards");

  	isCE = !hasAnalyzer && !hasIteractiveReporting && !hasDashboards;
  }
%>

function MM_callJS(jsStr) { //v2.0
  return eval(jsStr)
}

function launch_new_WAQR() {
  launch('launch_WAQR', function() {warning('You do not have Data Source access.')})
}

function launch_newDatasource() {
  launch('launch_new_datasource', function() {warning('You do not have Data Source access.')})
}

function launch_managesDatasources() {
  launch('launch_manage_datasources', function() {warning('You do not have Data Source access.')})
}

function launch_newDashboard() {
    launch('launch_new_dashboard', function() {warning('Dashboards Plug-in missing, corrupted or license not found.')})
}

function launch(action, defaultAction) {
  // if we have a plugin to handle this use it
  if (actionToCmdMap[action]) {
    eval("window.top." + actionToCmdMap[action]);
  } else {
    defaultAction();
  }  
}

function warning(message) {
  window.top.mantle_showMessage("Error", message);
}

function checkDA(){ 
	jQuery.ajax({
		type: "GET",
		cache: false,	
		dataType: 'text',
		url: CONTEXT_PATH + 'content/ws-run/metadataServiceDA/getDatasourcePermissions',
		error:function (xhr, ajaxOptions, thrownError){
    	},            
		success:function(data, textStatus, jqXHR){
			if(data.indexOf("EDIT") > -1) {
				document.getElementById('datasourcePanel').style.display = 'block';
			}
		}
	}); 
}

</script>

</head>
<body style="height:auto; background:transparent;" onload="checkDA();customizeThemeStyling();">
<div id="wrapper">
  <div class="pentaho-launcher-panel-shadowed pentaho-launcher-shine" id="outterWrapper">
    <% 
    if (isCE) {
    %>
    <table id="proMenuTable" width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
       	<td align="center" width="226" valign="bottom" class="largeGraphicButton"><img src="images/clr.gif" width="226" height="10"><br><a href="#" onClick="launch_new_WAQR()"><img src="images/new_report.png" border="0"></a></td>
       	<td valign="bottom" width="3" class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="11"></td>
       	<td align="center" width="226" valign="bottom" class="largeGraphicButton"><img src="images/clr.gif" width="226" height="10"><br><a href="#" onClick="launch('launch_new_analysis', window.top.openAnalysis)"><img src="images/new_analysis.png" border="0"></a></td>
      </tr>
      <tr>
        <td align="center" class="smallButton"><button class="pentaho-button" id="button0" onClick="launch('launch_new_report', window.top.openWAQR)">New Report</button></td>
        <td class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="4"></td>
        <td align="center" class="smallButton"><button class="pentaho-button" id="button0" onClick="launch('launch_new_analysis', window.top.openAnalysis)">New Analysis</button></td>
      </tr>
	</table>
    <%
    } else {
    %>
    <table id="proMenuTable" width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
	<% if (hasIteractiveReporting) { %>
       	<td align="center" width="226" valign="bottom" class="largeGraphicButton"><img src="images/clr.gif" width="226" height="10"><br><a href="#" onClick="launch('launch_new_report', window.top.openWAQR)"><img src="images/new_report.png" border="0"></a></td>
        <% } %>
        <% if (hasAnalyzer) { %>
		<% if (hasIteractiveReporting) { %>
        	<td valign="bottom" width="3" class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="11"></td>
		<% } %>
       	<td align="center" width="226" valign="bottom" class="largeGraphicButton"><img src="images/clr.gif" width="226" height="10"><br><a href="#" onClick="launch('launch_new_analysis', window.top.openAnalysis)"><img src="images/new_analysis.png" border="0"></a></td>
        <% } %>
	<% if (hasDashboards) { %>
		<% if (hasIteractiveReporting || hasAnalyzer) { %>
		       	<td valign="bottom" width="3" class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="11"></td>
		<% } %>
        <td align="center" width="226" valign="bottom" class="largeGraphicButton"><img src="images/clr.gif" width="226" height="10"><br><a href="#" onClick="launch_newDashboard()"><img src="images/new_dash.png" border="0"></a></td>
	<% } %>
      </tr>
      <tr>
        <% if (hasIteractiveReporting) { %>
        <td align="center" class="smallButton"><button class="pentaho-button" onClick="launch('launch_new_report', window.top.openWAQR)">New Report</button></td>
        <% } %>
        <% if (hasAnalyzer) { %>
		<% if (hasIteractiveReporting) { %>
		        <td class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="4"></td>
		<% } %>
        <td align="center" class="smallButton"><button class="pentaho-button" onClick="launch('launch_new_analysis', window.top.openAnalysis)">New Analysis</button></td>
        <% } %>
	<% if (hasDashboards) { %>
                <% if (hasIteractiveReporting || hasAnalyzer) { %>
        		<td class="largeGraphicSpacer"><img src="images/clr.gif" width="3" height="4"></td>
		<% } %>
	        <td align="center" class="smallButton"><button class="pentaho-button" onClick="launch_newDashboard()">New Dashboard</button></td>
	<% } %>
      </tr>
	 </table><%
     }
	 %>
	 <table id="datasourcePanel" style="display:none" width="684" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td colspan="3"><img src="../themes/onyx/images/seperator_horz.png" width="684" height="3"></td>
      </tr>
      <tr>
        <td class="newDsPanel" width="227px"><img src="images/new_ds.png"></td>
        <td valign="top" class="launcher-bottom-text">Data Sources:<br>Create data sources from a csv or database and define metadata to simplify content creation.</td>
        <td width="227" valign="top">
          <table border="0" cellspacing="0" cellpadding="0">
            <tr>      
             <td class="bottomButtonWrapper" align="center"><button class="pentaho-button" id="button0" style="width: 116px" onClick="launch_newDatasource()">Create New</button></td>
            </tr>
            <tr>
              <td class="bottomButtonWrapper" align="center"><button class="pentaho-button" id="button0" style="width: 116px" onClick="launch_managesDatasources()">Manage Existing</button></td> 
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </div>
</div>
</body>
</html>
