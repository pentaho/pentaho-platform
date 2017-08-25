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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
--%>

<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@
    page language="java"
    import="org.springframework.security.web.savedrequest.SavedRequest,
            org.pentaho.platform.engine.core.system.PentahoSystem,
            org.pentaho.platform.util.messages.LocaleHelper,
            org.pentaho.platform.api.engine.IPentahoSession,
            org.pentaho.platform.api.engine.IPluginManager,
            org.pentaho.platform.web.jsp.messages.Messages,
            java.util.ArrayList,
            java.util.Iterator,
            java.util.LinkedHashMap,
            java.util.List,
            java.util.Map,
            java.util.StringTokenizer,
            org.pentaho.platform.engine.core.system.PentahoSessionHolder,
            org.owasp.encoder.Encode"%>
<%!
  // List of request URL strings to look for to send 401

  private List<String> send401RequestList;

  public final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";

  public void jspInit() {
    // super.jspInit();
    send401RequestList = new ArrayList<String>();
    String unauthList = getServletConfig().getInitParameter("send401List"); //$NON-NLS-1$
    if (unauthList == null) {
      send401RequestList.add("AdhocWebService"); //$NON-NLS-1$
    } else {
      StringTokenizer st = new StringTokenizer(unauthList, ","); //$NON-NLS-1$
      String requestStr;
      while (st.hasMoreElements()) {
        requestStr = st.nextToken();
        send401RequestList.add(requestStr.trim());
      }
    }
  }
%>
<%
  response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
  String path = request.getContextPath();

  IPentahoSession userSession = PentahoSessionHolder.getSession();
  // SPRING_SECURITY_SAVED_REQUEST_KEY contains the URL the user originally wanted before being redirected to the login page
  // if the requested url is in the list of URLs specified in the web.xml's init-param send401List,
  // then return a 401 status now and don't show a login page (401 means not authenticated)
  Object reqObj = request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
  String requestedURL = "";
  if (reqObj != null) {
    requestedURL = ((SavedRequest) reqObj).getRedirectUrl();

    String lookFor;
    for (int i=0; i<send401RequestList.size(); i++) {
      lookFor = send401RequestList.get(i);
      if ( requestedURL.indexOf(lookFor) >=0 ) {
        response.sendError(401);
        return;
      }
    }
  }


  boolean loggedIn = request.getRemoteUser() != null && request.getRemoteUser() != "";
  int year = (new java.util.Date()).getYear() + 1900;

  boolean showUsers = Boolean.parseBoolean(PentahoSystem.getSystemSetting("login-show-sample-users-hint", "true"));
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" class="bootstrap">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title><%=Messages.getInstance().getString("UI.PUC.TITLE")%></title>

  <%
    String ua = request.getHeader( "User-Agent" );
    if ( ua != null ) {
      ua = ua.toLowerCase();
    } else {
      ua = "none";
    }
    if ( !"desktop".equalsIgnoreCase( request.getParameter( "mode") ) ) {
      if ( ua.contains( "ipad" ) || ua.contains( "ipod" ) || ua.contains( "iphone" )
           || ua.contains( "android" ) || "mobile".equalsIgnoreCase( request.getParameter( "mode" ) ) ) {
        IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
        List<String> pluginIds = pluginManager.getRegisteredPlugins();
        for ( String id : pluginIds ) {
          String mobileRedirect = (String)pluginManager.getPluginSetting( id, "mobile-redirect", null );
          if ( mobileRedirect != null ) {
            // we have a mobile redirect            
            String queryString = request.getQueryString();
            if( queryString != null ) {
              final Map<String, String> queryPairs = new LinkedHashMap<String, String>();
              //Check for deep linking by fetching the name and startup-url values from URL query parameters
              String[] pairs = queryString.split( "&" );
              for ( String pair : pairs ) {
                int delimiter = pair.indexOf( "=" );
                queryPairs.put( Encode.forJavaScript( pair.substring( 0, delimiter ) ),  Encode.forJavaScript( pair.substring( delimiter + 1 ) ) );
              }
              if ( queryPairs.size() > 0 ) {
                mobileRedirect += "?";
                Iterator it = queryPairs.entrySet().iterator();
                while ( it.hasNext() ) {
                  Map.Entry entry = (Map.Entry) it.next();
                  mobileRedirect += entry.getKey() + "=" + entry.getValue();
                  it.remove();
                    if ( it.hasNext() ){
                      mobileRedirect += "&";
                    }
                }
              }
            }
  %>
  <script type="text/javascript">
    if(typeof window.top.PentahoMobile != "undefined"){
      window.top.location.reload();
    } else {
      var tag = document.createElement('META');
      tag.setAttribute('HTTP-EQUIV', 'refresh');
      tag.setAttribute('CONTENT', '0;URL=<%=mobileRedirect%>');
      document.getElementsByTagName('HEAD')[0].appendChild(tag);
    }
  </script>
</head>
<BODY>
<!-- this div is here for authentication detection (used by mobile, PIR, etc) -->
<div style="display:none">j_spring_security_check</div>
</BODY>
</HTML>
<%
          return;
        }
      }
    }
  }
%>

<meta name="gwt:property" content="locale=<%=Encode.forHtmlAttribute(request.getLocale().toString())%>">
<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />

<style type="text/css">
  #login-background,
  #loginError.pentaho-dialog,
  #systemDown.pentaho-dialog,
  #login-footer {
    display: none;
  }
</style>

<script language="javascript" type="text/javascript" src="webcontext.js"></script>
<script type="text/javascript">
  var targetUrl = window.location.pathname.replace(new RegExp("(/){2,}"), "/");
  if (history && history.pushState){
    history.pushState(null, null, targetUrl);
  }
</script>

</head>

<body class="pentaho-page-background">
<div id="login-wrapper">
  <div id="login-background">
    <div id="login-logo"></div>
	
	<script type="text/javascript">
      if (window.active_theme === 'ruby') {
		document.write('<div id="login-title"><%=Messages.getInstance().getString("UI.PUC.LOGIN.HEADER")%></div>');
	  }	  
	</script>

<% 
		String cleanedLang = Encode.forHtmlAttribute(request.getLocale().toString());
		if ( cleanedLang != null ) {
			if ( cleanedLang.indexOf("_") > 0 ){
				cleanedLang = cleanedLang.substring( 0, cleanedLang.indexOf("_") );
			}
		}
%>
    <div id="login-form-container" class="lang_<%=cleanedLang%>">
      <div id="animate-wrapper">
        <h1><%=Messages.getInstance().getString("UI.PUC.LOGIN.TITLE")%></h1>
        <form name="login" id="login" action="j_spring_security_check" method="POST" onkeyup="if(window.event && window.event.keyCode && window.event.keyCode==13){var buttonToClick = document.getElementById('loginbtn'); if(buttonToClick){ buttonToClick.click();}}">
          <div class="row-fluid nowrap">
            <div class="input-container">
              <label><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></label>
              <input id="j_username" name="j_username" type="text" placeholder="" autocomplete="off">
            </div>
            <div class="input-container">
              <label><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></label>
              <input id="j_password" name="j_password" type="password" placeholder="" autocomplete="off">
            </div>
            <div class="input-container">
              <label>&nbsp;</label>
              <button type="submit" id="loginbtn" class="btn"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
              <input type="hidden" name="locale" value="en_US">
            </div>
          </div>
          <div id="eval-users-toggle-container">
            <%
              if (showUsers) {
            %>
            <div id="eval-users-toggle" onClick="toggleEvalPanel()">
              <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.EVAL_LOGIN")%></div>
                <div id="eval-arrow" class="closed"></div>
            </div>

            <%
            } else {
            %>
            &nbsp;
            <%
              }
            %>
          </div>
        </form>
      </div>

      <div class="row-fluid">
        <div id="evaluationPanel" class="span10 row-fluid">
          <div id="role-admin-panel" class="span6 well">
            <div class="login-role"><%=Messages.getInstance().getString("UI.PUC.LOGIN.ADMIN_USER")%></div>
            <div class="row-fluid">
              <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></div>
              <div class="span6 login-value">Admin</div>
            </div>
            <div class="row-fluid">
              <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></div>
              <div class="span6 login-value">password</div>
            </div>
            <button class="btn" onClick="loginAs('Admin', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.GO")%></button>
          </div>
          <div id="role-business-user-panel" class="span6 well ">
            <div class="login-role"><%=Messages.getInstance().getString("UI.PUC.LOGIN.BUSINESS_USER")%></div>
            <div class="row-fluid">
              <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></div>
              <div class="span6 login-value">Suzy</div>
            </div>
            <div class="row-fluid">
              <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></div>
              <div class="span6 login-value">password</div>
            </div>
            <button class="btn" onClick="loginAs('Suzy', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.GO")%></button>
        </div>
      </div>
      </div>

    </div>
  </div>
  <div id="login-footer-wrapper">
    <div id="login-footer" class="beforeSlide"><%=Messages.getInstance().getString("UI.PUC.LOGIN.COPYRIGHT", String.valueOf(year))%></div>
  </div>
</div>

<div id="loginError" class="pentaho-dialog">
  <div class="dialogTopCenterInner">
    <div class="Caption">
      <%=Messages.getInstance().getString("UI.PUC.LOGIN.ERROR.CAPTION")%>
    </div>
  </div>
  <div class="dialogMiddleCenterInner">
    <div class="dialog-content pentaho-padding-sm">
      <%=Messages.getInstance().getString("UI.PUC.LOGIN.ERROR")%>
    </div>
  </div>
  <div class="dialogMBottomCenterInner">
    <div class="button-panel">
      <button class="btn pull-right" onclick="document.getElementById('loginError').style.display='none'"><%=Messages.getInstance().getString("UI.PUC.LOGIN.OK")%></button>
    </div>
  </div>
</div>

<script type="text/javascript">

  <%
  if (showUsers) {
  %>

  function toggleEvalPanel() {
    var evaluationPanel = $("#evaluationPanel");
    evaluationPanel.toggleClass("afterSlide");
    $("#eval-arrow").toggleClass("closed");
  }
  <%
  }
  %>

  function bounceToReturnLocation() {
    // pass
    var locale = document.login.locale.value;

    var returnLocation = '<%=Encode.forJavaScript(requestedURL)%>';

    if (returnLocation != '' && returnLocation != null) {
      window.location.href = returnLocation;
    } else {
      window.location.href = window.location.href.replace("Login", "Home") + "?locale=" + locale;
    }

  }

  function doLogin() {

    // if we have a valid session and we attempt to login on top of it, the server
    // will actually log us out and will not log in with the supplied credentials, you must
    // login again. So instead, if they're already logged in, we bounce out of here to
    // prevent confusion.
    if (<%=loggedIn%>) {
      bounceToReturnLocation();
      return false;
    }

    jQuery.ajax({
      type: "POST",
      url: "j_spring_security_check",
      dataType: "text",
      data: $("#login").serialize(),

      error:function (xhr, ajaxOptions, thrownError){
        if (xhr.status == 404) {
          // if we get a 404 it means login was successful but intended resource does not exist
          // just let it go - let the user get the 404
          bounceToReturnLocation();
          return;
        }
        //Fix for BISERVER-7525
        //parsereerror caused by attempting to serve a complex document like a prd report in any presentation format like a .ppt
        //does not necesarly mean that there was a failure in the login process, status is 200 so just let it serve the archive to the web browser.
        if (xhr.status == 200 && thrownError == 'parsererror') {
          document.getElementById("j_password").value = "";
          bounceToReturnLocation();
          return;
        }
        // fail
        $("#loginError").show();
        $("#loginError button").focus();
      },

      success:function(data, textStatus, jqXHR){
        if (data.indexOf("j_spring_security_check") != -1) {
          // fail
          $("#loginError").show();
          $("#loginError button").focus();
          return false;
        } else {
          document.getElementById("j_password").value = "";
          bounceToReturnLocation();
        }
      }
    });
    return false;
  }

  function loginAs (username, password) {
    $("#j_username").prop("value", username);
    $("#j_password").prop("value", password);
    doLogin();
  }

  $(document).ready(function(){
    $("#login").submit(doLogin);

    if (<%=loggedIn%>) {
      bounceToReturnLocation();
    }


    $("#login-background").fadeIn(1000, function() {
      $("#login-logo").addClass("afterSlide");

      $("#animate-wrapper").addClass("afterSlide");
      $("#j_username").focus();

      $("#login-footer").addClass("afterSlide");

    });


  });
</script>
</body>
