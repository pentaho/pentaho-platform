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
* Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
--%>

<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@
    page language="java"
    import="org.springframework.security.web.savedrequest.SavedRequest,
            org.pentaho.platform.web.http.security.PreventBruteForceException,
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
            javax.servlet.http.HttpSession,
            org.pentaho.platform.engine.core.system.PentahoSessionHolder,
            org.owasp.encoder.Encode"%>
<%!
  // List of request URL strings to look for to send 401

  private List<String> send401RequestList;

  public final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";
  public final String SPRING_SECURITY_LAST_EXCEPTION_KEY = "SPRING_SECURITY_LAST_EXCEPTION";

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
<%!
  public boolean isUserBlocked(HttpSession session) {
    Object springLastException = session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION_KEY);
    return springLastException != null && springLastException instanceof PreventBruteForceException;
  }
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
      if ( ua.contains( "ipad" )   || ua.contains( "ipod" )    ||
           ua.contains( "iphone" ) || ua.contains( "android" ) ||
           "mobile".equalsIgnoreCase( request.getParameter( "mode" ) ) ) {

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
<link rel="icon" href="/pentaho-style/favicon.ico"/>
<link rel="apple-touch-icon" sizes="180x180" href="/pentaho-style/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/pentaho-style/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/pentaho-style/favicon-16x16.png">
<link rel="mask-icon" href="/pentaho-style/safari-pinned-tab.svg" color="#cc0000">

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
  <div id="login-header-wrapper">
    <div id="login-header-logo"></div>
    <div id="login-header-separator-box">
      <div id="login-header-separator"></div>
      <div id="login-header-separator-padding"></div>
    </div>
    <div id="login-header-app-name"><%=Messages.getInstance().getString("UI.PUC.LOGIN.HEADER.APPNAME")%></div>
  </div>
  <div id="login-background-main">
    <div id="login-background-opacity">
      <div id="login-background">

        <div id="login-title"><%=Messages.getInstance().getString("UI.PUC.LOGIN.WELCOME")%></div>
        <div id="login-messages" class="none-login-message-visible">
          <div id="loginError" class="login-error-message">
            <div class="login-error-icon"></div>
            <div class="login-error-text"><%=Messages.getInstance().getString("UI.PUC.LOGIN.ERROR")%></div>
          </div>
          <div id="loginBlocked" class="login-error-message">
            <div class="login-error-icon"></div>
            <div class="login-error-text"><%=Messages.getInstance().getString("UI.PUC.LOGIN.BLOCKED")%></div>
          </div>
        </div>

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
            <form name="login" id="login" action="j_spring_security_check" method="POST">
              <div class="row-fluid nowrap">
                <div class="space-10"></div>
                <div class="input-container">
                  <label><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></label>
                  <input id="j_username" name="j_username" type="text" placeholder="" autocomplete="off">
                </div>
                <div class="space-30"></div>
                <div class="input-container">
                  <label><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></label>
                  <input id="j_password" name="j_password" type="password"
                         placeholder=""
                         autocomplete="off">
                </div>
                <div class="space-60"></div>
                <div class="input-container">
                  <button type="submit" id="loginbtn" class="btn"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
                </div>
                <div class="space-60"></div>
              </div>
              <div class="space-60"></div>
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
                <button class="btn" onClick="loginAs('Admin', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
              </div>
              <div id="role-business-user-panel" class="span6 well">
                <div class="login-role"><%=Messages.getInstance().getString("UI.PUC.LOGIN.BUSINESS_USER")%></div>
                <div class="row-fluid">
                  <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></div>
                  <div class="span6 login-value">Suzy</div>
                </div>
                <div class="row-fluid">
                  <div class="span6 login-label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></div>
                  <div class="span6 login-value">password</div>
                </div>
                <button class="btn" onClick="loginAs('Suzy', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
              </div>
            </div>
          </div>

          <div class="space-30"></div>

        </div>
      </div>
    </div>
  </div>
  <div id="login-footer-wrapper">
    <div id="login-footer-company"><%=Messages.getInstance().getString("UI.PUC.LOGIN.FOOTER.COMPANY")%></div>
    <div id="login-footer-copyright"><%=Messages.getInstance().getString("UI.PUC.LOGIN.COPYRIGHT", String.valueOf(year))%></div>
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
    var returnLocation = '<%=Encode.forJavaScript(requestedURL)%>';

    if (returnLocation != '' && returnLocation != null) {
      window.location.href = returnLocation;
    } else {
      window.location.href = window.location.href.replace("Login", "Home");
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

    <% if (isUserBlocked(session)) { %>
    var userState = 'j_spring_security_user_blocked';
    <% }
    else { %>
    var userState = '';
    <% } %>

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
        showOneErrorMessage('loginError');
      },

      success:function(data, textStatus, jqXHR){
        if (data.indexOf("j_spring_security_check") != -1) {
          // fail
          if( userState === 'j_spring_security_user_blocked' || data.match(/j_spring_security_user_blocked/g).length > 2 ){
            showOneErrorMessage('loginBlocked');
          }
          else {
            showOneErrorMessage('loginError');
          }
          return false;
        } else {
          document.getElementById("j_password").value = "";
          bounceToReturnLocation();
        }
      }
    });
    return false;
  }

  function showOneErrorMessage(divId) {
    var msgs = document.getElementsByClassName('login-error-message');
    var isSomeMessageVisible = false;
    if(msgs && msgs.length > 0) {
      for (var i = 0; i < msgs.length; i++) {
        if(msgs[i].id === divId) {
          msgs[i].style.display='inline-flex';
          isSomeMessageVisible = true;
        } else {
          msgs[i].style.display='none';
        }
      }
    }

    if(isSomeMessageVisible){
      document.getElementById('login-messages').className='some-login-message-visible';
    } else {
      document.getElementById('login-messages').className='none-login-message-visible';
    }
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
      $("#animate-wrapper").addClass("afterSlide");
      $("#j_username").focus();
    });


  });
</script>
</body>
