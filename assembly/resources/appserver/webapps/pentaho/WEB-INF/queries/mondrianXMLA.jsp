<%@ page session="true" contentType="text/html; charset=ISO-8859-1" %>
<%@ taglib uri="http://www.tonbeller.com/jpivot" prefix="jp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jp:xmlaQuery id="query01"
    uri="http://localhost:8080/jpivot/xmla"
    dataSource="Provider=Mondrian"
    catalog="MondrianFoodMart">
select
  {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} on columns,
  {([Promotion Media].[All Media], [Product].[All Products])} ON rows
from Sales
where ([Time].[1997])
</jp:xmlaQuery>

<c:set var="title01" scope="session">Mondrian OLAP via XML/A</c:set>
