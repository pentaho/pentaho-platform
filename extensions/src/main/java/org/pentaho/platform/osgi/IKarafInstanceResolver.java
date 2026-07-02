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
 * Assigns an instance number and ports to a KarafInstance
 * <p/>
 * Created by nbaker on 3/20/16.
 */
public interface IKarafInstanceResolver {
  /**
   * Given the instance parameters, resolve all ports and cache folders
   *
   * @param instance
   * @throws KarafInstanceResolverException
   */
  void resolveInstance( KarafInstance instance ) throws KarafInstanceResolverException;

}
