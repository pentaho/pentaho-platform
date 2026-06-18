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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckRefererFilter implements Filter, InitializingBean {

  private String refererPrefix;
  private String redirectTo;
  private boolean checked;

  public void afterPropertiesSet() throws Exception {
    if ( StringUtils.isBlank( refererPrefix ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
        "CheckRefererFilter.ERROR_0001_REFERER_PREFIX_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    }
    if ( StringUtils.isBlank( redirectTo ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
          "CheckRefererFilter.ERROR_0002_REDIRECT_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    }
    checked = true;
  }

  public void setRefererPrefix( String value ) {
    this.refererPrefix = value;
  }

  public void setRedirectTo( String value ) {
    this.redirectTo = value;
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String header = req.getHeader( "referer" ); //$NON-NLS-1$
    if ( ( header != null ) && ( header.startsWith( refererPrefix ) ) ) {
      chain.doFilter( request, response );
    } else {
      // Illegal Referer - cloaked, missing, or invalid
      System.out.println( "***** No Referrer: " + req.getRequestURL() ); //$NON-NLS-1$
      HttpServletResponse resp = (HttpServletResponse) response;
      resp.sendRedirect( redirectTo );
    }
  }

  public void destroy() {
    // Required to be here...
  }

  public void init( FilterConfig filterConfig ) throws ServletException {
    String pfx = filterConfig.getInitParameter( "refererPrefix" ); //$NON-NLS-1$
    String redirect = filterConfig.getInitParameter( "redirectTo" ); //$NON-NLS-1$
    if ( !( StringUtils.isBlank( pfx ) ) ) {
      this.setRefererPrefix( pfx );
    }
    if ( !( StringUtils.isBlank( redirect ) ) ) {
      this.setRedirectTo( redirect );
    }
    if ( !checked ) {
      try {
        afterPropertiesSet();
      } catch ( Exception e ) {
        throw new ServletException( e );
      }
    }
  }

}
