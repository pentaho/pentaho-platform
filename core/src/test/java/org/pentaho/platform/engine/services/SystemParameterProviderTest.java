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


package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( { "all" } )
public class SystemParameterProviderTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSystemParameter() {
    startTest();
    SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
    assertEquals( "Output is not correct", "server.log", provider.getStringParameter(
        "pentaho.xml{pentaho-system/log-file}", null ) );
    finishTest();
  }

  public void testCustomParameter() {
    startTest();
    IPentahoSession session = new StandaloneSession( "admin" );
    CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();
    provider.setSession( session );

    assertEquals( "Output is not correct", "value1", provider.getStringParameter(
        "settings-{$user}.xml{personal-settings/setting1}", null ) );
    finishTest();
  }
}
