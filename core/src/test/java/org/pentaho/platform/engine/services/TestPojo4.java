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

public class TestPojo4 {

  protected String output1;

  public String getOutput1() {
    return output1;
  }

  public void setInput1( String input1 ) {
    output1 = input1 + input1;
  }

}
