package org.pentaho.platform.api.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An access control policy.
 * 
 * <p>
 * Reponsible for determining if access to a given action should be allowed or denied. A implementation could be
 * one based on roles, as is done in the Servlet specification. (In other words, if the policy has an association 
 * between the given action and a role that has been granted to the user, then the decision will be to allow.)
 * </p>
 * 
 * @author mlowery
 */
public interface IAuthorizationPolicy {
  
  class Initializer {
    static Set<String> getPredefinedSystemRoles() {
      HashSet<String> predefinedSystemRoles = new HashSet<String>();
      predefinedSystemRoles.add(CREATE_REPOSITORY_CONTENT_ACTION);
      predefinedSystemRoles.add(READ_REPOSITORY_CONTENT_ACTION);
      predefinedSystemRoles.add(ADMINISTER_SECURITY_ACTION);
      predefinedSystemRoles.add(CREATE_TENANTS_ACTION);
      predefinedSystemRoles.add(MANAGE_SCHEDULING);
      return predefinedSystemRoles;
    }
  }
  public static final String CREATE_REPOSITORY_CONTENT_ACTION = "org.pentaho.repository.create";
  public static final String READ_REPOSITORY_CONTENT_ACTION = "org.pentaho.repository.read";
  public static final String ADMINISTER_SECURITY_ACTION = "org.pentaho.security.administerSecurity";
  public static final String CREATE_TENANTS_ACTION = "org.pentaho.security.administerSystem";
  public static final String MANAGE_SCHEDULING = "org.pentaho.scheduler.manage";
  
  public static final Set<String> PREDEFINED_SYSTEM_LOGICAL_ROLES = Initializer.getPredefinedSystemRoles();
  
  
  /**
   * Returns {@code true} if the the action should be allowed.
   * 
   * @param actionName name of action (e.g. {@code org.pentaho.di.repository.create})
   * @return {@code true} to allow
   */
  boolean isAllowed(final String actionName);

  /**
   * Returns all actions in the given namespace that are currently allowed.
   * 
   * @param actionNamespace action namespace (e.g. {@code org.pentaho.di.repository}); {@code null} means all allowed actions
   * @return list of actions
   */
  List<String> getAllowedActions(final String actionNamespace);

}
