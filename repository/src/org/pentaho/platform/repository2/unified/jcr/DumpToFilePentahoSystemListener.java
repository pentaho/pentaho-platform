package org.pentaho.platform.repository2.unified.jcr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates an XML export of the JCR to the file given in a system property called pentaho.repository.dumpToFile.
 * 
 * To use (1) add the following to the end of {@code systemListeners.xml}:
 * 
 * <pre>
 * &lt;bean id="dumpToFilePentahoSystemListener" 
 *   class="org.pentaho.platform.repository2.unified.jcr.DumpToFilePentahoSystemListener" /&gt;
 * </pre>
 * 
 * Then (2) add the following system property on the command line:
 * 
 * <pre>
 * -Dpentaho.repository.dumpToFile=/tmp/repodump
 * </pre>
 * 
 * <p>
 * Uses PentahoSystem instead of Spring injection since its collaborators are not yet instantiated when this class is
 * instantiated.
 * </p>
 * 
 * @author mlowery
 */
public class DumpToFilePentahoSystemListener implements IPentahoSystemListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DumpToFilePentahoSystemListener.class);

  public static final String PROP_DUMP_TO_FILE = "pentaho.repository.dumpToFile"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  // ~ Methods =========================================================================================================

  @Override
  public boolean startup(IPentahoSession pentahoSession) {
    String filename = System.getProperty(PROP_DUMP_TO_FILE);
    if (filename != null) {
      final JcrTemplate jcrTemplate = PentahoSystem.get(JcrTemplate.class, "jcrTemplate", pentahoSession); //$NON-NLS-1$
      TransactionTemplate txnTemplate = PentahoSystem.get(TransactionTemplate.class,
          "jcrTransactionTemplate", pentahoSession); //$NON-NLS-1$
      String repositoryAdminUsername = PentahoSystem.get(String.class, "repositoryAdminUsername", pentahoSession); //$NON-NLS-1$

      final String ZIP_EXTENSION = ".zip"; //$NON-NLS-1$
      // let the user know this is a zip
      if (!filename.endsWith(ZIP_EXTENSION)) {
        filename = filename + ZIP_EXTENSION;
      }
      logger.debug(String.format("dumping repository to file \"%s\"", filename)); //$NON-NLS-1$
      ZipOutputStream tmpOut = null;
      try {
        tmpOut = new
            ZipOutputStream(new BufferedOutputStream(FileUtils.openOutputStream(new File(filename))));
      } catch (IOException e) {
        IOUtils.closeQuietly(tmpOut);
        throw new RuntimeException(e);
      }
      final ZipOutputStream out = tmpOut;
      // stash existing session
      IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
      // run as repo super user
      PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession(repositoryAdminUsername));
      try {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
          public void doInTransactionWithoutResult(final TransactionStatus status) {
            jcrTemplate.execute(new JcrCallback() {
              public Object doInJcr(final Session session) throws RepositoryException, IOException {
                final boolean SKIP_BINARY = false;
                final boolean NO_RECURSE = false;
                out.putNextEntry(new ZipEntry("repository.xml")); //$NON-NLS-1$
                session.exportSystemView("/", out, SKIP_BINARY, NO_RECURSE); //$NON-NLS-1$
                return null;
              }
            });
          }
        });
      } finally {
        // restore original session
        PentahoSessionHolder.setSession(origPentahoSession);
        IOUtils.closeQuietly(out);
      }
      logger.debug(String.format("dumped repository to file \"%s\"", filename)); //$NON-NLS-1$
    }
    return true;
  }

  protected IPentahoSession createRepositoryAdminPentahoSession(final String repositoryAdminUsername) {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

  @Override
  public void shutdown() {
  }

}
