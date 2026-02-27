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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.beans.PropertyNameFormatter;

/**
 * This formatter exists to make old-style action sequence inputs, outputs and resource names work with bean utils
 * which adheres to the Java bean spec. All action definition inputs outputs and resources should be named in camel
 * case and dash characters "-" should be avoided. This method will convert a dash-style arg name into camel case
 * and print a warning, or just return the original name if there are no dashes found.
 * 
 * @author aphillips
 * @param name
 *          argument name to convert, if needed.
 * @return camel case representation of name
 */
public class ActionSequenceCompatibilityFormatter implements PropertyNameFormatter {

  Log logger = LogFactory.getLog( ActionSequenceCompatibilityFormatter.class );

  public String format( String name ) {
    return compatibilityToCamelCase( name );
  }

  protected String compatibilityToCamelCase( String name ) {
    String[] parts = name.split( "-", 0 ); //$NON-NLS-1$
    if ( parts.length > 1 ) {
      String camelCaseName = ""; //$NON-NLS-1$
      for ( int i = 0; i < parts.length; i++ ) {
        if ( i > 0 ) {
          camelCaseName += StringUtils.capitalize( parts[i] );
        } else {
          camelCaseName += parts[i];
        }
      }
      logger.warn( Messages.getInstance().getString(
          "ActionSequenceCompatibilityFormatter.WARN_USING_IO_COMPATIBILITY_MODE", camelCaseName, name ) ); //$NON-NLS-1$
      return camelCaseName;
    }
    return name;
  }

}
