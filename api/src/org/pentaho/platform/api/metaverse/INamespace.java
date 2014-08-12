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

  /**
   * get the name space for the current level entity
   *
   * @param child the string representation of hte current entity's contribution to the namespace path
   * @param type the type of the child
   * @return the namespace object for the entity represented by child
   */
  public INamespace getChildNamespace( String child, String type );

}

