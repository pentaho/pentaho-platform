/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
