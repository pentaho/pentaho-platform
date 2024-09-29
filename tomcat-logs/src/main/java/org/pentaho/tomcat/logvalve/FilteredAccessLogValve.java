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
 * Copyright (c) 2022-2024 Hitachi Vantara. All rights reserved.
 *
 */

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
