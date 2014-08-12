package org.pentaho.platform.api.metaverse;

/**
 * IMetaverseComponentDescriptor is a contract for an object that can describe a metaverse component. For example, it
 * could contain name, type, and namespace information for a particular document. The metadata about the component and
 * the component itself are separated to allow for maximum flexibility.
 */
public interface IMetaverseComponentDescriptor extends IIdentifiable, INamespace {

  public void setNamespace( INamespace namespace );

  public INamespace getNamespace();
}
