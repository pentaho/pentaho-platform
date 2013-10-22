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

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.awt.Color;

public class ColorHelperTest extends TestCase {

  public ColorHelperTest( final String arg0 ) {
    super( arg0 );
  }

  public void testColorHelper() {
    Color color = ColorHelper.lookupColor( "thistle" ); //$NON-NLS-1$
    Assert.assertEquals( color, Color.decode( "#d8bfd8" ) ); //$NON-NLS-1$
    color = ColorHelper.lookupColor( "teal", Color.BLACK ); //$NON-NLS-1$
    Assert.assertEquals( color, Color.decode( "#008080" ) ); //$NON-NLS-1$
    color = ColorHelper.lookupColor( "noSuchColorInMap", Color.BLACK ); //$NON-NLS-1$
    Assert.assertEquals( color, Color.BLACK );
  }

}
