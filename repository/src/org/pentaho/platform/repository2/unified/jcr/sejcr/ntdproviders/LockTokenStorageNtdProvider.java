package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import org.pentaho.platform.repository2.unified.jcr.sejcr.NodeTypeDefinitionProvider;

// Equivalent CND:
// [pho_nt:pentahoLockTokenStorage]
//     // reference to locked node is not strictly needed; only stored here in case admin needs to find the locked node to 
//     // clear its lock
//     - pho:lockedNodeRef (reference) ignore
//     // lock token string; this is added to session via session.addLockToken()
//     - pho:lockToken (string) ignore
public class LockTokenStorageNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public NodeTypeDefinition getNodeTypeDefinition(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName(PHO_NT + "pentahoLockTokenStorage"); //$NON-NLS-1$
    t.getPropertyDefinitionTemplates().add(getLockedNodeRefProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getLockTokenProperty(ntMgr, vFac));
    return t;
  }

  private PropertyDefinitionTemplate getLockTokenProperty(NodeTypeManager ntMgr, ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "lockToken"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setOnParentVersion(OnParentVersionAction.IGNORE);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getLockedNodeRefProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "lockedNodeRef"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.REFERENCE);
    t.setOnParentVersion(OnParentVersionAction.IGNORE);
    t.setMultiple(false);
    return t;
  }

}
