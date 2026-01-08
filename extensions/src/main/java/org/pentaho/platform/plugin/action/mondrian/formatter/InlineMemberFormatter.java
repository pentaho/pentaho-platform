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

import java.util.Locale;

import mondrian.olap.Annotation;
import mondrian.olap.Member;
import mondrian.olap.Property;
import mondrian.spi.MemberFormatter;
import mondrian.util.Format;

/**
 * Used to format the member given a format string.
 *
 * @author Benny
 */
public class InlineMemberFormatter implements MemberFormatter {

  public static String FORMAT_STRING = "InlineMemberFormatString";

  @Override
  public String formatMember( Member member ) {

    Annotation annot = member.getLevel().getAnnotationMap().get( FORMAT_STRING );
    if ( annot == null ) {
      throw new IllegalStateException( "Missing InlineMemberFormatString on level "
        + member.getLevel().getUniqueName() );
    }
    Object key = member.getPropertyValue( Property.KEY.getName() );

    Format format = Format.get( annot.getValue().toString(), Locale.getDefault() );
    return format.format( key );
  }

}
