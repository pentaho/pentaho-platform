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

package org.pentaho.platform.repository2.unified.webservices;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeRepositoryFileDataAdapterTest {

  private void testMarshalUnmarshalDate( Locale locale, TimeZone timeZone ) throws Exception {
    final Locale defaultLocale = Locale.getDefault();
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    final String DATE_PROPERTY = "date"; //$NON-NLS-1$

    NodeRepositoryFileDataAdapter adapter = new NodeRepositoryFileDataAdapter();
    Date date = new Date();
    DataNode node = new DataNode( "" ); //$NON-NLS-1$
    node.setProperty( DATE_PROPERTY, date );
    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    NodeRepositoryFileData result;

    // Convert using the provided locale
    try {
      Locale.setDefault( locale );
      TimeZone.setDefault( timeZone );
      NodeRepositoryFileDataDto dto = adapter.marshal( data );
      result = adapter.unmarshal( dto );
    } finally {
      Locale.setDefault( defaultLocale );
      TimeZone.setDefault( defaultTimeZone );
    }

    DataProperty property = result.getNode().getProperty( DATE_PROPERTY );
    assertNotNull( property );
    assertEquals( date, property.getDate() );
  }

  @Test
  public void testMarshalUnmarshalDate_locale_us() throws Exception {
    testMarshalUnmarshalDate( Locale.US, TimeZone.getTimeZone( "EST" ) ); //$NON-NLS-1$
    testMarshalUnmarshalDate( Locale.US, TimeZone.getTimeZone( "CST" ) ); //$NON-NLS-1$
    testMarshalUnmarshalDate( Locale.US, TimeZone.getTimeZone( "PST" ) ); //$NON-NLS-1$
  }

  @Test
  public void testMarshalUnmarshalDate_locale_germany() throws Exception {
    testMarshalUnmarshalDate( Locale.GERMANY, TimeZone.getTimeZone( "CET" ) ); //$NON-NLS-1$
  }

  @Test
  public void testMarshalUnmarshalDate_locale_japan() throws Exception {
    testMarshalUnmarshalDate( Locale.JAPAN, TimeZone.getTimeZone( "JST" ) );
  }
}
