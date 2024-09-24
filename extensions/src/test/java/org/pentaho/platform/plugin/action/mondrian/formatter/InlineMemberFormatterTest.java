/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.formatter;

import mondrian.olap.Annotation;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Property;
import org.junit.Test;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Used to format a member from a format string.
 *
 * Created by bgroves on 3/14/16.
 */
public class InlineMemberFormatterTest {

  private static String FORMAT = "yyyy-mm-dd";

  @Test
  public void testFormatMember() {
    Level level = mock( Level.class );

    Member member = mock( Member.class );
    when( member.getLevel() ).thenReturn( level );
    when( member.getPropertyValue( Property.KEY.getName() ) ).thenReturn( ( new GregorianCalendar( 1997, 0, 2 ).getTime() ) );

    InlineMemberFormatter formatter = new InlineMemberFormatter();

    try {
      formatter.formatMember( member );
      fail();
    } catch ( IllegalStateException execption ) {
      //pass
    }

    Annotation annotation = mock( Annotation.class );
    when( annotation.getValue() ).thenReturn( FORMAT );

    Map<String, Annotation> annotationMap = new HashMap<>();
    annotationMap.put( InlineMemberFormatter.FORMAT_STRING, annotation );

    when( level.getAnnotationMap() ).thenReturn( annotationMap );

    String formatterMember = formatter.formatMember( member );
    System.out.println( formatterMember );
    assertEquals( "1997-01-02", formatterMember );
  }
}
