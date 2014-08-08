package org.pentaho.platform.api.metaverse;

/**
 * Created by gmoran on 8/6/14.
 *
 * The IRequiresNamespace interface is for classes and interfaces that require a metaverse namespace
 */
public interface IRequiresNamespace {

  public void setNamespace(INamespace namespace);

  public INamespace getNamespace();

}
