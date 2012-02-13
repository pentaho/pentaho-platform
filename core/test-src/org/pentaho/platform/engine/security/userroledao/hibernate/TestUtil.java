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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;

import static org.junit.Assert.assertTrue;

/**
 * Methods to support testing. Contains methods that use only JDBC for test verification.
 * 
 * @author mlowery
 */
public class TestUtil {

  public static enum DdlType {
    CREATE, DROP, ALTER
  };

  private static Configuration cfg;

  static {
    cfg = new Configuration()
        .addResource("PentahoUser.hbm.xml").addResource("PentahoRole.hbm.xml").setProperty(Environment.DIALECT, //$NON-NLS-1$ //$NON-NLS-2$
            "org.hibernate.dialect.HSQLDialect").setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver") //$NON-NLS-1$ //$NON-NLS-2$
        .setProperty(Environment.URL, "jdbc:hsqldb:mem:test").setProperty(Environment.SHOW_SQL, "true").setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
  }

  public static void generateAndExecuteDdl(DdlType type) throws SQLException {
    String[] sqls = null;
    switch (type) {
      case CREATE:
        // generate schema creation script
        sqls = cfg.generateSchemaCreationScript(Dialect.getDialect(cfg.getProperties()));
        break;
      case DROP:
        // generate drop script
        sqls = cfg.generateDropSchemaScript(Dialect.getDialect(cfg.getProperties()));
        break;
      case ALTER:
        throw new IllegalArgumentException();
    }
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = getConnection();
      stmt = conn.createStatement();
      for (String sql : sqls) {
        stmt.addBatch(sql);
      }
      // execute schema creation script
      stmt.executeBatch();
      stmt.close();
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  public static Connection getConnection() throws SQLException {
    return ConnectionProviderFactory.newConnectionProvider(cfg.getProperties()).getConnection();
  }

  /**
   * Uses JDBC to do verification of tests.
   * 
   * @param conn connection
   * @param countSql sql that results in one row with one column that contains the actual count
   * @param expectedCount expected count
   * @return true if actual count equals expected count
   * @throws SQLException
   */
  public static boolean count(Connection conn, String countSql, int expectedCount) throws SQLException {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(countSql);
      if (rs.next()) {
        int count = rs.getInt(1);
        return count == expectedCount;
      } else {
        return false;
      }
    } finally {
      stmt.close();
    }
  }

  public static void executeUpdate(Connection conn, String sql) throws SQLException {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      stmt.executeUpdate(sql);
    } finally {
      stmt.close();
    }
  }

  public static SessionFactory getSessionFactory() {
    return cfg.buildSessionFactory();
  }

  public static void assertUserPersisted(Connection connection, String username, String password, boolean enabled)
      throws SQLException {
    assertTrue(TestUtil.count(connection, String.format(
        "select count(*) from USERS where USERNAME='%s' and PASSWORD='%s' and ENABLED=%d", username, password, //$NON-NLS-1$
        enabled ? 1 : 0), 1));
  }

  public static void assertRolePersisted(Connection connection, String name) throws SQLException {
    assertTrue(TestUtil.count(connection, String.format("select count(*) from AUTHORITIES where AUTHORITY='%s'", name), //$NON-NLS-1$
        1));
  }

  public static void assertRoleAssignmentPersisted(Connection connection, String username, String name)
      throws SQLException {
    assertTrue(TestUtil.count(connection, String.format(
        "select count(*) from GRANTED_AUTHORITIES where USERNAME='%s' and AUTHORITY='%s'", username, name), 1)); //$NON-NLS-1$
  }

  public static void assertUserRemoved(Connection connection, String username) throws SQLException {
    assertTrue(count(connection, String.format("select count(*) from USERS where USERNAME='%s'", username), 0)); //$NON-NLS-1$
  }

  public static void assertRoleAssignmentRemoved(Connection connection, String username, String name)
      throws SQLException {
    assertTrue(count(connection, String.format(
        "select count(*) from GRANTED_AUTHORITIES where USERNAME='%s' and AUTHORITY='%s'", username, name), 0)); //$NON-NLS-1$
  }

  public static void assertRoleRemoved(Connection connection, String name) throws SQLException {
    assertTrue(count(connection, String.format("select count(*) from AUTHORITIES where AUTHORITY='%s'", name), 0)); //$NON-NLS-1$
  }

  public static void createTestRole(Connection connection, String name, String description) throws SQLException {
    if (description != null) {
      TestUtil.executeUpdate(connection, String.format(
          "insert into AUTHORITIES (AUTHORITY, DESCRIPTION) values ('%s', '%s')", name, description)); //$NON-NLS-1$
    } else {
      TestUtil.executeUpdate(connection, String.format("insert into AUTHORITIES (AUTHORITY) values ('%s')", name)); //$NON-NLS-1$
    }
  }

  public static void createTestUser(Connection connection, String username, String password, boolean enabled,
      String description, String... roleNames) throws SQLException {
    // insert user
    if (description != null) {
      TestUtil.executeUpdate(connection, String.format(
          "insert into USERS (USERNAME, PASSWORD, ENABLED, DESCRIPTION) values ('%s', '%s', %d, '%s')", username, //$NON-NLS-1$
          password, enabled ? 1 : 0, description));
    } else {
      TestUtil.executeUpdate(connection, String.format(
          "insert into USERS (USERNAME, PASSWORD, ENABLED) values ('%s', '%s', %d)", username, password, enabled ? 1 //$NON-NLS-1$
              : 0));
    }

    // insert assigned roles
    for (String roleName : roleNames) {
      TestUtil.executeUpdate(connection, String.format(
          "insert into GRANTED_AUTHORITIES (USERNAME, AUTHORITY) values ('%s', '%s')", username, roleName)); //$NON-NLS-1$

    }
  }

}
