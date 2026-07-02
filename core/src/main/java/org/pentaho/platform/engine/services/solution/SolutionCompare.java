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


package org.pentaho.platform.engine.services.solution;

import org.pentaho.platform.util.StringUtil;

import java.util.Comparator;

/**
 * Class that compares solution paths used for sorting. It takes into account the path depth and sorts the parent
 * directories before than the children.
 * 
 * @author dmoran
 * 
 */
public class SolutionCompare implements Comparator {

  public int compare( final Object o1, final Object o2 ) {
    String str1 = o1.toString();
    String str2 = o2.toString();

    String[] str1Array = StringUtil.tokenStringToArray( str1, "/" ); //$NON-NLS-1$
    String[] str2Array = StringUtil.tokenStringToArray( str2, "/" ); //$NON-NLS-1$

    // If the solution paths are at the same depth, a straight compare will do
    if ( str1Array.length == str2Array.length ) {
      return ( str1.compareToIgnoreCase( str2 ) );
    }

    // if the solution paths are un-even, just compare the path and not the Action Sequence
    int count = Math.min( str1Array.length, str2Array.length );
    for ( int i = 0; i < count - 1; ++i ) {
      int compare = str1Array[i].compareToIgnoreCase( str2Array[i] );
      if ( compare != 0 ) {
        return ( compare );
      }
    }

    // The shortest one comes first
    return ( str1Array.length - str2Array.length );
  }
}
