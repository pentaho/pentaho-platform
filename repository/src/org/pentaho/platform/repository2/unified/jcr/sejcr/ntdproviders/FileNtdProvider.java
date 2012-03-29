package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO;

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
// [pho_nt:pentahoFile] > nt:file, pho_nt:pentahoHierarchyNode
//     // strangely, nt:file's don't have lastModified; instead this attribute is on nt:resource; however not all pentaho
//     // files have an nt:resource; so we put the lastModified date on the nt:file node itself
//     - pho:lastModified (date) mandatory ignore
//     - pho:contentType (string) mandatory copy
//     - pho:fileSize (long) copy
public class FileNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public NodeTypeDefinition getNodeTypeDefinition(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName(PHO_NT + "pentahoFile"); //$NON-NLS-1$
    t.setDeclaredSuperTypeNames(new String[] { NT + "file", PHO_NT + "pentahoHierarchyNode" }); //$NON-NLS-1$ //$NON-NLS-2$
    t.getPropertyDefinitionTemplates().add(getLastModifiedProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getContentTypeProperty(ntMgr, vFac));
    t.getPropertyDefinitionTemplates().add(getFileSizeProperty(ntMgr, vFac));
    return t;
  }

  private PropertyDefinitionTemplate getLastModifiedProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "lastModified"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.DATE);
    t.setMandatory(true);
    t.setOnParentVersion(OnParentVersionAction.IGNORE);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getContentTypeProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "contentType"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.STRING);
    t.setMandatory(true);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }

  private PropertyDefinitionTemplate getFileSizeProperty(final NodeTypeManager ntMgr, final ValueFactory vFac)
      throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName(PHO + "fileSize"); //$NON-NLS-1$
    t.setRequiredType(PropertyType.LONG);
    t.setOnParentVersion(OnParentVersionAction.COPY);
    t.setMultiple(false);
    return t;
  }

}
