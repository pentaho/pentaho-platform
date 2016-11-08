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

package org.pentaho.platform.plugin.action.hql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Set;

public class HQLLookupRule extends HQLBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4633628885885427927L;

  /**
   * 
   */
  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) ) {
      // if( (outputs == null) || (outputs.size() == 0 ) ||
      // (outputs.size() > 1 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return null;
    }

    // Did we override the output name? // TODO Deprecation Warning
    String outputName = getInputStringValue( StandardSettings.OUTPUT_NAME );
    if ( ( outputName == null ) && outputs.contains( "query-result" ) ) { //$NON-NLS-1$ // Get the query-result node - This is the preferred method to use 
      outputName = "query-result"; //$NON-NLS-1$
    }

    if ( outputName == null ) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
      // TODO Deprecation Warning
    }
    return outputName;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( HQLLookupRule.class );
  }

  @Override
  public boolean init() {
    return true;
  }
}
