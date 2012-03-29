package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.MIX;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_MIX;

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
// [pho_mix:pentahoVersionable] > mix:simpleVersionable
//     mixin
//     - pho:versionAuthor (string) copy
//     - pho:versionMessage (string) copy
//     // since JCR requires a checkout-checkin for setPolicy, mark it as such so it can be filtered later
//     - pho:aclChangeOnly (boolean) copy

public class VersionableNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public NodeTypeDefinition getNodeTypeDefinition(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName(PHO_MIX + "pentahoVersionable"); //$NON-NLS-1$
    t.setMixin(true);
    t.setDeclaredSuperTypeNames(new String[] { MIX + "simpleVersionable" }); //$NON-NLS-1$
    t.getPropertyDefinitionTemplates().add(getAuthorProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getMessageProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getAclChangeOnlyProperty(ntMgr, vFac));
    return t;
  }

  private PropertyDefinitionTemplate getAuthorProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "versionAuthor"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getMessageProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "versionMessage"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getAclChangeOnlyProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "aclChangeOnly"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.BOOLEAN);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }
}
