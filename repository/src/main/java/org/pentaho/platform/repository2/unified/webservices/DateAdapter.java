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


package org.pentaho.platform.repository2.unified.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

public class DateAdapter extends XmlAdapter<String, Date> {
  private static final String DF = "MM/dd/yyyy HH:mm:ss";
  private static final Log log = LogFactory.getLog( DateAdapter.class );

  @Override
  public Date unmarshal( String v ) throws Exception {
    return new Date( Long.valueOf( v ) );
  }

  @Override
  public String marshal( Date v ) throws Exception {
    return String.valueOf( v.getTime() );
  }
}
