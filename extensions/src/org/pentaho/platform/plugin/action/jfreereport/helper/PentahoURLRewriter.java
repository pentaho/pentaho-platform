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

package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriteException;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriter;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Creation-Date: 05.07.2007, 19:16:13
 * 
 * @author Thomas Morgner
 */
public class PentahoURLRewriter implements URLRewriter {
  private String pattern;

  public PentahoURLRewriter( final String pattern ) {
    this.pattern = pattern;
  }

  public String rewrite( final ContentEntity contentEntry, final ContentEntity dataEntity ) throws URLRewriteException {
    try {
      final ArrayList<String> entityNames = new ArrayList<String>();
      entityNames.add( dataEntity.getName() );

      ContentLocation location = dataEntity.getParent();
      while ( location != null ) {
        entityNames.add( location.getName() );
        location = location.getParent();
      }

      final ArrayList<String> contentNames = new ArrayList<String>();
      location = dataEntity.getRepository().getRoot();

      while ( location != null ) {
        contentNames.add( location.getName() );
        location = location.getParent();
      }

      // now remove all path elements that are equal ..
      while ( ( contentNames.isEmpty() == false ) && ( entityNames.isEmpty() == false ) ) {
        final String lastEntity = (String) entityNames.get( entityNames.size() - 1 );
        final String lastContent = (String) contentNames.get( contentNames.size() - 1 );
        if ( lastContent.equals( lastEntity ) == false ) {
          break;
        }
        entityNames.remove( entityNames.size() - 1 );
        contentNames.remove( contentNames.size() - 1 );
      }

      final StringBuffer b = new StringBuffer();
      for ( int i = entityNames.size() - 1; i >= 0; i-- ) {
        final String name = (String) entityNames.get( i );
        b.append( name );
        if ( i != 0 ) {
          b.append( "/" ); //$NON-NLS-1$
        }
      }

      if ( pattern == null ) {
        return b.toString();
      }

      return MessageFormat.format( pattern, new Object[] { b.toString() } );
    } catch ( ContentIOException cioe ) {
      throw new URLRewriteException();
    }

  }
}
