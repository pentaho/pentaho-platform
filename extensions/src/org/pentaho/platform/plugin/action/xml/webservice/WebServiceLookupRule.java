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

package org.pentaho.platform.plugin.action.xml.webservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.action.xml.xquery.XQueryLookupRule;

/**
 * Performs the action of processing a webservice call in an action by doing the same functionality as the XQuery action
 * (but not trying to retrieve the column types). <br/>
 * The reason for the modification stems from a problem in the encoding of the URL. Retrieving the columns types expects
 * an XML decoded URL. The XQuery processing expects an XML encoded URL. <br/>
 * 
 * @author dkincade
 */
public class WebServiceLookupRule extends XQueryLookupRule {

  private static final long serialVersionUID = -3785939302984708094L;

  /**
   * Returns the logger for this class
   */
  @Override
  public Log getLogger() {
    return LogFactory.getLog( this.getClass() );
  }

  /**
   * For web services, we don't need to retrieve the columns types during processing
   */
  @Override
  protected boolean retrieveColumnTypes() {
    return false;
  }
}
