package org.apache.jackrabbit.core;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.core.nodetype.NodeDefinitionImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeImpl;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.state.ChildNodeEntry;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.PentahoAccessControlEditor;


/**
 * Certain operations (e.g. access control reading and writing) do not use the standard JCR Item/Node/Property API, even 
 * though Jackrabbit implements ACLs as properties. This is because Jackrabbit implements them as "protected 
 * properties." The specification requires the reading and writing of protected properties to go through a 
 * "feature-specific API" (e.g. AccessControlManager.setPolicy). See section 16.3.12 in JSR-283 "Interaction with 
 * Protected Properties." This is the private API upon which calls like {@code AccessControlManager.setPolicy} are 
 * implemented.
 * 
 * <p>
 * This class was required since the original ProtectedItemModifier's constructor tested the implementation class of the
 * subclass (just as this one does). This class must be in the org.apache.jackrabbit.core package since it makes calls 
 * to methods that are default-scoped.
 * </p>
 * 
 * <p>ACLs are not versioned and do not apply to locks, so all references to checkSetProperty() have been removed.<p>
 * 
 * <p>
 * Original Javadoc:
 * </p>
 * <p>
 * <code>SecurityItemModifier</code>: An abstract helper class to allow classes
 * of the security API residing outside of the core package to modify and remove
 * protected items for security. The protected item definitions are required in
 * order not to have security relevant content being changed through common
 * item operations but forcing the usage of the security API. The latter asserts
 * that implementation specific constraints are not violated.
 * </p>
 */
public abstract class PentahoProtectedItemModifier {

  private static final int DEFAULT_PERM_CHECK = -1;

  private final int permission;

  protected PentahoProtectedItemModifier() {
    this(DEFAULT_PERM_CHECK);
  }

  protected PentahoProtectedItemModifier(int permission) {
    Class cl = getClass();
    if (!cl.equals(PentahoAccessControlEditor.class)) {
      throw new IllegalArgumentException(
          "Only PentahoAccessControlEditor may extend from the PentahoProtectedItemModifier");
    }
    this.permission = permission;
  }

  protected NodeImpl addNode(NodeImpl parentImpl, Name name, Name ntName) throws RepositoryException {
    checkPermission(parentImpl, name, getPermission(true, false));

    NodeTypeImpl nodeType = parentImpl.session.getNodeTypeManager().getNodeType(ntName);
    NodeDefinitionImpl def = parentImpl.getApplicableChildNodeDefinition(name, ntName);

    // check for name collisions
    NodeState thisState = (NodeState) parentImpl.getItemState();
    ChildNodeEntry cne = thisState.getChildNodeEntry(name, 1);
    if (cne != null) {
      // there's already a child node entry with that name;
      // check same-name sibling setting of new node
      if (!def.allowsSameNameSiblings()) {
        throw new ItemExistsException();
      }
      // check same-name sibling setting of existing node
      NodeId newId = cne.getId();
      NodeImpl n = (NodeImpl) parentImpl.session.getItemManager().getItem(newId);
      if (!n.getDefinition().allowsSameNameSiblings()) {
        throw new ItemExistsException();
      }
    }

    return parentImpl.createChildNode(name, def, nodeType, null);
  }

  protected Property setProperty(NodeImpl parentImpl, Name name, Value value) throws RepositoryException {
    return setProperty(parentImpl, name, value, false);
  }

  protected Property setProperty(NodeImpl parentImpl, Name name, Value value, boolean ignorePermissions)
      throws RepositoryException {
    if (!ignorePermissions) {
      checkPermission(parentImpl, name, getPermission(false, false));
    }
    InternalValue intVs = InternalValue.create(value, parentImpl.session);
    return parentImpl.internalSetProperty(name, intVs);
  }

  protected Property setProperty(NodeImpl parentImpl, Name name, Value[] values) throws RepositoryException {
    checkPermission(parentImpl, name, getPermission(false, false));
    InternalValue[] intVs = new InternalValue[values.length];
    for (int i = 0; i < values.length; i++) {
      intVs[i] = InternalValue.create(values[i], parentImpl.session);
    }
    return parentImpl.internalSetProperty(name, intVs);
  }

  protected void removeItem(ItemImpl itemImpl) throws RepositoryException {
    checkPermission(itemImpl, getPermission(itemImpl.isNode(), true));
    itemImpl.internalRemove(true);
  }

  private void checkPermission(ItemImpl item, int perm) throws RepositoryException {
    if (perm > Permission.NONE) {
      SessionImpl sImpl = (SessionImpl) item.getSession();
      AccessManager acMgr = sImpl.getAccessManager();

      Path path = item.getPrimaryPath();
      acMgr.checkPermission(path, perm);
    }
  }

  private void checkPermission(NodeImpl node, Name childName, int perm) throws RepositoryException {
    if (perm > Permission.NONE) {
      SessionImpl sImpl = (SessionImpl) node.getSession();
      AccessManager acMgr = sImpl.getAccessManager();

      boolean isGranted = acMgr.isGranted(node.getPrimaryPath(), childName, perm);
      if (!isGranted) {
        throw new AccessDeniedException("Permission denied.");
      }
    }
  }

  private int getPermission(boolean isNode, boolean isRemove) {
    if (permission < Permission.NONE) {
      if (isNode) {
        return (isRemove) ? Permission.REMOVE_NODE : Permission.ADD_NODE;
      } else {
        return (isRemove) ? Permission.REMOVE_PROPERTY : Permission.SET_PROPERTY;
      }
    } else {
      return permission;
    }
  }

}
