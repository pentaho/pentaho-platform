package org.pentaho.platform.osgi;

/**
 * Simple Exception for errors when resoving a KarafInstance
 *
 * Created by nbaker on 3/24/16.
 */
public class KarafInstanceResolverException extends Exception {
  public KarafInstanceResolverException( String s ) {
    super( s );
  }

}
