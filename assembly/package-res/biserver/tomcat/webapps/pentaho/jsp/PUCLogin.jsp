<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@
    page language="java"
    import="org.springframework.security.ui.AbstractProcessingFilter,
            org.springframework.security.ui.webapp.AuthenticationProcessingFilter,
            org.springframework.security.ui.savedrequest.SavedRequest,
            org.springframework.security.AuthenticationException,
            org.pentaho.platform.uifoundation.component.HtmlComponent,
            org.pentaho.platform.engine.core.system.PentahoSystem,
            org.pentaho.platform.util.messages.LocaleHelper,
            org.pentaho.platform.api.engine.IPentahoSession,
            org.pentaho.platform.web.http.WebTemplateHelper,
            org.pentaho.platform.api.engine.IUITemplater,
      org.pentaho.platform.api.engine.IPluginManager,
            org.pentaho.platform.web.jsp.messages.Messages,
            java.util.List,
            java.util.ArrayList,
            java.util.StringTokenizer,
            org.apache.commons.lang.StringEscapeUtils,
            org.pentaho.platform.engine.core.system.PentahoSessionHolder,
            org.owasp.esapi.ESAPI,
            org.pentaho.platform.util.ServerTypeUtil"%>
<%!
  // List of request URL strings to look for to send 401

  private List<String> send401RequestList;

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
  Object reqObj = request.getSession().getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
  String requestedURL = "";
  if (reqObj != null) {
    requestedURL = ((SavedRequest) reqObj).getFullRequestUrl();

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

  boolean isPlatformServer = ServerTypeUtil.isPlatformServer();
  boolean showUsers = isPlatformServer && Boolean.parseBoolean(PentahoSystem.getSystemSetting("login-show-sample-users-hint", "true"));
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" class="bootstrap">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Pentaho User Console - Login</title>

  <%
    String ua = request.getHeader("User-Agent").toLowerCase();
    if (!"desktop".equalsIgnoreCase(request.getParameter("mode"))) {
      if (ua.contains("ipad") || ua.contains("ipod") || ua.contains("iphone") || ua.contains("android") || "mobile".equalsIgnoreCase(request.getParameter("mode"))) {
        IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
        List<String> pluginIds = pluginManager.getRegisteredPlugins();
        for (String id : pluginIds) {
          String mobileRedirect = (String)pluginManager.getPluginSetting(id, "mobile-redirect", null);
          if (mobileRedirect != null) {
            // we have a mobile redirect
  %>
  <script type="text/javascript">
    if(typeof window.top.PentahoMobile != "undefined"){
      window.top.location.reload();
    } else {
      document.write('<META HTTP-EQUIV="refresh" CONTENT="0;URL=<%=mobileRedirect%>">');
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

<meta name="gwt:property" content="locale=<%=ESAPI.encoder().encodeForHTMLAttribute(request.getLocale().toString())%>">
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

<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
<script src="bootstrap/js/html5shiv.js"></script>
<![endif]-->

</head>

<body class="pentaho-page-background">
<div id="login-wrapper">
  <div id="login-background">
    <div id="login-logo"></div>

    <div id="login-form-container">
      <div id="animate-wrapper">
        <h1><%=Messages.getInstance().getString("UI.PUC.LOGIN.TITLE")%></h1>
        <form name="login" id="login" action="j_spring_security_check" method="POST" class="form-inline">
          <div>
            <input id="j_username" name="j_username" type="text" placeholder="<%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%>">
            <input id="j_password" name="j_password" type="password" placeholder="<%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%>">
            <button type="submit" class="btn"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
            <input type="hidden" name="locale" value="en">
          </div>
          <div id="eval-users-toggle-container">
            <%
              if (showUsers) {
            %>
            <div id="eval-users-toggle" onClick="toggleEvalPanel()">
              <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.EVAL_LOGIN")%></div>
              <div class="custom-dropdown-arrow small"></div>
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

      <div id="evaluationPanel" class="row-fluid">
        <div class="span6 well">
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.ADMIN_USER")%></div>
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%> admin</div>
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%> password</div>
          <button class="btn" onClick="loginAs('admin', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
        </div>
        <div class="span6 well">
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.BUSINESS_USER")%></div>
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%> suzy</div>
          <div><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%> password</div>
          <button class="btn" onClick="loginAs('suzy', 'password');"><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></button>
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
  }
  <%
  }
  %>

  function bounceToReturnLocation() {
    // pass
    var locale = document.login.locale.value;

    var returnLocation = '<%=ESAPI.encoder().encodeForJavaScript(requestedURL)%>';

    if(/(iPad|iPod|iPhone)/.test(navigator.userAgent) || window.orientation !== undefined){
      returnLocation = CONTEXT_PATH+"content/analyzer/selectSchema";
    }


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
      },

      success:function(data, textStatus, jqXHR){
        if (data.indexOf("j_spring_security_check") != -1) {
          // fail
          $("#loginError").show();
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
    $("#j_username").attr("value", username);
    $("#j_password").attr("value", password);
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
