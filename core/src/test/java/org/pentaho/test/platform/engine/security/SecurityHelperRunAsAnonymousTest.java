/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
