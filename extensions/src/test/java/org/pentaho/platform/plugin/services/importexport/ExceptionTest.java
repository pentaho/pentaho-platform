/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class ExceptionTest extends TestCase {
  public void testExceptions() {
    // Nothing to test - padding stats
    new InitializationException();
    new InitializationException( "test" );
    new InitializationException( "test", new Exception() );
    new InitializationException( new Exception() );
  }
}
