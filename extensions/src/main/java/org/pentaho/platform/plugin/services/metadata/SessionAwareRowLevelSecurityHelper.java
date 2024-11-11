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
