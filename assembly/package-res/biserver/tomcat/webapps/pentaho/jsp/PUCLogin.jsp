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
            org.owasp.esapi.ESAPI"%>
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

boolean showUsers = Boolean.parseBoolean(PentahoSystem.getSystemSetting("login-show-sample-users-hint", "true"));

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
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


<style type="text/css">
<!--
html, body {
  margin:0;
  padding:0;
  height:100%;
  border:none
}
#container_header {
  margin: 0 auto;
  padding: 0;
  width:740px;
  height: 94px;
  display: block;
}
#links{
Float: right;
clear: both;
color: #828282;
padding: 8px 0 0 0;
}
#links a{
  color: #999;
  text-decoration: none;
  font-size: .8em;
}
#container_content {
  margin: 0 auto;
  padding: 0;
  width:740px;
  height: 335px;
  font-family: Tahoma, Arial, sans-serif;
  display: block;
  background-image: url(/pentaho-style/images/login/middle_shadows.png);
  background-repeat:no-repeat;
}
#container_footer {
  margin: 0 auto;
  padding: 0;
  width:740px;
  height: 100%;
  color: #000;
  font-size: .75em;
  /* padding: 8px 0 0 80px;*/
  display: block;
  background-image: url(/pentaho-style/images/login/middle_shadows_footer.png);
  background-repeat:no-repeat;
}
#message {
  color: #FFF;
  font-size: 1.05em;
  font-family: Tahoma, Arial, sans-serif;
  float: left;
  clear: both;
  display: block;
  width: 260px;
  padding: 20px 10px 0 40px;
  line-height: 1.85em;
}
.dark {
  background-image: url(/pentaho-style/images/login/content_bg.png);
  background-position:bottom;
  background-repeat:repeat-x;
  height: 225px;
}
a {
  color: #e17b03
}
.IE .pentaho-rounded-panel {
  border: 1px solid #ccc;
}
-->
</style>
<meta name="gwt:property" content="locale=<%=ESAPI.encoder().encodeForHTMLAttribute(request.getLocale().toString())%>">
<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />
<script language="javascript" type="text/javascript" src="webcontext.js"></script>
</head>

<body class="pentaho-page-background">
<div id="loginError" class="pentaho-dialog" style="width: 400px; display: none">
  <div class="Caption">
    <span>Login Error</span>
  </div>
  <div style="width: auto; height: auto;">
    <table class="dialog-content pentaho-padding-sm" style="width: 100%;">
      <tbody>
        <tr>
          <td>
            <span class="label"><%=Messages.getInstance().getString("UI.PUC.LOGIN.ERROR")%></span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <table class="button-panel" style="width: 100%;">
    <tbody>
      <tr>
        <td style="width: 100%;"> </td>
        <td>
           <button class="pentaho-button" onclick="document.getElementById('loginError').style.display='none'"><%=Messages.getInstance().getString("UI.PUC.LOGIN.OK")%></button>
        </td>
      </tr>
    </tbody>
  </table>
</div>
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
  <tr height="94">
    <td bgcolor="#FFFFFF"><div id="container_header"><div id="links"><a href="http://www.pentaho.com" target="_blank">www.pentaho.com</a> | <a href="http://www.pentaho.com/contact/?puc=y" target="_blank"><%=Messages.getInstance().getString("UI.PUC.LOGIN.CONTACT_US")%></a></div>
        <div class="pentaho-rounded-panel" style="width: 323px; padding: 20px 20px 20px 20px; position: absolute; margin: 199px 0 0 380px;">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <form name="login" id="login" action="j_spring_security_check" method="POST">
              <tr>
                <td colspan="1" rowspan="7" style="padding: 20px 20px 0 0;"><img src="/pentaho-style/images/login/lock.png" width="100" height="172"></td>
                <td colspan="2" ><span style="color: #FFF; font-size: 1.7em; font-family: &quot;Franklin Gothic Demi&quot;, Tahoma, Arial; "><%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%></span></td>
              </tr>
              <tr>
                <td colspan="2" style="padding: 10px 0 4px 0;"><select style="display:none;" id="locale" name="locale">
                    <option value="de">German</option>
                    <option value="en" selected="selected">English</option>
                    <option value="es">Spanish</option>
                    <option value="fr">French</option>
                    <option value="ja">Japanese</option>
                  </select>
                <label style="color: #FFF; font-size:.85em; font-family: Tahoma, Arial, sans-serif; text-shadow: 0px 1px 1px #000;" for="userid"><%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%></label></td>
              </tr>
              <tr>
                <td colspan="2">
          <input  style="border:1px solid #333; padding: 4px; width:190px;height:17px;" id="j_username" name="j_username" type="text"/>
                </td>
              </tr>
              <tr>
                <td colspan="2" style="padding: 5px 0 4px 0;">
					<label style="padding: 15px 0 2px 0; color: #FFF; font-size:.85em; font-family: Tahoma, Arial, sans-serif; text-shadow: 0px 1px 1px #000;" for="password"><%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%></label>
        </td>
              </tr>
              <tr>
                <td colspan="2">
          <input style="border:1px solid #333; padding: 4px; width:190px;height:17px;" id="j_password" name="j_password" type="password"/>
        </td>
              </tr>
              <tr>
                <td colspan="2" align="left" style="padding:5px 0 2px 0px;">
          <input id="launchInNewWindow" name="Launch in new window" type="checkbox" value="" />
					<span style="padding:0px 0 2px 0px; color:#fff; font-size:.8em; font-family: Tahoma, Arial, sans-serif;"><%=Messages.getInstance().getString("UI.PUC.LOGIN.NEW_WINDOW")%></span>
        </td>
              </tr>
              <tr>
                <td style="padding:4px 0 0 0px;">
                <%
                if (showUsers) {
                %>
          <img src="/pentaho-style/images/login/about.png" width="18" height="16" align="absmiddle"/>
				  <a style="color: #fff; padding: 0 4px 0px 4px; font-size: .8em;" href="#" onClick="toggleEvalPanel()"><%=Messages.getInstance().getString("UI.PUC.LOGIN.EVAL_LOGIN")%></a>
        <%
        } else {
        %>
          &nbsp;
        <%
        }
        %>
                </td>       
                <td style="padding:4px 0 0 0px;">
                  <input class="pentaho-button" value="<%=Messages.getInstance().getString("UI.PUC.LOGIN.LOGIN")%>" type="submit" style="float:right; clear: both;"/>
                </td>
              </tr>
              <%
                if (showUsers) {
              %>
              <tr>
                <td id="evaluationPanel" colspan="3" style="padding: 30px 20px 0 0; display: none;">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td style="font-size: .8em;"><strong><%=Messages.getInstance().getString("UI.PUC.LOGIN.ADMIN_USER")%></strong><br>
                        <%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%> admin<br>
                        <%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%> password</td>
                      <td style="font-size: .8em;"><strong><%=Messages.getInstance().getString("UI.PUC.LOGIN.BUSINESS_USER")%></strong><br>
                        <%=Messages.getInstance().getString("UI.PUC.LOGIN.USERNAME")%> suzy<br>
                        <%=Messages.getInstance().getString("UI.PUC.LOGIN.PASSWORD")%> password</td>
                    </tr>
                    <tr>
                    <td colspan="3" style="padding: 4px 20px 0 0; font-size: .8em;"><a href="http://www.pentaho.com/helpmeout/" target="_blank"><%=Messages.getInstance().getString("UI.PUC.LOGIN.REQUEST_SUPPORT")%></a><img src="/pentaho-style/images/login/help_link.png" width="20" height="20" align="absbottom"></td>
                    </tr>
                  </table>
        </td>
              </tr>
              <%
        }
        %>
            </form>
          </table>
        </div>
        <a href="http://www.pentaho.com" target="_blank"><img src="/pentaho-style/images/login/logo.png" alt="Pentaho Corporation" width="224" height="94" border="0" /></a></div></td>
  </tr>
  <tr height="334">
    <td class="dark"><div id="container_content"><img src="/pentaho-style/images/login/title_text.png">
        <div id="message"><%=Messages.getInstance().getString("UI.PUC.LOGIN.MESSAGE")%></div>
      </div></td>
  </tr>
  <tr height="100%">
    <td bgcolor="#FFFFFF" valign="top"><div id="container_footer" style="padding: 4px 20px 0 80px; height:200px;"><%=Messages.getInstance().getString("UI.PUC.LOGIN.COPYRIGHT", String.valueOf(year))%></div></td>
  </tr>
</table>


<script type="text/javascript">

function DisplayAlert(id,left,top) {
  document.getElementById(id).style.left=left+'%';
  document.getElementById(id).style.top=top+'%';
  document.getElementById(id).style.display='block';
}

document.getElementById('j_username').focus();

<%
if (showUsers) {
%>

function toggleEvalPanel() {
  var evaluationPanel = document.getElementById("evaluationPanel");
  var display = evaluationPanel.style.display;
  if (display == "none") {
    evaluationPanel.style.display = "";
  } else {
    evaluationPanel.style.display = "none";
  }
}
<%
}
%>

function bounceToReturnLocation() {
  // pass
  var locale = document.login.locale.options[document.login.locale.selectedIndex].value;
  
  var returnLocation = '<%=ESAPI.encoder().encodeForJavaScript(requestedURL)%>';

  if(/(iPad|iPod|iPhone)/.test(navigator.userAgent) || window.orientation !== undefined){
    returnLocation = CONTEXT_PATH+"content/analyzer/selectSchema";
  }


  if (document.getElementById("launchInNewWindow").checked) {
    if (returnLocation != '' && returnLocation != null) {
      window.open(returnLocation, '_blank', 'menubar=no,location=no,resizable=yes,scrollbars=yes,status=no');
    } else {
      window.open(window.location.href.replace("Login", "Home") + "?locale=" + locale, '_blank', 'menubar=no,location=no,resizable=yes,scrollbars=yes,status=no');
    }
  } else {
    if (returnLocation != '' && returnLocation != null) {
        window.location.href = returnLocation;
    } else {
        window.location.href = window.location.href.replace("Login", "Home") + "?locale=" + locale;
    }
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
       DisplayAlert('loginError', 40, 30);
      },
            
        success:function(data, textStatus, jqXHR){
      if (data.indexOf("j_spring_security_check") != -1) {
        // fail
	    DisplayAlert('loginError', 40, 30);
	    return false;
      } else {
        document.getElementById("j_password").value = "";
        bounceToReturnLocation();
      }
        }
        
    });
    return false;
}

$(document).ready(function(){
    $("#login").submit(doLogin);

  if (<%=loggedIn%>) {
    bounceToReturnLocation();
  }
});
</script>
</body>
