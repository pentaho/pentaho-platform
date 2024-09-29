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

package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Factory for Jackrabbit System Sessions. This should only be used to perform low-level JCR internal operations.
 *
 * Created by nbaker on 10/6/15.
 */
public interface IPentahoSystemSessionFactory {
  Session create(RepositoryImpl repository) throws RepositoryException;

  class DefaultImpl implements IPentahoSystemSessionFactory {
    public Session create(RepositoryImpl repository) throws
        RepositoryException {
      return SystemSession.create( repository.getRepositoryContext(), repository.getWorkspaceInfo( "default" ).getConfig() );
    }
  }


}
