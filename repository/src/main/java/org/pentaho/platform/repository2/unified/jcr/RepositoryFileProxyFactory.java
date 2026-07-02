/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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