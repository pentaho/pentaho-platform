package org.pentaho.platform.repository2.unified.jcr.sejcr;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;

public interface NodeTypeDefinitionProvider {

  NodeTypeDefinition getNodeTypeDefinition(NodeTypeManager ntMgr, ValueFactory vFac) throws RepositoryException;

}
