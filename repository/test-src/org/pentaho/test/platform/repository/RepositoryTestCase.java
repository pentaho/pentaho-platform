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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.repository;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

@SuppressWarnings( "nls" )
public class RepositoryTestCase extends BaseTest {

  /*
   * @see TestCase#setUp()
   */

  private static IPentahoSession sess;

  private static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }

  }

  public void setUp() {
    super.setUp();
    sess = new StandaloneSession( Messages.getInstance().getString( "REPOSTEST.JUNIT_TEST_SESSION" ) ); //$NON-NLS-1$
    HibernateUtil.beginTransaction();
  }

  public IPentahoSession getPentahoSession() {
    return sess;
  }

  /*
   * @see TestCase#tearDown()
   */
  public void tearDown() {
    super.tearDown();
    try {
      HibernateUtil.commitTransaction();
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Constructor for RepositoryTestCase.
   * 
   * @param arg0
   */
  public RepositoryTestCase( String arg0 ) {
    super( arg0 );
  }

  public RepositoryTestCase() {
    super();
  }

}
