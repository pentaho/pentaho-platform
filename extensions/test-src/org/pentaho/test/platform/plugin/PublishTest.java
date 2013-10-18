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

package org.pentaho.test.platform.plugin;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionPublisher;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.util.Locale;

@SuppressWarnings( "nls" )
public class PublishTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSolutionPublish() {
    startTest();

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel( getLoggingLevel() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    publisher.publish( session, getLoggingLevel() );
    assertTrue( publisher != null );
    finishTest();
  }

  public void testSolutionPublishI18N() {
    startTest();

    Locale tmpLocale = LocaleHelper.getLocale();
    // Try a different locale from the default
    String localeLanguage = "fr"; //$NON-NLS-1$
    String localeCountry = "FR"; //$NON-NLS-1$
    if ( localeLanguage != null && !"".equals( localeLanguage ) && localeCountry != null && !"".equals( localeCountry ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      Locale[] locales = Locale.getAvailableLocales();
      if ( locales != null ) {
        for ( int i = 0; i < locales.length; i++ ) {
          if ( locales[i].getLanguage().equals( localeLanguage ) && locales[i].getCountry().equals( localeCountry ) ) {
            LocaleHelper.setLocale( locales[i] );
            break;
          }
        }
      }
    }

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel( getLoggingLevel() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    publisher.publish( session, getLoggingLevel() );
    assertTrue( publisher != null );
    // now set the locale back again
    LocaleHelper.setLocale( tmpLocale );
    finishTest();
  }

  /*
   * public void testWorkflowPublish() { startTest(); SharkPublisher publisher = new SharkPublisher();
   * publisher.setLoggingLevel(getLoggingLevel()); StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * publisher.setLoggingLevel(getLoggingLevel()); publisher.publish(session); assertTrue(publisher != null);
   * finishTest(); }
   */
  public static void main( String[] args ) {
    PublishTest test = new PublishTest();
    test.setUp();
    try {
      test.testSolutionPublish();
      test.testSolutionPublishI18N();
      // test.testWorkflowPublish();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
