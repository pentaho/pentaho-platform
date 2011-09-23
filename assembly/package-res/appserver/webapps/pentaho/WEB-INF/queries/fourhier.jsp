<%@ page session="true" contentType="text/html; charset=ISO-8859-1" %>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:mondrianQuery id="query01" jdbcDriver="sun.jdbc.odbc.JdbcOdbcDriver" jdbcUrl="jdbc:odbc:MondrianFoodMart" catalogUri="/WEB-INF/queries/FoodMart.xml">
select {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} on columns,
{([Gender].[All Gender], [Marital Status].[All Marital Status],
  [Customers].[All Customers],
  [Product].[All Products] ) } on rows
  from Sales where ([Time].[1997])

</jp:mondrianQuery>

<c:set var="title01" scope="session">4 hierarchies on one axis</c:set>
