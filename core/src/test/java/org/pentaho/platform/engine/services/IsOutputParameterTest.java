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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

import junit.framework.Assert;

@SuppressWarnings( { "all" } )
public class IsOutputParameterTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";
  private static final String xactionName   = "isOutputParameterTest.xaction";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  /**
   * Assert parameters with is-output-parameter=false don't appear in output
   * 
   * @throws XmlParseException
   */
  public void testIsOutputParameter() throws XmlParseException {
    startTest();

    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "services/" + xactionName );

    Document actionSequenceDocument = XmlDom4JHelper.getDocFromString( xactionStr, null );
    IActionSequence actionSequence =
        SequenceDefinition.ActionSequenceFactory( actionSequenceDocument, "", this, PentahoSystem //$NON-NLS-1$
            .getApplicationContext(), DEBUG );
    Map allParameters = actionSequence.getOutputDefinitions();
    Set<String> outParameters = new HashSet<String>();
    Set<String> nonOutParameters = new HashSet<String>();
    for ( Object key : allParameters.keySet() ) {
      IActionParameter param = (IActionParameter) allParameters.get( key );
      if ( param.isOutputParameter() ) {
        outParameters.add( param.getName() );
      } else {
        nonOutParameters.add( param.getName() );
      }
    }
    Assert.assertEquals( "expected 2 outputable parameters in xaction", 2, outParameters.size() );
    Assert.assertEquals( "expected 1 paramater with is-output-parameter=false", 1, nonOutParameters.size() );

    IRuntimeContext runtimeContext =
        solutionEngine.execute( xactionStr, xactionName, "simple output test", false, true, null, false, new HashMap(), //$NON-NLS-1$
            null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$
    IParameterManager paramManager = runtimeContext.getParameterManager();
    Assert.assertEquals( outParameters.size(), paramManager.getCurrentOutputNames().size() );
    for ( Object key : paramManager.getCurrentOutputNames() ) {
      Assert.assertTrue( "output parameter not found in definition", outParameters.contains( key ) );
      Assert.assertFalse( "non-output parameter in output", nonOutParameters.contains( key ) );
    }

    finishTest();

  }

}
