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


package org.pentaho.tomcat.logvalve;

import java.io.CharArrayWriter;
import java.io.IOException;
import org.apache.catalina.valves.AccessLogValve;

/**
 * This class makes sure that the passwords visible in the tomcat server access logs are masked
 *
 * @author samhithavootkoor
 */
public class FilteredAccessLogValve extends AccessLogValve {

  @Override
  public void log( CharArrayWriter message ) {
    try ( CharArrayWriter caw = new CharArrayWriter() ) {
      // Mask the user password
      caw.write( message.toString().replaceAll( "j_password=[^&^ ]*", "j_password=***" ) );
      super.log( caw );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}
