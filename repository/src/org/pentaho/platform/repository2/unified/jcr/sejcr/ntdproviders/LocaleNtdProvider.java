package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import org.pentaho.platform.repository2.unified.jcr.sejcr.NodeTypeDefinitionProvider;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.*;
import javax.jcr.version.OnParentVersionAction;

import java.util.Locale;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

// Equivalent CND:
// [pho_nt:locale]
//     - rootLocale (string) copy
//     - en (string) copy
//     - es (string) copy,
//     - ...etc
public class LocaleNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public NodeTypeDefinition getNodeTypeDefinition(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName(PHO_NT + "locale"); //$NON-NLS-1$

    // create node definition for default locale
    t.getNodeDefinitionTemplates().add(getLocaleNode(ntMgr, "default"));

    // create node definitions for each available locale
    for(Locale locale : Locale.getAvailableLocales()){
      t.getNodeDefinitionTemplates().add(getLocaleNode(ntMgr, locale.toString()));
    }

    return t;
  }

  private NodeDefinitionTemplate getLocaleNode(final NodeTypeManager ntMgr, final String localeName) throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName(localeName); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames(new String[] {  NT + "unstructured" }); //$NON-NLS-1$
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setSameNameSiblings(false);
    return t;
  }
}
