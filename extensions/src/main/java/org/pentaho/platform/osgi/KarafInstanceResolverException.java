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


package org.pentaho.platform.osgi;

/**
 * Simple Exception for errors when resoving a KarafInstance
 *
 * Created by nbaker on 3/24/16.
 */
public class KarafInstanceResolverException extends Exception {
  private static final long serialVersionUID = -4666903929040782424L;

  public KarafInstanceResolverException( String s ) {
    super( s );
  }

}
