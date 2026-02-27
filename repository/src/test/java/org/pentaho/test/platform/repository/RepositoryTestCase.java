/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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

  private static final String SOLUTION_PATH = "src/test/resources/solution";
  private static final String ALT_SOLUTION_PATH = "src/test/resources/solution";
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
