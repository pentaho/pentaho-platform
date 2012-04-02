package org.pentaho.platform.repository2.unified.jcr;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository2.unified.jcr.IAclMetadataStrategy.AclMetadata;

/**
 * ACL utilities.
 * 
 * <p>These utility methods are static because they are used from within Jackrabbit.</p>
 * 
 * @author mlowery
 */
public class JcrRepositoryFileAclUtils {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JcrRepositoryFileAclUtils.class);

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repository.server.aclMetadataStrategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty(SYSTEM_PROPERTY);

  private static IAclMetadataStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  private JcrRepositoryFileAclUtils() {
    super();
  }

  // ~ Methods =========================================================================================================

  private static void initialize() {
    if ((strategyName == null) || "".equals(strategyName)) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if (strategyName.equals(DEFAULT)) {
      strategy = new JcrAclMetadataStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class<?> clazz = Class.forName(strategyName);
        Constructor<?> customStrategy = clazz.getConstructor(new Class[] {});
        strategy = (IAclMetadataStrategy) customStrategy.newInstance(new Object[] {});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    logger.debug("JcrRepositoryFileAclUtils initialized: strategy=" + strategyName); //$NON-NLS-1$
  }

  public static AclMetadata getAclMetadata(final Session session, final String path, final AccessControlList acList)
      throws RepositoryException {
    return strategy.getAclMetadata(session, path, acList);
  }

  public static void setAclMetadata(final Session session, final String path, final AccessControlList acList,
      final AclMetadata aclMetadata) throws RepositoryException {
    strategy.setAclMetadata(session, path, acList, aclMetadata);
  }

  public static List<AccessControlEntry> removeAclMetadata(final List<AccessControlEntry> acEntries)
      throws RepositoryException {
    return strategy.removeAclMetadata(acEntries);
  }
  
  /**
   * Expands all aggregate privileges.
   * 
   * @param privileges input privileges
   * @param expandNonStandardOnly if {@code true} expand only privileges outside of jcr: namespace
   * @return expanded privileges
   */
  public static Privilege[] expandPrivileges(final Privilege[] privileges, final boolean expandNonStandardOnly) {
    // find all aggregate privileges and expand
    Set<Privilege> expandedPrivileges = new HashSet<Privilege>();
    expandedPrivileges.addAll(Arrays.asList(privileges));
    while (true) {
      boolean foundAggregatePrivilege = false;
      Set<Privilege> iterable = new HashSet<Privilege>(expandedPrivileges);
      for (Privilege privilege : iterable) {
        // expand impl custom privileges (e.g. rep:write) but keep aggregates like jcr:write intact
        if (!expandNonStandardOnly || expandNonStandardOnly && !privilege.getName().startsWith("jcr:")) { //$NON-NLS-1$
          if (privilege.isAggregate()) {
            expandedPrivileges.remove(privilege);
            expandedPrivileges.addAll(Arrays.asList(privilege.getAggregatePrivileges()));
            foundAggregatePrivilege = true;
          }
        }
      }
      if (!foundAggregatePrivilege) {
        break;
      }
    }
    return expandedPrivileges.toArray(new Privilege[0]);
  }
  
}
