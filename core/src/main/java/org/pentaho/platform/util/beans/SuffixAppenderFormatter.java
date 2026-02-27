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


package org.pentaho.platform.util.beans;

public class SuffixAppenderFormatter implements PropertyNameFormatter {

  private String suffixToAppend;

  public SuffixAppenderFormatter( String suffixToAppend ) {
    this.suffixToAppend = suffixToAppend;
  }

  public String format( String name ) {
    return name + suffixToAppend;
  }

}
