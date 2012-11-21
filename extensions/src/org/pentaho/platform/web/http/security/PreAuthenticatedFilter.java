package org.pentaho.platform.web.http.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;
import org.springframework.security.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * This filter handles the assignment of IPentahoSession and Authentication based on a previously stored in the IPreAuthenticatedSessionHolder.
 *
 * User: nbaker
 * Date: 6/28/12
 */
public class PreAuthenticatedFilter implements Filter {

  private static Log log = LogFactory.getLog(PreAuthenticatedFilter.class);
  private PreAuthenticatedSessionHolder preAuthHolder;
  private static final String ASSO_KEY = "asso";

  public PreAuthenticatedFilter(PreAuthenticatedSessionHolder sessionHolder){
    this.preAuthHolder = sessionHolder;
  }


  @Override
  public void destroy() {
    preAuthHolder.close();
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }


  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    try{
      String assoKey = servletRequest.getParameter(ASSO_KEY);
      if(StringUtils.isEmpty(assoKey)){
        return;
      }
      preAuthHolder.restoreSession(assoKey);

    } catch (Exception e) {
      log.error(e);
      // ensure that no credentials remain.
      SecurityContextHolder.getContext().setAuthentication(null);
      PentahoSessionHolder.removeSession();
    } finally {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

}
