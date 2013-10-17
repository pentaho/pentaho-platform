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

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.util.RowLevelSecurityHelper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This row level security helper resolves SESSION("VALUE") in addition to it's parent USER() and ROLE() resolvers.
 * 
 * Note that it's the responsibility of the script to quote the SESSION objects, for instance:
 * 
 * Session ------- UID=1234 TENANT=PENTAHO
 * 
 * EQUALS([NUMBIZCOL];SESSION("UID")) will return NCOL=1234
 * 
 * EQUALS([STRBIZCOL];"SESSION("TENANT")") will return SCOL=`PENTAHO`
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class SessionAwareRowLevelSecurityHelper extends RowLevelSecurityHelper {

  protected final Log logger = LogFactory.getLog( SessionAwareRowLevelSecurityHelper.class );

  @Override
  protected String expandFunctions( String formula, String user, List<String> roles ) {
    formula = super.expandFunctions( formula, user, roles );

    // "expand" any SESSION('var')

    IPentahoSession session = PentahoSessionHolder.getSession();

    Pattern p = Pattern.compile( "SESSION\\(\"(.*?)\"\\)" ); //$NON-NLS-1$
    Matcher m = p.matcher( formula );
    StringBuffer sb = new StringBuffer( formula.length() );
    while ( m.find() ) {
      String text = m.group( 1 );
      String value = null;
      if ( session.getAttribute( text ) != null ) {
        value = session.getAttribute( text ).toString();
      } else {
        logger.warn( Messages.getInstance().getString(
          "SessionAwareRowLevelSecurityHelper.WARN_0001_NULL_ATTRIBUTE", text, user ) ); //$NON-NLS-1$
        return "FALSE()"; //$NON-NLS-1$
      }
      // escape string if necessary (double quote quotes)
      m.appendReplacement( sb, Matcher.quoteReplacement( value.replaceAll( "\"", "\"\"" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    m.appendTail( sb );
    return sb.toString();
  }
}
