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

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * Utility class for converting between XMLGregorianCalendar and java.util.Date
 * 
 * @author tkafalas
 */
public class XmlGregorianCalendarConverter {

  /**
   * Needed to create XMLGregorianCalendar instances
   */
  private static DatatypeFactory df = null;
  static {
    try {
      df = DatatypeFactory.newInstance();
    } catch ( DatatypeConfigurationException e ) {
      throw new IllegalStateException( "Exception while obtaining DatatypeFactory instance", e );
    }
  }

  /**
   * Converts a java.util.Date into an instance of XMLGregorianCalendar
   * 
   * @param date
   *          Instance of java.util.Date or a null reference
   * @return XMLGregorianCalendar instance whose value is based upon the value in the date parameter. If the date
   *         parameter is null then this method will simply return null.
   */
  public static XMLGregorianCalendar asXMLGregorianCalendar( java.util.Date date ) {
    if ( date == null ) {
      return null;
    } else {
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTimeInMillis( date.getTime() );
      return df.newXMLGregorianCalendar( gc );
    }
  }

  /**
   * Converts an XMLGregorianCalendar to an instance of java.util.Date
   * 
   * @param xgc
   *          Instance of XMLGregorianCalendar or a null reference
   * @return java.util.Date instance whose value is based upon the value in the xgc parameter. If the xgc parameter is
   *         null then this method will simply return null.
   */
  public static java.util.Date asDate( XMLGregorianCalendar xgc ) {
    if ( xgc == null ) {
      return null;
    } else {
      return xgc.toGregorianCalendar().getTime();
    }
  }
}
