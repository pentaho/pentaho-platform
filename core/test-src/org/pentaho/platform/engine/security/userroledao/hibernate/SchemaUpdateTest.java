/*
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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.engine.security.userroledao.hibernate;

import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.CREATE;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.hsqldb.Types;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that DDL generation by Hibernate is as expected. It generates the DDL by reading the hbm.xml file.
 * 
 * @author mlowery
 */
public class SchemaUpdateTest {

  private static final String YES = "YES"; //$NON-NLS-1$

  private static final String NO = "NO"; //$NON-NLS-1$

  private static final String IS_NULLABLE = "IS_NULLABLE"; //$NON-NLS-1$

  private static final String COLUMN_SIZE = "COLUMN_SIZE"; //$NON-NLS-1$

  private static final String DATA_TYPE = "DATA_TYPE"; //$NON-NLS-1$

  private static final String COLUMN_NAME = "COLUMN_NAME"; //$NON-NLS-1$

  private Connection connection;

  @Before
  public void setUp() throws Exception {
    connection = TestUtil.getConnection();
  }

  @After
  public void tearDown() throws Exception {
    if (connection != null) {
      connection.close();
    }
  }

  @Test
  public void doTest() throws Exception {

    TestUtil.generateAndExecuteDdl(CREATE);

    ResultSet rs = null;

    // check column and table existence
    DatabaseMetaData meta = connection.getMetaData();

    // check USERS table
    rs = meta.getColumns(null, null, "USERS", null); //$NON-NLS-1$
    checkUsersTable(rs);
    rs.close();

    // check AUTHORITIES table
    rs = meta.getColumns(null, null, "AUTHORITIES", null); //$NON-NLS-1$
    checkAuthoritiesTable(rs);
    rs.close();

    // check GRANTED_AUTHORITIES table
    rs = meta.getColumns(null, null, "GRANTED_AUTHORITIES", null); //$NON-NLS-1$
    checkGrantedAuthoritiesTable(rs);
    rs.close();

  }

  protected void checkUsersTable(ResultSet rs) throws Exception {
    boolean usernameColFound = false;
    boolean passwordColFound = false;
    boolean enabledColFound = false;
    boolean descColFound = false;
    while (rs.next()) {
      String columnName = rs.getString(COLUMN_NAME);
      int dataType = rs.getInt(DATA_TYPE);
      int columnSize = rs.getInt(COLUMN_SIZE);
      String isNullableString = rs.getString(IS_NULLABLE);

      if ("USERNAME".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 50);
        assertTrue(NO.equals(isNullableString));
        usernameColFound = true;
      }
      if ("PASSWORD".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 50);
        assertTrue(NO.equals(isNullableString));
        passwordColFound = true;
      }
      if ("ENABLED".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.BOOLEAN);
        assertTrue(NO.equals(isNullableString));
        enabledColFound = true;
      }
      if ("DESCRIPTION".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 100);
        assertTrue(YES.equals(isNullableString));
        descColFound = true;
      }

    }
    assertTrue(usernameColFound && passwordColFound && enabledColFound && descColFound);
  }

  protected void checkAuthoritiesTable(ResultSet rs) throws Exception {
    boolean authorityColFound = false;
    boolean descColFound = false;
    while (rs.next()) {
      String columnName = rs.getString(COLUMN_NAME);
      int dataType = rs.getInt(DATA_TYPE);
      int columnSize = rs.getInt(COLUMN_SIZE);
      String isNullableString = rs.getString(IS_NULLABLE);

      if ("AUTHORITY".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 50);
        assertTrue(NO.equals(isNullableString));
        authorityColFound = true;
      }
      if ("DESCRIPTION".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 100);
        assertTrue(YES.equals(isNullableString));
        descColFound = true;
      }

    }
    assertTrue(authorityColFound && descColFound);
  }

  protected void checkGrantedAuthoritiesTable(ResultSet rs) throws Exception {
    boolean authorityColFound = false;
    boolean usernameColFound = false;
    while (rs.next()) {
      String columnName = rs.getString(COLUMN_NAME);
      int dataType = rs.getInt(DATA_TYPE);
      int columnSize = rs.getInt(COLUMN_SIZE);
      String isNullableString = rs.getString(IS_NULLABLE);

      if ("AUTHORITY".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 50);
        assertTrue(NO.equals(isNullableString));
        authorityColFound = true;
      }
      if ("USERNAME".equals(columnName)) { //$NON-NLS-1$
        assertTrue(dataType == Types.VARCHAR);
        assertTrue(columnSize == 50);
        assertTrue(NO.equals(isNullableString));
        usernameColFound = true;
      }

    }
    assertTrue(authorityColFound && usernameColFound);
  }

}
