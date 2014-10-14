package org.pentaho.platform.api.metaverse;

/**
 * This interface allows for multiple levels of namespacing entities within the metaverse.
 */
public interface INamespace {

  /**
   * The entity namespace
   *
   * @return the namespace id, represents the container for this element
   */
  public String getNamespaceId();

  /**
   * Get the namespace one level above the current entity namespace
   *
   * @return the INamespace of the entity one level above the current
   */
  public INamespace getParentNamespace();

  public INamespace getSiblingNamespace( String name, String type );

}

