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

import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IProducesRuntimeOutputs;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class TestPojo5 implements IAcceptsRuntimeInputs, IProducesRuntimeOutputs {

  private String input1;
  private String input2;
  private IActionSequenceResource resource1;
  private Map<String, Object> outputs = new HashMap<String, Object>();

  public boolean execute() {
    outputs.put( "output1", input1 );
    outputs.put( "output2", input2 );
    return true;
  }

  public void setResources( Map<String, IActionSequenceResource> resources ) {
    resource1 = resources.get( "RESOURCE1" );
  }

  public void setInputs( Map<String, Object> inputs ) {
    input1 = (String) inputs.get( "INPUT1" );
    input2 = (String) inputs.get( "INPUT2" );
  }

  public Map<String, Object> getOutputs() {
    return outputs;
  }

}
