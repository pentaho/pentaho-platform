<%@ page session="true" contentType="text/html; charset=ISO-8859-1" %>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:testQuery id="query01" onColumns="Measures" onRows="Products Region">
  dummy text
</jp:testQuery>

<c:set var="title01" scope="session">Test Query with built in Test Data</c:set>

