/*!
 *
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
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.user; 

import java.util.Properties;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;

public class PentahoUserManagerImpl extends UserManagerImpl {

  public PentahoUserManagerImpl( SessionImpl session, String adminId, Properties config ) throws RepositoryException {
    super( session, adminId, config );
    // TODO Auto-generated constructor stub
  }

  /**
   * We are over riding this method to always set the forceHash value to to be value. It will then hash the password if it is a plain one
   * other wise it will store the encrypted password.
   * @param userNode
   * @param password
   * @param forceHash
   * @throws RepositoryException
   */
  void setPassword( NodeImpl userNode, String password, boolean forceHash ) throws RepositoryException {
    // TODO Auto-generated method stub
    super.setPassword( userNode, password, false );
  }

  
}
