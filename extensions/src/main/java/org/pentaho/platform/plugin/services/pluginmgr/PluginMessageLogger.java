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


package org.pentaho.platform.plugin.services.pluginmgr;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.List;

public class PluginMessageLogger {
  private static ThreadLocal<List<String>> messages = new ThreadLocal<List<String>>() {
    protected List<String> initialValue() {
      return new ArrayList<String>();
    }
  };

  public static void clear() {
    messages.get().clear();
  }

  public static void add( String message ) {
    messages.get().add( message );
  }

  public static List<String> getAll() {
    return messages.get();
  }

  public static int count( final String messagePrefix ) {
    return CollectionUtils.countMatches( messages.get(), new Predicate() {

      public boolean evaluate( Object object ) {
        return ( (String) object ).startsWith( messagePrefix );
      }

    } );
  }

  public static String prettyPrint() {
    StringBuilder builder = new StringBuilder();
    for ( String msg : messages.get() ) {
      builder.append( msg );
      builder.append( '\n' );
    }
    return builder.toString();
  }

}
