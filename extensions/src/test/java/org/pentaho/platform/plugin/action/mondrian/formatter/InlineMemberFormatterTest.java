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
