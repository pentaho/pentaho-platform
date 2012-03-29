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
// [pho_nt:localizedString]
//     - pho:rootLocale (string) copy
//     - * (string) copy
public class LocalizedStringNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public NodeTypeDefinition getNodeTypeDefinition(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName(PHO_NT + "localizedString"); //$NON-NLS-1$
    t.getPropertyDefinitionTemplates().add(getRootLocaleProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getWildcardStringProperty(ntMgr, vFac));
    return t;
  }

  private PropertyDefinitionTemplate getRootLocaleProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "rootLocale"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getWildcardStringProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName("*"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    return t;
  }

}
