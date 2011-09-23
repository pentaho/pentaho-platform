<%@page import="org.pentaho.platform.util.messages.LocaleHelper"%>

// Temporary fix for GWT localization BISERVER-3640
document.write("<meta name='gwt:property' content='locale=<%=LocaleHelper.getLocale().toString()%>'/>");
