package org.pentaho.platform.repository2.unified.jcr;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;

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
  
}
