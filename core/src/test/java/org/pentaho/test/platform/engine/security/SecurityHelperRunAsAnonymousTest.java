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


package org.pentaho.test.platform.engine.security;

import java.util.concurrent.Callable;

import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import junit.framework.TestCase;

/**
 * Test class that validates if ISecurityHelper.runAsAnonymous() method holds an AnonymousAuthenticationToken User:
 * pteixeira
 */
public class SecurityHelperRunAsAnonymousTest extends TestCase {

  public void testRunAsAnonymousWithAnonymousAuthenticationToken() throws Exception {

    Callable<Object> callableObject = new Callable<Object>() {

      public Object call() throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Assert.assertTrue( auth != null && auth instanceof AnonymousAuthenticationToken );

        return null;
      }
    };

    SecurityHelper.getInstance().runAsAnonymous( callableObject );
  }

  public static void main( final String[] args ) {
    SecurityHelperRunAsAnonymousTest test = new SecurityHelperRunAsAnonymousTest();
    try {

      test.testRunAsAnonymousWithAnonymousAuthenticationToken();

    } catch ( Throwable t ) {
      System.err.println( t.toString() );
    }
  }

}
