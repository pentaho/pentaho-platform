package org.pentaho.platform.security.policy.rolebased;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class JcrRoleAuthorizationPolicyUtils {

  protected boolean isInNamespace(final String namespace, final String namespacedAction) {
    if (namespace == null) {
      return true;
    }
    final String DOT = "."; //$NON-NLS-1$
    int lastIndexOfDot = namespacedAction.lastIndexOf(DOT);
    // there is no dot; therefore there is no namespace; return true if namespace is null or empty
    if (lastIndexOfDot == -1) {
      return !StringUtils.hasText(namespace);
    } else {
      return namespace.equals(namespacedAction.substring(0, lastIndexOfDot));
    }
  }

  public static void internalSetBindings(final PentahoJcrConstants pentahoJcrConstants, final Node runtimeRolesFolderNode,
      final String runtimeRoleNodeName, final List<String> logicalRoleNames) throws RepositoryException {
    Node runtimeRoleNode = null;
    if (runtimeRolesFolderNode.hasNode(runtimeRoleNodeName)) {
      runtimeRoleNode = runtimeRolesFolderNode.getNode(runtimeRoleNodeName);
    } else {
      runtimeRoleNode = runtimeRolesFolderNode.addNode(runtimeRoleNodeName);
    }
    // clear all existing properties
    if (runtimeRoleNode.hasProperty(pentahoJcrConstants.getPHO_BOUNDROLES())) {
      runtimeRoleNode.getProperty(pentahoJcrConstants.getPHO_BOUNDROLES()).remove();
    }
    runtimeRoleNode.setProperty(pentahoJcrConstants.getPHO_BOUNDROLES(), logicalRoleNames.toArray(new String[0]));
  }

}
