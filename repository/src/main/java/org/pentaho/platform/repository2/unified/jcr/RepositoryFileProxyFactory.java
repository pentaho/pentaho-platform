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

package org.pentaho.platform.repository2.unified.jcr;

import javax.jcr.Node;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.springframework.extensions.jcr.JcrTemplate;

/**
 * User: nbaker
 * Date: 5/28/13
 */
public class RepositoryFileProxyFactory {
  private JcrTemplate template;
  private IRepositoryFileDao repositoryFileDao;

  public RepositoryFileProxyFactory(JcrTemplate template, IRepositoryFileDao repositoryFileDao) {
    this.template = template;
    this.repositoryFileDao = repositoryFileDao;
  }

  public RepositoryFileProxy getProxy(final Node node, IPentahoLocale pentahoLocale){
    return new RepositoryFileProxy(node, template, pentahoLocale);
  }
}