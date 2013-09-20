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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
--%>

<%@ page import="java.text.DecimalFormat" %>
<%@ page import="org.pentaho.platform.repository.hibernate.HibernateUtil" %>
<%@ page import="org.hibernate.SessionFactory" %>



<% SessionFactory _hibSessionFactory = HibernateUtil.getSessionFactory(); %>

<TABLE BORDER="1" BGCOLOR="#e8e8ff" style="border: 1px solid #000000;">
<TR>
	<TD ALIGN="RIGHT"><b>Region Name</b></TD>
	<TD ALIGN="RIGHT"><b>Cache Puts</b></TD>
	<TD ALIGN="RIGHT"><b>Cache Hits</b></TD>
	<TD ALIGN="RIGHT"><b>Cache Misses</b></TD>
	<TD ALIGN="RIGHT"><b>Cache Hit %</b></TD>
	<TD ALIGN="RIGHT"><b>Elements in Memory</b></TD>
	<TD ALIGN="RIGHT"><b>Memory Used</b></TD>
	<TD ALIGN="RIGHT"><b>Elements on Disk</b></TD>
</TR>

<%    String regionNames[] = _hibSessionFactory.getStatistics().getSecondLevelCacheRegionNames();
      int totalItemsInMemory = 0;
      int totalItemsOnDisk = 0;
      float totalSizeInMemory = 0;
      DecimalFormat df = new DecimalFormat("#,##0.#");
	  for (int i=0;i<regionNames.length;i++) {
		if (regionNames[i].indexOf("StandardQueryCache") == -1) { %>
          <TR>
		<TD ALIGN="RIGHT"><%= regionNames[i] %></TD>
		<TD ALIGN="RIGHT"><%= _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getPutCount() %></TD>
		<TD ALIGN="RIGHT"><%= _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getHitCount() %></TD>
		<TD ALIGN="RIGHT"><%= _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getMissCount() %></TD>
<%          if (_hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getHitCount() > 0) { %>
		  <TD ALIGN="RIGHT"><%= (int)(((float)_hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getHitCount() / (float)(_hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getHitCount() + _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getMissCount())) * 100) + "%" %></TD>
<%          } else { %>
		  <TD ALIGN="RIGHT"><%= "0%" %></TD>
<%          }
float sizeInMemory = _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getSizeInMemory();

totalItemsInMemory+=_hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getElementCountInMemory();
totalItemsOnDisk+=_hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getElementCountOnDisk();
totalSizeInMemory+=sizeInMemory;


 %>
		<TD ALIGN="RIGHT"><%= _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getElementCountInMemory() %></TD>
		<TD ALIGN="RIGHT"><%= df.format(sizeInMemory/1024) + "k" %></TD>
		<TD ALIGN="RIGHT"><%= _hibSessionFactory.getStatistics().getSecondLevelCacheStatistics(regionNames[i]).getElementCountOnDisk() %></TD>
	    </TR>
<%        }  
        }  %>

<TR>
<TD ALIGN="RIGHT"><b>Second Level Cache Totals</b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getSecondLevelCachePutCount() %></b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() %></b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getSecondLevelCacheMissCount() %></b></TD>
<%          if (_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() > 0) { %>
		  <TD ALIGN="RIGHT"><b><%= (int)(((float)_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() / (float)(_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() + _hibSessionFactory.getStatistics().getSecondLevelCacheMissCount())) * 100) + "%" %></b></TD>
<%          } else { %>
		  <TD ALIGN="RIGHT"><b><%= "0%" %></b></TD>
<%          } %>
<TD ALIGN="RIGHT"><b>-</b></TD>
<TD ALIGN="RIGHT"><b>-</b></TD>
<TD ALIGN="RIGHT"><b>-</b></TD>
</TR>

<TR>
<TD ALIGN="RIGHT"><b>Query Cache Totals</b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getQueryCachePutCount() %></b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getQueryCacheHitCount() %></b></TD>
<TD ALIGN="RIGHT"><b><%= _hibSessionFactory.getStatistics().getQueryCacheMissCount() %></b></TD>
<%          if (_hibSessionFactory.getStatistics().getQueryCacheHitCount() > 0) { %>
		  <TD ALIGN="RIGHT"><b><%= (int)(((float)_hibSessionFactory.getStatistics().getQueryCacheHitCount() / (float)(_hibSessionFactory.getStatistics().getQueryCacheHitCount() + _hibSessionFactory.getStatistics().getQueryCacheMissCount())) * 100) + "%" %></b></TD>
<%          } else { %>
		  <TD ALIGN="RIGHT"><b><%= "0%" %></b></TD>
<%          } %>
<TD ALIGN="RIGHT"><b>-</b></TD>
<TD ALIGN="RIGHT"><b>-</b></TD>
<TD ALIGN="RIGHT"><b>-</b></TD>
</TR>

<TR>
<TD ALIGN="RIGHT"><b><u>Grand Totals</u></b></TD>
<TD ALIGN="RIGHT"><b><u><%= _hibSessionFactory.getStatistics().getSecondLevelCachePutCount() +  _hibSessionFactory.getStatistics().getQueryCachePutCount() %></u></b></TD>
<TD ALIGN="RIGHT"><b><u><%= _hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() + _hibSessionFactory.getStatistics().getQueryCacheHitCount() %></u></b></TD>
<TD ALIGN="RIGHT"><b><u><%= _hibSessionFactory.getStatistics().getSecondLevelCacheMissCount() + _hibSessionFactory.getStatistics().getQueryCacheMissCount() %></u></b></TD>
<%          if (_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() + _hibSessionFactory.getStatistics().getQueryCacheHitCount() > 0) { %>
		  <TD ALIGN="RIGHT"><b><u><%= (int)(((float)(_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() + _hibSessionFactory.getStatistics().getQueryCacheHitCount()) / (float)(_hibSessionFactory.getStatistics().getSecondLevelCacheHitCount() + _hibSessionFactory.getStatistics().getQueryCacheHitCount() + _hibSessionFactory.getStatistics().getSecondLevelCacheMissCount() + _hibSessionFactory.getStatistics().getQueryCacheMissCount())) * 100) + "%" %></u></b></TD>
<%          } else { %>
		  <TD ALIGN="RIGHT"><b><u><%= "0%" %></u></b></TD>
<%          } %>
<TD ALIGN="RIGHT"><b><u><%= totalItemsInMemory %></u></b></TD>
<TD ALIGN="RIGHT"><b><u><%= df.format(totalSizeInMemory/1024) + "k" %></u></b></TD>
<TD ALIGN="RIGHT"><b><u><%= totalItemsOnDisk %></u></b></TD>
</TR>



</TABLE>

