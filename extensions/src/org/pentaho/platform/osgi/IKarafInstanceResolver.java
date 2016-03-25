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
