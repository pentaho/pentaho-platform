package org.pentaho.platform.api.metaverse;

/**
 * Created by mburgess on 8/12/14.
 */
public interface IMetaverseComponentDescriptor extends IIdentifiable, INamespace {

  public void setNamespace(INamespace namespace);

  public INamespace getNamespace();
}
