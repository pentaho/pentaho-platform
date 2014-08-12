package org.pentaho.platform.api.metaverse;

/**
 * Created by mburgess on 8/12/14.
 */
public interface IMetaverseComponentDescriptor extends IIdentifiable {

  public void setNamespace(INamespace namespace);

  public INamespace getNamespace();
}
