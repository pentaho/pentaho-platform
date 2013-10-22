/*!
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
 */

package org.pentaho.test.platform.engine.security;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;

import java.util.concurrent.Callable;

/**
 * Test class that validates if ISecurityHelper.runAsAnonymous() method holds an AnonymousAuthenticationToken User:
 * pteixeira
 */
public class SecurityHelperRunAsAnonymousTest extends TestCase {

  public void testRunAsAnonymousWithAnonymousAuthenticationToken() throws Exception {

    Callable<Object> callableObject = new Callable<Object>() {

      public Object call() throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Assert.assertTrue( auth != null && auth instanceof AnonymousAuthenticationToken );

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
