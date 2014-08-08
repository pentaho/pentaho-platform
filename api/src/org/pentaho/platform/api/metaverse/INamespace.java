package org.pentaho.platform.api.metaverse;

/**
 * Created by gmoran on 8/7/14.
 */

/**
 * This interface allows for multiple levels of namespacing entities within the metaverse.
 *
 *
 */
public interface INamespace {

  /**
   * The entity namespace
   *
   * @return the namespace id, represents the container for this element
   */
  public String getNamespaceId();

  /**
   *
    * @return the INamespace of the entity one level above the current
   */
  public INamespace getParentNamespace();

}

