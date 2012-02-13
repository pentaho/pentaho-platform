/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.security.policy.rolebased;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections.map.LRUMap;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import com.google.common.collect.HashMultimap;

/**
 * An {@link IRoleAuthorizationPolicyRoleBindingDao} implementation that uses JCR. Storage is done using nodes and
 * properties, not XML. Storage looks like this:
 * 
 * <pre>{@code 
 * - acme
 *   - .authz
 *     - roleBased
 *       - runtimeRoles
 *         - runtimeRole1
 *           - logicalRole1,logicalRole2 (multi-valued property)
 *         - runtimeRole2
 *           - logicalRole2 (multi-valued property)
 * }</pre>
 * 
 * <p>Note: All multi-valued properties are ordered.</p>
 * 
 * <p>Note: This code runs as the repository superuser. Ideally this would run as the tenant admin but such a named user
 * doesn't exist for us to run as. Now that the repo uses IAuthorizationPolicy for access control, this code MUST 
 * continue to run as the repository superuser.  This is one reason not to implement this on top of PUR.</p>
 * 
 * @author mlowery
 */
public class JcrRoleAuthorizationPolicyRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {

  private static final String FOLDER_NAME_AUTHZ = ".authz"; //$NON-NLS-1$

  private static final String FOLDER_NAME_ROLEBASED = "roleBased"; //$NON-NLS-1$

  private static final String FOLDER_NAME_RUNTIMEROLES = "runtimeRoles"; //$NON-NLS-1$

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  /**
   * Keys are locales. Values are Properties instances. The map must contain a key of empty string which will act as the
   * root locale.
   */
  private Map<String, Properties> logicalRoleLocaleMap;

  /**
   * Repository super user.
   */
  protected String repositoryAdminUsername;

  protected TransactionTemplate txnTemplate;

  protected Map<String, List<String>> immutableRoleBindings;

  protected Map<String, List<String>> bootstrapRoleBindings;
  
  /**
   * Key: runtime role name; value: list of logical role names
   */
  @SuppressWarnings("unchecked")
  protected Map boundLogicalRoleNamesCache = Collections.synchronizedMap(new LRUMap());

  // ~ Constructors ====================================================================================================

  public JcrRoleAuthorizationPolicyRoleBindingDao(final TransactionTemplate txnTemplate, final JcrTemplate jcrTemplate,
      final List<String> logicalRoleNames, final Map<String, Properties> logicalRoleLocaleMap,
      final Map<String, List<String>> immutableRoleBindings, final Map<String, List<String>> bootstrapRoleBindings,
      final String repositoryAdminUsername) {
    super();
    Assert.notNull(txnTemplate);
    Assert.notNull(jcrTemplate);
    Assert.notNull(immutableRoleBindings);
    Assert.notNull(bootstrapRoleBindings);
    Assert.notNull(logicalRoleNames);
    Assert.notNull(logicalRoleLocaleMap);
    Assert.notNull(repositoryAdminUsername);
    this.txnTemplate = txnTemplate;
    this.jcrTemplate = jcrTemplate;
    this.immutableRoleBindings = immutableRoleBindings;
    this.bootstrapRoleBindings = bootstrapRoleBindings;
    this.logicalRoleLocaleMap = logicalRoleLocaleMap;
    this.repositoryAdminUsername = repositoryAdminUsername;
    initTransactionTemplate();
  }

  // ~ Methods =========================================================================================================

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * {@inheritDoc}
   */
  public RoleBindingStruct getRoleBindingStruct(final String locale) {
    return new RoleBindingStruct(getMapForLocale(locale), getRoleBindings());
  }

  /**
   * {@inheritDoc}
   */
  public void setRoleBindings(final String runtimeRoleName, final List<String> logicalRoleNames) {
    Assert.notNull(logicalRoleNames);
    final String tenantId = TenantUtils.getTenantId();
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          jcrTemplate.execute(new JcrCallback() {
            @SuppressWarnings("unchecked")
            public Object doInJcr(final Session session) throws RepositoryException, IOException {
              PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
              final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
              final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
              Node runtimeRolesFolderNode = getOrCreateRuntimeRolesFolderNode(session, pentahoJcrConstants, tenantId);
              NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
              int i = 0;
              while (runtimeRoleNodes.hasNext()) {
                runtimeRoleNodes.nextNode();
                i++;
              }
              if (i == 0) {
                // no bindings setup yet; install bootstrap bindings; bootstrapRoleBindings will now no longer be 
                // consulted
                for (Map.Entry<String, List<String>> entry : bootstrapRoleBindings.entrySet()) {
                  internalSetBindings(pentahoJcrConstants, runtimeRolesFolderNode, phoNsPrefix
                      + substitute(entry.getKey(), tenantId), entry.getValue());
                }
              }
              if (!isImmutable(runtimeRoleName, tenantId)) {
                internalSetBindings(pentahoJcrConstants, runtimeRolesFolderNode, phoNsPrefix + runtimeRoleName,
                    logicalRoleNames);
              } else {
                throw new RuntimeException(Messages.getInstance().getString(
                    "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0001_ATTEMPT_MOD_IMMUTABLE", runtimeRoleName)); //$NON-NLS-1$
              }
              session.save();
              Assert.isTrue(runtimeRolesFolderNode.hasNode(phoNsPrefix + runtimeRoleName));
              
              // update cache
              boundLogicalRoleNamesCache.put(runtimeRoleName, logicalRoleNames);
              
              return null;
            }
          });
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected boolean isImmutable(final String runtimeRoleName, final String tenantId) {
    return substitute(immutableRoleBindings, tenantId).containsKey(runtimeRoleName);
  }

  protected String substitute(final String roleName, final String tenantId) {
    return MessageFormat.format(roleName, tenantId);
  }

  protected Map<String, List<String>> substitute(final Map<String, List<String>> map, final String tenantId) {
    Map<String, List<String>> mapWithSubstitutions = new HashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      mapWithSubstitutions.put(substitute(entry.getKey(), tenantId), entry.getValue());
    }
    return mapWithSubstitutions;
  }

  protected void internalSetBindings(final PentahoJcrConstants pentahoJcrConstants, final Node runtimeRolesFolderNode,
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

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<String> getBoundLogicalRoleNames(final List<String> runtimeRoleNames) {
    
    // what runtimeRoleNames are in the cache; we don't need to fetch them
    final List<String> uncachedRuntimeRoleNames = new ArrayList<String>();
    final Set<String> cachedBoundLogicalRoleNames = new HashSet<String>();
    for (String runtimeRoleName : runtimeRoleNames) {
      if (boundLogicalRoleNamesCache.containsKey(runtimeRoleName)) {
        cachedBoundLogicalRoleNames.addAll((Collection<String>) boundLogicalRoleNamesCache.get(runtimeRoleName));
      } else {
        uncachedRuntimeRoleNames.add(runtimeRoleName);
      }
    }
    if (uncachedRuntimeRoleNames.isEmpty()) {
      // no need to hit the repo
      return new ArrayList<String>(cachedBoundLogicalRoleNames);
    }

    final String tenantId = TenantUtils.getTenantId();
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (List<String>) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          return jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(final Session session) throws RepositoryException, IOException {
              PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
              final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
              final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
              HashMultimap<String, String> boundLogicalRoleNames = HashMultimap.create();
              Node runtimeRolesFolderNode = getOrCreateRuntimeRolesFolderNode(session, pentahoJcrConstants, tenantId);
              NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
              if (!runtimeRoleNodes.hasNext()) {
                // no bindings setup yet; fall back on bootstrap bindings
                Map<String, List<String>> bootstrapRoleBindingsWithSubstitutions = substitute(bootstrapRoleBindings,
                    tenantId);
                for (String runtimeRoleName : uncachedRuntimeRoleNames) {
                  if (bootstrapRoleBindingsWithSubstitutions.containsKey(runtimeRoleName)) {
                    boundLogicalRoleNames.putAll(runtimeRoleName, bootstrapRoleBindingsWithSubstitutions.get(runtimeRoleName));
                  }
                }
              } else {
                for (String runtimeRoleName : uncachedRuntimeRoleNames) {
                  if (runtimeRolesFolderNode.hasNode(phoNsPrefix + runtimeRoleName)) {
                    Node runtimeRoleFolderNode = runtimeRolesFolderNode.getNode(phoNsPrefix + runtimeRoleName);
                    if (runtimeRoleFolderNode.hasProperty(pentahoJcrConstants.getPHO_BOUNDROLES())) {
                      Value[] values = runtimeRoleFolderNode.getProperty(pentahoJcrConstants.getPHO_BOUNDROLES())
                          .getValues();
                      for (Value value : values) {
                        boundLogicalRoleNames.put(runtimeRoleName, value.getString());
                      }
                    }
                  }
                }
              }
              // now add in immutable bound logical role names
              Map<String, List<String>> immutableRoleBindingsWithSubstitutions = substitute(immutableRoleBindings,
                  tenantId);
              for (String runtimeRoleName : uncachedRuntimeRoleNames) {
                if (immutableRoleBindingsWithSubstitutions.containsKey(runtimeRoleName)) {
                  boundLogicalRoleNames.putAll(runtimeRoleName, immutableRoleBindingsWithSubstitutions.get(runtimeRoleName));
                }
              }
              
              // update cache
              boundLogicalRoleNamesCache.putAll(boundLogicalRoleNames.asMap());
              // now add in those runtime roles that have no bindings to the cache
              for (String runtimeRoleName : uncachedRuntimeRoleNames) {
                if (!boundLogicalRoleNamesCache.containsKey(runtimeRoleName)) {
                  boundLogicalRoleNamesCache.put(runtimeRoleName, Collections.emptyList());
                }
              }
              
              // combine cached findings plus ones from repo
              Set<String> res = new HashSet<String>();
              res.addAll(cachedBoundLogicalRoleNames);
              res.addAll(boundLogicalRoleNames.values());
              return new ArrayList<String>(res);
            }
          });
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected Node getOrCreateAuthzFolderNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final String tenantId) throws RepositoryException {
    Node tenantRootFolderNode = null;
    try {
      tenantRootFolderNode = (Node) session.getItem(ServerRepositoryPaths.getTenantRootFolderPath(tenantId));
    } catch (PathNotFoundException e) {
      Assert.state(false, Messages.getInstance().getString(
          "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0002_REPO_NOT_INITIALIZED")); //$NON-NLS-1$
    }
    if (tenantRootFolderNode.hasNode(FOLDER_NAME_AUTHZ)) {
      return tenantRootFolderNode.getNode(FOLDER_NAME_AUTHZ);
    } else {
      return tenantRootFolderNode.addNode(FOLDER_NAME_AUTHZ, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

  protected Node getOrCreateRoleBasedFolderNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final String tenantId) throws RepositoryException {
    Node authzFolderNode = getOrCreateAuthzFolderNode(session, pentahoJcrConstants, tenantId);
    if (authzFolderNode.hasNode(FOLDER_NAME_ROLEBASED)) {
      return authzFolderNode.getNode(FOLDER_NAME_ROLEBASED);
    } else {
      return authzFolderNode.addNode(FOLDER_NAME_ROLEBASED, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

  protected Node getOrCreateRuntimeRolesFolderNode(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final String tenantId) throws RepositoryException {
    Node roleBasedFolderNode = getOrCreateRoleBasedFolderNode(session, pentahoJcrConstants, tenantId);
    if (roleBasedFolderNode.hasNode(FOLDER_NAME_RUNTIMEROLES)) {
      return roleBasedFolderNode.getNode(FOLDER_NAME_RUNTIMEROLES);
    } else {
      return roleBasedFolderNode.addNode(FOLDER_NAME_RUNTIMEROLES, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

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

  protected Map<String, String> getMapForLocale(final String localeString) {
    final String UNDERSCORE = "_"; //$NON-NLS-1$
    Locale locale;

    if (localeString == null) {
      locale = Locale.getDefault();
    } else {
      String[] tokens = localeString.split(UNDERSCORE);
      if (tokens.length == 3) {
        locale = new Locale(tokens[0], tokens[1], tokens[2]);
      } else if (tokens.length == 2) {
        locale = new Locale(tokens[0], tokens[1]);
      } else {
        locale = new Locale(tokens[0]);
      }
    }

    boolean hasLanguage = StringUtils.hasText(locale.getLanguage());
    boolean hasCountry = StringUtils.hasText(locale.getCountry());
    boolean hasVariant = StringUtils.hasText(locale.getVariant());

    List<String> candidateNames = new ArrayList<String>(3);

    if (hasVariant) {
      candidateNames.add(locale.getLanguage() + UNDERSCORE + locale.getCountry() + UNDERSCORE + locale.getVariant());
    }
    if (hasCountry) {
      candidateNames.add(locale.getLanguage() + UNDERSCORE + locale.getCountry());
    }
    if (hasLanguage) {
      candidateNames.add(locale.getLanguage());
    }
    candidateNames.add(""); //$NON-NLS-1$

    Properties props = null;
    for (String candidateName : candidateNames) {
      if (logicalRoleLocaleMap.containsKey(candidateName)) {
        props = logicalRoleLocaleMap.get(candidateName);
        break;
      }
    }
    Assert.notNull(props);
    Map<String, String> map = new HashMap<String, String>();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      map.put(entry.getKey().toString(), entry.getValue().toString());
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, List<String>> getRoleBindings() {
    final String tenantId = TenantUtils.getTenantId();
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (Map<String, List<String>>) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          return jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(final Session session) throws RepositoryException, IOException {
              PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
              final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
              final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
              Map<String, List<String>> map = new HashMap<String, List<String>>();
              Node runtimeRolesFolderNode = getOrCreateRuntimeRolesFolderNode(session, pentahoJcrConstants, tenantId);
              NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
              if (!runtimeRoleNodes.hasNext()) {
                // no bindings setup yet; fall back on bootstrap bindings
                map.putAll(substitute(bootstrapRoleBindings, tenantId));
              } else {
                while (runtimeRoleNodes.hasNext()) {
                  Node runtimeRoleNode = runtimeRoleNodes.nextNode();
                  if (runtimeRoleNode.hasProperty(pentahoJcrConstants.getPHO_BOUNDROLES())) {
                    // get clean runtime role name
                    String runtimeRoleName = runtimeRoleNode.getName().substring(phoNsPrefix.length());
                    // get logical role names
                    List<String> logicalRoleNames = new ArrayList<String>();
                    Value[] values = runtimeRoleNode.getProperty(pentahoJcrConstants.getPHO_BOUNDROLES()).getValues();
                    for (Value value : values) {
                      logicalRoleNames.add(value.getString());
                    }
                    map.put(runtimeRoleName, logicalRoleNames);
                  }
                }
              }
              // add all immutable bindings
              map.putAll(substitute(immutableRoleBindings, tenantId));
              return map;
            }
          });
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

}
