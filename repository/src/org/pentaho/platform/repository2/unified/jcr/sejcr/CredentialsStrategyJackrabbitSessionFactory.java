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
package org.pentaho.platform.repository2.unified.jcr.sejcr;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.extensions.jcr.SessionHolderProviderManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Copy-and-paste of {@link org.springframework.extensions.jcr.jackrabbit.JackrabbitSessionFactory} except that it 
 * extends {@link CredentialsStrategySessionFactory}. Also has fixes from 
 * <a href="http://jira.springframework.org/browse/SEJCR-18">SEJCR-18</a>.
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class CredentialsStrategyJackrabbitSessionFactory extends CredentialsStrategySessionFactory {

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, CredentialsStrategy credentialsStrategy) {
    super(repository, credentialsStrategy);
  }

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, String workspaceName,
      CredentialsStrategy credentialsStrategy, SessionHolderProviderManager sessionHolderProviderManager) {
    super(repository, workspaceName, credentialsStrategy, sessionHolderProviderManager);
  }

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, String workspaceName,
      CredentialsStrategy credentialsStrategy) {
    super(repository, workspaceName, credentialsStrategy);
  }

  private static final Logger LOG = LoggerFactory.getLogger(CredentialsStrategyJackrabbitSessionFactory.class);

  /**
   * Node definitions in CND format.
   */
  private Resource[] nodeDefinitions;

  private String contentType = JackrabbitNodeTypeManager.TEXT_X_JCR_CND;

  /*
   * (non-Javadoc)
   * @see org.springframework.extensions.jcr.JcrSessionFactory#registerNodeTypes()
   */
  protected void registerNodeTypes() throws Exception {
    if (!ObjectUtils.isEmpty(nodeDefinitions)) {

      Session session = getBareSession();
      Workspace ws = session.getWorkspace();

      JackrabbitNodeTypeManager jackrabbitNodeTypeManager = (JackrabbitNodeTypeManager) ws.getNodeTypeManager();

      boolean debug = LOG.isDebugEnabled();
      for (int i = 0; i < nodeDefinitions.length; i++) {
        Resource resource = nodeDefinitions[i];
        if (debug) {
          LOG.debug("adding node type definitions from " + resource.getDescription());
        }
        try {
          // unfortunately, this method lies outside of the interface so a cast is required
          // http://jira.springframework.org/browse/MOD-470
          // http://jira.springframework.org/browse/SEJCR-12
          ((NodeTypeManagerImpl) jackrabbitNodeTypeManager).registerNodeTypes(resource.getInputStream(), contentType, 
              true);
        } catch (RepositoryException ex) {
          LOG.error("Error registering nodetypes ", ex.getCause());
        }
      }
      session.logout();
    }
  }

  /**
   * @param nodeDefinitions The nodeDefinitions to set.
   */
  public void setNodeDefinitions(Resource[] nodeDefinitions) {
    this.nodeDefinitions = nodeDefinitions;
  }

  /**
   * Indicate the node definition content type (by default, JackrabbitNodeTypeManager#TEXT_XML).
   * @see JackrabbitNodeTypeManager#TEXT_X_JCR_CND
   * @see JackrabbitNodeTypeManager#TEXT_XML
   * @param contentType The contentType to set.
   */
  public void setContentType(String contentType) {
    Assert.hasText(contentType, "contentType is required");
    this.contentType = contentType;
  }

}
