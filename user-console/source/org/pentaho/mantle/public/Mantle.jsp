<%--
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
--%>

<!DOCTYPE html>
<%@page pageEncoding="UTF-8" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="org.pentaho.platform.util.messages.LocaleHelper" %>
<%@page import="java.net.URL" %>
<%@page import="java.net.URLClassLoader" %>
<%@page import="java.util.Locale" %>
<%@page import="java.util.List" %>
<%@page import="java.util.ResourceBundle" %>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSystem" %>
<%@page import="org.pentaho.platform.api.engine.IPluginManager" %>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder" %>

<%
  boolean hasDataAccessPlugin = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ).getRegisteredPlugins().contains( "data-access" );
%>

<%
  Locale effectiveLocale = request.getLocale();
  if ( !StringUtils.isEmpty( request.getParameter( "locale" ) ) ) {
    request.getSession().setAttribute( "locale_override", request.getParameter( "locale" ) );
    LocaleHelper.parseAndSetLocaleOverride( request.getParameter( "locale" ) );
  } else {
    request.getSession().setAttribute( "locale_override", null );
    LocaleHelper.setLocaleOverride( null );
  }

  URLClassLoader loader = new URLClassLoader( new URL[] { application.getResource( "/mantle/messages/" ) } );
  ResourceBundle properties = ResourceBundle.getBundle( "mantleMessages", request.getLocale(), loader );

%>

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <title><%= properties.getString("productName") %></title>

  <%
    boolean haveMobileRedirect = false;
    String ua = request.getHeader("User-Agent").toLowerCase();
    if (!"desktop".equalsIgnoreCase(request.getParameter("mode"))) {
      if (ua.contains("ipad") || ua.contains("ipod") || ua.contains("iphone") || ua.contains("android") || "mobile".equalsIgnoreCase(request.getParameter("mode"))) {
        IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
        List<String> pluginIds = pluginManager.getRegisteredPlugins();
        for (String id : pluginIds) {
          String mobileRedirect = (String)pluginManager.getPluginSetting(id, "mobile-redirect", null);
          if (mobileRedirect != null) {
            // we have a mobile redirect
            haveMobileRedirect = true;
  %>
  <script type="text/javascript">
    //Get URL parameters
    var getParams = document.URL.split("?");
    var params = '';

    //If there are no GET parameters on the URL leave the params object empty so that the check for
    //a startup report setting is conducted
    if (getParams.length > 1) {
      params = '?' + getParams[1];
    }
    if(typeof window.top.PentahoMobile != "undefined"){
      window.top.location.reload();
    } else {
      document.write('<META HTTP-EQUIV="refresh" CONTENT="0;URL=<%=mobileRedirect%>' + params + '">');
    }
  </script>
  <%
          break;
          }
        }
      }
    if (!haveMobileRedirect) {
  %>
  <meta name="gwt:property" content="locale=<%=ESAPI.encoder().encodeForHTMLAttribute(effectiveLocale.toString())%>">
  <link rel="shortcut icon" href="/pentaho-style/favicon.ico"/>
  <link rel='stylesheet' href='mantle/MantleStyle.css'/>
  <%if ( hasDataAccessPlugin ) {%>
  <link rel="stylesheet" href="content/data-access/resources/gwt/datasourceEditorDialog.css"/>
  <%}%>
  <link rel="stylesheet" href="mantle/Widgets.css"/>

  <!-- ANGULAR INCLUDES -->
  <link rel='stylesheet' href='content/common-ui/resources/themes/css/angular-animations.css'/>
  <script language="javascript" type="text/javascript" src="webcontext.js?context=mantle"></script>

  <script type="text/javascript" src="mantle/nativeScripts.js"></script>
  <script type="text/javascript">
    try{
    if (window.opener && window.opener.reportWindowOpened != undefined) {
      window.opener.reportWindowOpened();
    }
    } catch(/* XSS */ ignored){}

    var dataAccessAvailable = false; //Used by child iframes to tell if data access is available.
    /* this function is called by the gwt code when initing, if the user has permission */
    function initDataAccess(hasAccess) {
      dataAccessAvailable = hasAccess;
      if (!hasAccess) {
        return;
      }
      if (typeof(addMenuItem) == "undefined") {
        setTimeout("initDataAccess(" + hasAccess + ")", 1000);
        return;
      } else {
        addMenuItem("manageDatasourcesEllipsis", "manage_content_menu", "ManageDatasourcesCommand");
        addMenuItem("newDatasource", "new_menu", "AddDatasourceCommand");
      }
    }

    var datasourceEditorCallback = {
      onFinish: function (val, transport) {
      },
      onError: function (val) {
        alert('error:' + val);
      },
      onCancel: function () {
      },
      onReady: function () {
      }
    }

    // This allows content panels to have PUC create new datasources. The iframe requesting
    // the new datasource must have a function "openDatasourceEditorCallback" on it's window scope
    // to be notified of the successful creation of the datasource.
    function openDatasourceEditorIFrameProxy(windowReference) {
      var callbackHelper = function (bool, transport) {
        windowReference.openDatasourceEditorCallback(bool, transport);
      }
      pho.openDatasourceEditor(new function () {
        this.onError = function (err) {
          alert(err);
        }
        this.onCancel = function () {
        }
        this.onReady = function () {
        }
        this.onFinish = function (bool, transport) {
          callbackHelper(bool, transport);
        }
      });
    }

    // Require Angular Plugin Initialization
    require(['mantle/puc-api/pucAngularApi']);
  </script>

</head>

<body oncontextmenu="return false;" class="pentaho-page-background">

  <div ng-show="viewContainer === 'PUC'" 
    class="ng-app-element deny-animation-change" animate="fade" 
    id="pucWrapper" cellspacing="0" cellpadding="0" style="width: 100%; height: 100%;">
    
    <div id="pucHeader" cellspacing="0" cellpadding="0">
      <div id="pucMenuBar"></div>
      <div id="pucPerspectives"></div>
      <div id="pucToolBar"></div>
      <div id="pucUserDropDown"></div>
    </div>

    <div id="pucContent"></div>
  </div>

  <div ng-view ng-show="viewContainer === 'ngView'" class="ng-app-view ng-app-element"></div>
  

<script type="text/javascript">
  document.getElementById("pucWrapper").style.position = "absolute";
  document.getElementById("pucWrapper").style.left = "-5000px";
  require(["common-ui/util/BusyIndicator"], function (busy) {

    busy.show("<%= properties.getString("pleaseWait") %>", "<%= properties.getString("loadingConsole") %>", "pucPleaseWait");

    window.notifyOfLoad = function (area) {
      var allFramesLoaded = true;
      for (var i = 0; i < window.frames.length; i++) {
        try{
          if (window.frames[i].document.readyState != "complete") {
            allFramesLoaded = false;
            break;
          }
        } catch(ignored){
          // likely a XSS issue.
        }
      }

      if (allFramesLoaded) {
        busy.hide("pucPleaseWait");
        document.getElementById("pucWrapper").style.left = "0";
        document.getElementById("pucWrapper").style.position = "relative";
        window.allFramesLoaded = true;
      } else {
        // check again in a bit
        setTimeout("notifyOfLoad()", 300);
      }
    }


    // Remove when notifyOfLoad is called from PUC
    setTimeout(function () {
      notifyOfLoad();
    }, 4000);
  });

</script>

<!-- OPTIONAL: include this if you want history support -->
<iframe id="__gwt_historyFrame" style="width:0px;height:0px;border:0;display:none"></iframe>

</body>

<script language='javascript' src='mantle/mantle.nocache.js'></script>
<%if ( hasDataAccessPlugin ) {%>
<script language='javascript' src='content/data-access/resources/gwt/DatasourceEditor.nocache.js'></script>
<%}}}%>

</html>