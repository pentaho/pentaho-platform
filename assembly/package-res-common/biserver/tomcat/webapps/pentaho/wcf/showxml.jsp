<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE ROOT [
<!ENTITY nbsp "&#160;">
<!ENTITY Uuml "&#220;">
<!ENTITY uuml "&#252;">
<!ENTITY Auml "&#196;">
<!ENTITY auml "&#228;">
<!ENTITY Ouml "&#214;">
<!ENTITY ouml "&#246;">
<!ENTITY szlig "&#223;">
]>
<%@ page
  session="true"
  contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
%>

<%@ taglib prefix="wcf" uri="http://www.tonbeller.com/wcf" %>

<%--
  Displays XML of WCF Components in the Browser. Place a link
  like <a href="../wcf/showxml.jsp?render=myComponentId&amp;iehack=file.xml">Show XML of myComponentId</a>
  on the HTML Page
--%>

<%
  HttpServletRequest hsr = (HttpServletRequest)pageContext.getRequest();
  String id = hsr.getParameter("render");
  Object o = pageContext.getSession().getAttribute(id);
  pageContext.setAttribute("renderXML", o, pageContext.REQUEST_SCOPE);
%>

<ROOT>
  <wcf:render id="renderXML" ref="renderXML" xslUri="/WEB-INF/wcf/showxml.xsl" xslCache="true"/>
</ROOT>
