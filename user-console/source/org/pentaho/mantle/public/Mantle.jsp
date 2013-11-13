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
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="org.pentaho.platform.util.messages.LocaleHelper" %>
<%@page import="java.net.URL" %>
<%@page import="java.net.URLClassLoader" %>
<%@page import="java.util.Locale" %>
<%@page import="java.util.ResourceBundle" %>

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
  <title>Pentaho User Console</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="gwt:property" content="locale=<%=ESAPI.encoder().encodeForHTMLAttribute(effectiveLocale.toString())%>">
  <link rel="shortcut icon" href="/pentaho-style/favicon.ico"/>
  <link rel='stylesheet' href='mantle/MantleStyle.css'/>
  <link rel="stylesheet" href="content/data-access/resources/gwt/datasourceEditorDialog.css"/>
  <link rel="stylesheet" href="mantle/Widgets.css"/>

  <!-- ANGULAR INCLUDES -->
  <link rel='stylesheet' href='content/common-ui/resources/themes/css/angular-animations.css'/>  

  <script language="javascript" type="text/javascript" src="webcontext.js?context=mantle"></script>

  <script type="text/javascript" src="mantle/nativeScripts.js"></script>
  <script type="text/javascript">
    if (window.opener && window.opener.reportWindowOpened != undefined) {
      window.opener.reportWindowOpened();
    }

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

  <div ng-view ng-show="viewContainer === 'ngView' "
    class="ng-app-view ng-app-element" animate="slide-left"></div>
  

<script type="text/javascript">
  document.getElementById("pucWrapper").style.position = "absolute";
  document.getElementById("pucWrapper").style.left = "-5000px";
  pen.require(["common-ui/util/BusyIndicator"], function (busy) {

    busy.show("<%= properties.getString("pleaseWait") %>", "<%= properties.getString("loadingConsole") %>", "pucPleaseWait");

    window.notifyOfLoad = function (area) {
      var allFramesLoaded = true;
      for (var i = 0; i < window.frames.length; i++) {
        if (window.frames[i].document.readyState != "complete") {
          allFramesLoaded = false;
          break;
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
<script language='javascript' src='content/data-access/resources/gwt/DatasourceEditor.nocache.js'></script>

</html>