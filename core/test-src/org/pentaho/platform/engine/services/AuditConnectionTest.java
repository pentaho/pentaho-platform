/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.engine.services;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.services.audit.AuditConnection;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class AuditConnectionTest extends BaseTest {
	private static String SOLUTION_PATH = "test-src/solution";
	
	public String getSolutionPath() {
       return SOLUTION_PATH;  
 	}

	public void testAuditConnection() {
    startTest();
    AuditConnection auditConnection = new AuditConnection();
    auditConnection.setUseNewDatasourceService(true); // make sure we get a datasource from the object factory
    auditConnection.initialize();
    try {
      Connection connection = auditConnection.getAuditConnection();
      System.out.println("Audit Connection Is  " + connection); //$NON-NLS-1$  
      
      DataSource datasource = auditConnection.getAuditDatasource();
      System.out.println("Datasource Is  " + datasource); //$NON-NLS-1$      
    }
    catch(Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    } finally {
      finishTest();
    }
  }

  public void testAuditConnectionNoConfigFile() {
    startTest();
    SOLUTION_PATH = ""; // We want to test what happens when the config file can't be found
    AuditConnection auditConnection = new AuditConnection();
    auditConnection.setUseNewDatasourceService(true); // make sure we get a datasource from the object factory
    auditConnection.initialize();
    try {
      Connection connection = auditConnection.getAuditConnection();
      System.out.println("Audit Connection Is  " + connection); //$NON-NLS-1$  
      
      DataSource datasource = auditConnection.getAuditDatasource();
      System.out.println("Datasource Is  " + datasource); //$NON-NLS-1$      
    }
    catch(Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    } finally {
      finishTest();
    }
  }
  
  public void testAuditConnection_cannot_establish_connection() {
    startTest();
    try {
      // Load mock object factory with mock datasource service that produces null datasources
      StandaloneSession session = new StandaloneSession();
      StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
      factory.init("test-src/solution/system/pentahoObjects.datasourceservice.null.spring.xml", null );
      PentahoSystem.setObjectFactory(factory);
  
      AuditConnection auditConnection = new AuditConnection();
      auditConnection.setUseNewDatasourceService(true); // make sure we get a datasource from the object factory
      auditConnection.initialize();
      auditConnection.getAuditConnection();
      fail("Expected exception when no audit connection could be established");
    } catch (SQLException ex) {
      ex.printStackTrace();
      assertTrue("Expected AUDSQLENT.ERROR_0001", ex.getMessage().contains("AUDSQLENT.ERROR_0001"));
    } finally {
      finishTest();
    }
  }
  
  public static void main(String[] args) {
    AuditConnectionTest test = new AuditConnectionTest();
    test.setUp();
    test.testAuditConnection();
    test.testAuditConnectionNoConfigFile();
    try {

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
