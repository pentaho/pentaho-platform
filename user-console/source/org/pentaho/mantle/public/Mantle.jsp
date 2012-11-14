<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Locale"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.pentaho.platform.util.messages.LocaleHelper"%>
<%@page import="java.net.URLClassLoader"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.io.File"%>
<%@page import="org.owasp.esapi.ESAPI"%>

<%
  Locale effectiveLocale = request.getLocale();
  if (!StringUtils.isEmpty(request.getParameter("locale"))) {
    effectiveLocale = new Locale(request.getParameter("locale"));
    request.getSession().setAttribute("locale_override", request.getParameter("locale"));
    LocaleHelper.setLocaleOverride(effectiveLocale);
  } else {
    request.getSession().setAttribute("locale_override", null);
    LocaleHelper.setLocaleOverride(null);
  }
  
  URLClassLoader loader = new URLClassLoader(new URL[] {application.getResource("/mantle/messages/")});
  ResourceBundle properties = ResourceBundle.getBundle("mantleMessages", request.getLocale(), loader);

%>

<html>
	<head>
		<title>Pentaho User Console</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="gwt:property" content="locale=<%=ESAPI.encoder().encodeForHTMLAttribute(effectiveLocale.toString())%>">
	<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />
	<link rel='stylesheet' href='mantle/MantleStyle.css'/>
    <link rel="stylesheet" href="content/data-access/resources/gwt/datasourceEditorDialog.css"/>
    <link rel="stylesheet" href="mantle/Widgets.css" />
	
    <script language="javascript" type="text/javascript" src="webcontext.js?context=mantle"></script>
        
    <script type="text/javascript" src="mantle/nativeScripts.js"></script>
    <script type="text/javascript">
      if(window.opener && window.opener.reportWindowOpened != undefined){
        window.opener.reportWindowOpened();
      }

  	var dataAccessAvailable = false; //Used by child iframes to tell if data access is available.
    /* this function is called by the gwt code when initing, if the user has permission */
    function initDataAccess(hasAccess) {
      dataAccessAvailable = hasAccess;
      if(!hasAccess){
        return;
      }
      if(typeof(addMenuItem) == "undefined"){
        setTimeout("initDataAccess("+hasAccess+")", 1000);
        return;
      } else {
        addMenuItem("manageDatasourcesEllipsis","manage_content_menu", "ManageDatasourcesCommand");
        addMenuItem("newDatasource","new_menu", "AddDatasourceCommand");
      }
    }

    var datasourceEditorCallback = {
      onFinish : function(val, transport) {
      },
      onError : function(val) {
        alert('error:' + val);
      },
      onCancel : function() {
      },
      onReady : function() {
      }
    }

    // This allows content panels to have PUC create new datasources. The iframe requesting
    // the new datasource must have a function "openDatasourceEditorCallback" on it's window scope
    // to be notified of the successful creation of the datasource.
    function openDatasourceEditorIFrameProxy(windowReference){
    	var callbackHelper = function(bool, transport){
    		windowReference.openDatasourceEditorCallback(bool, transport);
    	}
    	pho.openDatasourceEditor(new function(){
        this.onError = function(err){
          alert(err);
        }
        this.onCancel = function(){
        }
        this.onReady = function(){
        }
        this.onFinish = function(bool, transport){
          callbackHelper(bool, transport);
        }
      });
    }

    </script>
	</head>

	<body oncontextmenu="return false;" class="pentaho-page-background">

	<!--
	<div id="loading">
    		<div class="loading-indicator">
    			<img src="mantle/large-loading.gif" width="32" height="32"/><%= properties.getString("loadingConsole") %><a href="http://www.pentaho.com"></a><br/>
    			<span id="loading-msg"><%= properties.getString("pleaseWait") %></span>
    		</div>
	</div>
	-->
	
	<!-- Standard -->
	<table cellspacing="0" cellpadding="0" style="width: 100%; height: 100%">
		<tr>
			<td>
				<table cellspacing="0" cellpadding="0" >
					<tr>
						<td id="pucMenuBar" style="width:100%"></td>
						<td id="pucPerspectives"></td>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
			<td id="pucToolBar"></td>
		</tr>
		
		<tr>
			<td id="pucContent" style="width:100%;height:100%"></td>
		</tr>
	</table>

	<!-- Toolbar On Top -->
	<!-- 
	<table cellspacing="0" cellpadding="0" style="width: 100%; height: 100%">
		<tr>
			<td id="pucToolBar"></td>
		</tr>
		
		<tr>
			<td>
				<table cellspacing="0" cellpadding="0" >
					<tr>
						<td id="pucMenuBar" style="width:100%"></td>
						<td id="pucPerspectives"></td>
					</tr>
				</table>
			</td>
		</tr>
		
		<tr>
			<td id="pucContent" style="width:100%;height:100%"></td>
		</tr>
	</table>
	-->
	
	
	<!-- LOGO -->
	<!--
	<table cellspacing="0" cellpadding="0" style="width: 100%; height: 100%">
		<tr>
			<td colspan="2">
				<table cellspacing="0" cellpadding="0" style="width: 100%">
					<tr>
						<td id="pucMenuBar" style="width:100%"></td>
						<td id="pucPerspectives"></td>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
			<td id="pucToolBar"></td>
			<td style="background-color: white;">LOGO PANEL</td>
		</tr>
		
		<tr>
			<td id="pucContent" colspan="2" style="width:100%;height:100%"></td>
		</tr>
	</table>
	-->

	
	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width:0px;height:0px;border:0;display:none"></iframe>

	</body>

	<script language='javascript' src='mantle/mantle.nocache.js'></script>
	<script language='javascript' src='content/data-access/resources/gwt/DatasourceEditor.nocache.js'></script>
	
</html>
