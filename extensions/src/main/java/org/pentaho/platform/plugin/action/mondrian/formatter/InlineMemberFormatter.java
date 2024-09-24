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
