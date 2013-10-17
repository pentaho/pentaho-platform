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
