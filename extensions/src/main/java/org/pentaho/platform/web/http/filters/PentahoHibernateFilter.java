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


package org.pentaho.platform.web.http.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class PentahoHibernateFilter implements Filter {

  private static final Log logger = LogFactory.getLog( PentahoHibernateFilter.class );

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
   */
  public void init( final FilterConfig arg0 ) {
    PentahoHibernateFilter.logger.info( Messages.getInstance().getString( "HIBFILTER.INFO_INIT" ) ); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse,
   * jakarta.servlet.FilterChain)
   */
  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
    throws IOException, ServletException {
    try {
      chain.doFilter( request, response );
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
   * @see jakarta.servlet.Filter#destroy()
   */
  public void destroy() {
    // Do nothing here...
  }

}
