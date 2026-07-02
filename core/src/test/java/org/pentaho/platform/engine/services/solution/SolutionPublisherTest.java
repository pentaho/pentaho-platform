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
