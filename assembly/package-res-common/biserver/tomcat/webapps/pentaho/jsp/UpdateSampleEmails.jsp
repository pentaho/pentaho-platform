
<%@ page language="java" contentType="text/html;" 
    import="
	javax.sql.DataSource,
	java.sql.Connection,
	java.sql.SQLException,
	java.sql.Statement,
	java.sql.ResultSet,
	java.sql.DriverManager,
	java.sql.PreparedStatement,
	org.pentaho.platform.api.data.IDBDatasourceService,
    org.pentaho.platform.engine.core.system.PentahoSystem,
    org.pentaho.platform.api.engine.IPentahoSession,
    org.pentaho.platform.web.jsp.messages.Messages,
    org.pentaho.platform.web.http.WebTemplateHelper,
    org.pentaho.platform.api.engine.IUITemplater,
    org.pentaho.platform.util.messages.LocaleHelper,
    org.pentaho.platform.engine.core.system.PentahoSessionHolder"
%><%   
/*
 * Copyright 2006 - 2010 Pentaho Corporation.  All rights reserved. 
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
*/

   response.setCharacterEncoding(LocaleHelper.getSystemEncoding()); 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();

	IPentahoSession userSession = PentahoSessionHolder.getSession();
   
   %>

<%!
  private static final String SampleDataJndiName = "SampleDataAdmin";
  // Try the datasource first. If unsuccessful, then go with the driver stuff
  private static final String SampleDataJDBCUrl = "jdbc:hsqldb:hsql://localhost/sampledata";
  private static final String SampleDataJDBCDriver = "org.hsqldb.jdbcDriver";
  private static final String SampleDataUserId = "pentaho_admin";
  private static final String SampleDataPassword = "password";
  private static final String SampleDataQuery = "SELECT REGION, MANAGER_NAME, EMAIL FROM DEPARTMENT_MANAGERS";
  private static final String SampleDataInsert = "INSERT INTO DEPARTMENT_MANAGERS (REGION, MANAGER_NAME, EMAIL) VALUES (?, ?, ?)";
  private static final String SampleDelete = "DELETE FROM DEPARTMENT_MANAGERS";
  private static final String SampleDataCount = "SELECT COUNT(*) FROM DEPARTMENT_MANAGERS";
  private static final int REGION_COLUMN = 0;
  private static final int MGR_COLUMN = 1;
  private static final int EMAIL_COLUMN = 2;
  private static final int COLUMN_COUNT = 3;
  private static DataSource sampleDataDS;
  private static boolean initialized = false;
  
  private static void initJdbc() {
      if (initialized) { 
        return; 
      }
      try {
        IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDBDatasourceService.class, null);
        sampleDataDS = datasourceService.getDataSource(SampleDataJndiName);
        if (sampleDataDS != null) {
          initialized = true;
          return;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      try {
        System.out.println("Couldn't load datasource - falling back to driver manager");
        initialized = true;
        Class.forName(SampleDataJDBCDriver);
      } catch (Exception ex) {
        System.out.println("Exception finding JDBC Driver " + SampleDataJDBCDriver);
      }
    
  }


  /*
   * Returns a 2-d array
   *   [0..rowcount-1][REGION_COLUMN] = Region
   *   [0..rowcount-1][MGR_COLUMN] = Mgr Name
   *   [0..rowcount-1][EMAIL_COLUMN] = Email Address
   * 
   * throws SQLException
   */
  private String[][] getExistingEmailUsers() throws SQLException {
    String[][] rtn = null;
    Connection conn = getConnection();
    try {
      Statement stmt = conn.createStatement();
      try {
        // Get count...
        int rowCount = 0;
        ResultSet rs = stmt.executeQuery(SampleDataCount);
        try {
          rs.next();
          rowCount = rs.getInt(1);
        } finally {
          rs.close();
        }
        if (rowCount == 0) {
          return rtn;
        }
        rtn = new String[rowCount][COLUMN_COUNT];
        
        rs = stmt.executeQuery(SampleDataQuery);
        try {
          int rowNum = 0;
          while (rs.next()) {
            rtn[rowNum][REGION_COLUMN] = rs.getString(REGION_COLUMN+1);
            rtn[rowNum][MGR_COLUMN] = rs.getString(MGR_COLUMN+1);
            rtn[rowNum][EMAIL_COLUMN] = rs.getString(EMAIL_COLUMN+1);
            rowNum++;
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      conn.close();
    }
    return rtn;
  }
  
  private static Connection getConnection() throws SQLException {
    return (sampleDataDS != null ? sampleDataDS.getConnection() : DriverManager.getConnection(SampleDataJDBCUrl, SampleDataUserId, SampleDataPassword));
  }

  /*
   * Updates the database from the 2-d array
   *   [0..rowcount-1][REGION_COLUMN] = Region
   *   [0..rowcount-1][MGR_COLUMN] = Mgr Name
   *   [0..rowcount-1][EMAIL_COLUMN] = Email Address
   * param userUpdates 2-d array as follows:
   * throws SQLException
   */
  private void setExistingEmailUsers(String[][] userUpdates) throws SQLException {
    Connection conn = getConnection();
    try {
      conn.setAutoCommit(false);
      try {
         // Delete all from the table.
         Statement delStmt = conn.createStatement();
         try {
           System.out.println("Executing: " + delStmt.toString());
           delStmt.executeUpdate(SampleDelete);
         } finally {
           delStmt.close();
         }

         PreparedStatement stmt = conn.prepareStatement(SampleDataInsert);
         try {
           for (int rowNum = 0; rowNum < userUpdates.length; rowNum++) {
             stmt.setString(REGION_COLUMN+1, userUpdates[rowNum][REGION_COLUMN]);
             stmt.setString(MGR_COLUMN+1, userUpdates[rowNum][MGR_COLUMN]);
             stmt.setString(EMAIL_COLUMN+1, userUpdates[rowNum][EMAIL_COLUMN]);
             System.out.println("Executing: " + stmt.toString());
             stmt.executeUpdate();
           }
         } finally {
           stmt.close();
         }
         conn.commit();
       } catch (SQLException ex) {
         ex.printStackTrace();
         conn.rollback();
       }
    } finally {
      conn.setAutoCommit(true);
      conn.close();
    }
    
  }
%><%
    initJdbc();
        
    String action = request.getParameter("action");
    if( "update".equals( action ) ) {
		// update the records in the database
		int index = 0;
		boolean running = true;
		while(running) {
			String region = request.getParameter( "region"+index );
			if( region == null ) {
				running = false;
			} else {
				index++;
			}
		}
		String data[][] = new String[index][4];
		index = 0;
		running = true;
		while(running) {
			String region = request.getParameter( "region"+index );
			String name = request.getParameter( "name"+index );
			String email = request.getParameter( "email"+index );
			if( region != null && name != null && email != null ) {
				data[index][REGION_COLUMN] = region;
				data[index][MGR_COLUMN] = name;
				data[index][EMAIL_COLUMN] = email;
				index++;
			} else {
				running = false;
			}
		}
		if( index > 0 ) {
			setExistingEmailUsers( data );
		}
    }

	String intro = "";
	String footer = "";
  	String[][] existingUsers = getExistingEmailUsers();
	
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template-document.html", Messages.getInstance().getString("UI.USER_BURST_EDIT_DATA_TITLE"), userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}

  %><%= intro %>

<div style="margin-left:10px;">
<span class="portlet-font"><%= Messages.getInstance().getString("UI.USER_BURST_EDIT_DATA_HINT") %></span>
<p/>
  <form name="burst_edit" method="GET">
  <table>
    <thead>
      <tr>
        <td class="portlet-table-header"><%= Messages.getInstance().getString("UI.USER_BURST_REGION") %></td>
        <td class="portlet-table-header"><%= Messages.getInstance().getString("UI.USER_BURST_MANAGER") %></td>
        <td class="portlet-table-header"><%= Messages.getInstance().getString("UI.USER_BURST_EMAIL") %></td>
      </tr>
    </thead>
        <% for (int rowNum = 0; rowNum < existingUsers.length; rowNum++) {%>
        <tr>
          <td class="portlet-table-text"><input type="hidden" name="region<%= rowNum %>" value="<%=existingUsers[rowNum][REGION_COLUMN]%>"><%=existingUsers[rowNum][0]%></td>
          <td><input class="portlet-form-input-field" name="name<%= rowNum %>" value="<%=existingUsers[rowNum][MGR_COLUMN]%>"></td>
          <td><input class="portlet-form-input-field" name="email<%= rowNum %>" size="40" value="<%=existingUsers[rowNum][EMAIL_COLUMN]%>"></td>
        </tr>
        <% } %>
  </table>
  <p/>
  <input type="hidden" name="action" value="update">
  <input type="submit" class="portlet-form-button" value="<%= Messages.getInstance().getString("UI.USER_UPDATE") %>"/>
  </form>
</div>

<%= footer %>