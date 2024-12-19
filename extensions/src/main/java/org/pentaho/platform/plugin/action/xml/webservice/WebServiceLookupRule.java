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
