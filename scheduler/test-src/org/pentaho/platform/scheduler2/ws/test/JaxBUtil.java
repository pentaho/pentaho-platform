/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.ws.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

public class JaxBUtil {

  /**
   * Returns an object that should match the <code>in</code> object value-wise, but is actually created by JAXB. JAXB
   * will marshall the <code>in</code> object to XML, print it to stdout, and hydrate an object of the same type from
   * the XML.
   * 
   * @param <T>
   *          the type to marshall and unmarshall to and from XML
   * @param in
   *          the object to marshall to XML
   * @param classes
   *          additional classes referred to by the <code>in</code> object
   * @return a JAXB-created object that should be value-wise equal to the <code>in</code> object
   * @throws JAXBException
   *           if something goes wrong
   */
  @SuppressWarnings( "unchecked" )
  public static <T> T outin( T in, Class<?>... classes ) throws JAXBException {
    //
    // marshal and unmarshall back into a new object
    //
    JAXBContext jc = JAXBContext.newInstance( classes );
    Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    m.marshal( in, bos );
    System.out.println( new String( bos.toByteArray() ) );
    Unmarshaller u = jc.createUnmarshaller();
    T unmarshalled = (T) u.unmarshal( new ByteArrayInputStream( bos.toByteArray() ) );
    return unmarshalled;
  }

  @SuppressWarnings( "unchecked" )
  /**
   * @param wrapInRootElement <code>false</code> means the class is not required to be annotated with @XmlRootElement
   */
  public static <T> void out( boolean wrapInRootElement, T in, Class<?>... classes ) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance( classes );
    Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if ( wrapInRootElement ) {
      m.marshal( new JAXBElement( new QName( in.getClass().getSimpleName() ), in.getClass(), in ), bos );
    }
    System.out.println( new String( bos.toByteArray() ) );
  }
}
