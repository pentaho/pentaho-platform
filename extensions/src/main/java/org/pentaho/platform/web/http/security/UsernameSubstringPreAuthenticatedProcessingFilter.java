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


package org.pentaho.platform.web.http.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specialization of {@link J2eePreAuthenticatedProcessingFilter} where
 * {@link #getPreAuthenticatedPrincipal(HttpServletRequest)} optionally extracts a substring of the user principal to
 * use as the return value.
 * 
 * <p>
 * The original user principal is stored in a request attribute named {@link #PENTAHO_ORIG_USER_PRINCIPAL}.
 * </p>
 * 
 * @author mlowery
 */
public class UsernameSubstringPreAuthenticatedProcessingFilter extends J2eePreAuthenticatedProcessingFilter {

  private static final Log logger = LogFactory.getLog( UsernameSubstringPreAuthenticatedProcessingFilter.class );

  /**
   * Key under which original {@link HttpServletRequest#getUserPrincipal()} name is stored.
   */
  public static final String PENTAHO_ORIG_USER_PRINCIPAL = "PENTAHO_ORIG_USER_PRINCIPAL"; //$NON-NLS-1$

  private Pattern pattern;

  @Override
  protected Object getPreAuthenticatedPrincipal( final HttpServletRequest httpRequest ) {
    String username = httpRequest.getUserPrincipal() == null ? null : httpRequest.getUserPrincipal().getName();
    logger.debug( "original user principal: " + username ); //$NON-NLS-1$
    httpRequest.setAttribute( PENTAHO_ORIG_USER_PRINCIPAL, username );
    if ( username != null && pattern != null ) {
      Matcher m = pattern.matcher( username );
      logger.debug( "pattern: " + pattern ); //$NON-NLS-1$
      logger.debug( "input: " + username ); //$NON-NLS-1$
      if ( m.find() ) {
        logger.debug( "pattern matches input; saving capture group" ); //$NON-NLS-1$
        username = m.group( 1 );
      }
    }
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Windows PreAuthenticated J2EE principal: " + username ); //$NON-NLS-1$
    }
    return username;
  }

  /**
   * Regular expression where the first capture group will be extracted and used as the username. Set to {@code null} or
   * empty string to disable matching and extraction.
   * 
   * <p>
   * Example:
   * 
   * <pre>
   * {@code .+\\(.+)}
   * </pre>
   * 
   * The above example regular expression would extract {@code USER} from {@code DOMAIN\USER}.
   */
  public void setRegex( final String regex ) {
    if ( StringUtils.isNotBlank( regex ) ) {
      pattern = Pattern.compile( regex );
    } else {
      pattern = null;
    }
  }

}
