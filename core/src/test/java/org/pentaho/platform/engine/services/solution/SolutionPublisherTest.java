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

package org.pentaho.platform.engine.services.solution;

import java.util.Locale;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class SolutionPublisherTest extends BaseTest {

  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSolutionPublish() {
    startTest();

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel( getLoggingLevel() );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    String result = publisher.publish( session, getLoggingLevel() );
    assertEquals( Messages.getInstance().getString( "SolutionPublisher.USER_SOLUTION_REPOSITORY_UPDATED" ), result );
    finishTest();
  }

  public void testSolutionPublishI18N() {
    startTest();

    Locale tmpLocale = LocaleHelper.getThreadLocaleBase();

    // Try a different locale from the default
    String localeLanguage = "fr"; //$NON-NLS-1$
    String localeCountry = "FR"; //$NON-NLS-1$
    Locale[] locales = Locale.getAvailableLocales();
    if ( locales != null ) {
      for ( int i = 0; i < locales.length; i++ ) {
        if ( locales[i].getLanguage().equals( localeLanguage ) && locales[i].getCountry().equals( localeCountry ) ) {
          LocaleHelper.setThreadLocaleBase( locales[i] );
          break;
        }
      }
    }

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel( getLoggingLevel() );
    StandaloneSession session =
      new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) );
    String result = publisher.publish( session, getLoggingLevel() );
    assertEquals( Messages.getInstance().getString( "SolutionPublisher.USER_SOLUTION_REPOSITORY_UPDATED" ), result );

    // now set the locale back again
    LocaleHelper.setThreadLocaleBase( tmpLocale );
    finishTest();
  }
}
