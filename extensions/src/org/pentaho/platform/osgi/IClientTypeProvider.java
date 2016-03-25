package org.pentaho.platform.osgi;

/**
 * Implementations return a PDI client-type identifier (spoon,pan,carte,kitchen) or "default" if not one of those.
 * <p/>
 * Created by nbaker on 3/24/16.
 */
public interface IClientTypeProvider {
  /**
   * Get the PDI client type associated with this running instance
   *
   * @return a PDI client-type identifier (spoon,pan,carte,kitchen) or "default"
   */
  String getClientType();
}
