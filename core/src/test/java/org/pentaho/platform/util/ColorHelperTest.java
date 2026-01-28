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


package org.pentaho.platform.util;

import junit.framework.TestCase;

import java.awt.Color;

public class ColorHelperTest extends TestCase {

  public ColorHelperTest( final String arg0 ) {
    super( arg0 );
  }

  public void testColorHelper() {
    Color color = ColorHelper.lookupColor( "thistle" ); //$NON-NLS-1$
    assertEquals( color, Color.decode( "#d8bfd8" ) ); //$NON-NLS-1$

    color = ColorHelper.lookupColor( "teal", Color.BLACK ); //$NON-NLS-1$
    assertEquals( color, Color.decode( "#008080" ) ); //$NON-NLS-1$
  }

  public void testUsingDefaultWhenNotFound() {
    assertEquals( ColorHelper.lookupColor( "noSuchColorInMap", Color.BLACK ), Color.BLACK );
  }

  public void testUsingNullString() {
    Color color = ColorHelper.lookupColor( null );
    assertEquals( color, null);
  }

  public void testUsingNullStringAnColor() {
    Color color = ColorHelper.lookupColor( null, Color.BLACK);
    assertEquals( color, Color.BLACK);
  }

}
