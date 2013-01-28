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
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 8, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.web.http.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;

public class PentahoHibernateFilter implements Filter {

  private static final Log logger = LogFactory.getLog(PentahoHibernateFilter.class);

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(final FilterConfig arg0) {
    PentahoHibernateFilter.logger.info(Messages.getInstance().getString("HIBFILTER.INFO_INIT")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
      // Commit any pending database transaction.
      HibernateUtil.commitTransaction();
    } finally {
      // No matter what happens, close the Session.
      HibernateUtil.closeSession();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    // Do nothing here...
  }

}
