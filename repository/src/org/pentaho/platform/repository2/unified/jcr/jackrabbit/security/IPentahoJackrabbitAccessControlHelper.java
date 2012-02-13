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
package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.util.List;
import java.util.Map;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;

/**
 * Helper class for PentahoAccessControlProvider.
 * 
 * @author mlowery
 */
public interface IPentahoJackrabbitAccessControlHelper {

  void init(Map configuration);
  
  /**
   * Builds a mask consisting of org.apache.jackrabbit.core.security.authorization.Permission constants for given path
   * and principal names.
   */
  int buildMask(Path absPath, SessionImpl session, List<String> principalNames, NamePathResolver resolver,
      PentahoAccessControlEditor systemEditor) throws RepositoryException;

  /**
   * Returns the effective AccessControlConstants.N_POLICY node.
   */
  NodeImpl getEffectiveAclNode(final SessionImpl session, final PentahoAccessControlEditor systemEditor,
      final String jcrPath) throws RepositoryException;
}
