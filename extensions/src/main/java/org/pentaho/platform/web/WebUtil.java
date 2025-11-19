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


package org.pentaho.platform.web;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class containing web related functionality.
 */
public class WebUtil {

  private WebUtil() {
  }

  // region CORS
  static final String ORIGIN_HEADER = "origin";
  static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
  static final String CORS_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";

  /**
   * Sets the default CORS response headers if CORS is enabled and the request origin is allowed.
   * <p>
   * The default headers are the {@code Access-Control-Allow-Origin} header with the current allowed origin as a value
   * and the {@code Access-Control-Allow-Credentials} with value {@code true}.
   * <p>
   * This method exists for when it is needed to set CORS headers programmatically. It's generally preferable to
   * configure the system's CORS filter, by editing {@code applicationContext-spring-security-cors.xml} or by publishing
   * an implementation of {@code com.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration} with the
   * Pentaho system.
   *
   * @param request  The HTTP request.
   * @param response The HTTP response.
   */
  public static void setCorsResponseHeaders( HttpServletRequest request, HttpServletResponse response ) {
    setCorsResponseHeaders( request, response, null );
  }

  /**
   * Sets the default CORS response headers and the additional provided headers if CORS is enabled and the request
   * origin is allowed.
   * <p>
   * The default headers are the {@code Access-Control-Allow-Origin} header with the current allowed origin as a value
   * and the {@code Access-Control-Allow-Credentials} with value {@code true}.
   * <p>
   * This method exists for when it is needed to set CORS headers programmatically. It's generally preferable to
   * configure the system's CORS filter, by editing {@code applicationContext-spring-security-cors.xml} or by publishing
   * an implementation of {@code com.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration} with the
   * Pentaho system.
   *
   * @param request                  The HTTP request.
   * @param response                 The HTTP response.
   * @param corsHeadersConfiguration Additional headers to conditionally define.
   */
  public static void setCorsResponseHeaders( HttpServletRequest request, HttpServletResponse response,
                                             Map<String, List<String>> corsHeadersConfiguration ) {
    if ( !WebUtil.isCorsRequestsAllowed() ) {
      return;
    }

    final String origin = request.getHeader( ORIGIN_HEADER );
    if ( WebUtil.isCorsRequestOriginAllowed( origin ) ) {
      response.setHeader( CORS_ALLOW_ORIGIN_HEADER, origin );
      response.setHeader( CORS_ALLOW_CREDENTIALS_HEADER, "true" );

      if ( corsHeadersConfiguration != null ) {
        corsHeadersConfiguration.forEach( ( header, parameters ) -> {
          final String value = String.join( ",", parameters );

          response.setHeader( header, value );
        } );
      }
    }
  }

  /**
   * Gets a value that indicates if CORS is enabled, in principle.
   * <p>
   * Further restrictions apply to determine if a specific cross origin or CORS request is actualy enabled.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  public static boolean isCorsRequestsAllowed() {
    String isCorsAllowed = WebUtil.getCorsRequestsAllowedSystemProperty();
    return "true".equals( isCorsAllowed );
  }

  @VisibleForTesting
  static List<String> getCorsRequestsAllowedOrigins() {
    String allowedDomains = WebUtil.getCorsAllowedOriginsSystemProperty();
    boolean hasDomains = !StringUtil.isEmpty( allowedDomains );

    return hasDomains ? Arrays.asList( allowedDomains.split( "\\s*,\\s*" ) ) : null;
  }

  @VisibleForTesting
  static boolean isCorsRequestOriginAllowed( String origin ) {
    List<String> allowedDomains = WebUtil.getCorsRequestsAllowedOrigins();
    return allowedDomains != null && allowedDomains.contains( origin );
  }

  // region package-private methods for unit testing mock/spying
  static String getCorsRequestsAllowedSystemProperty() {
    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig == null ) {
      return "false";
    }

    return systemConfig.getProperty( PentahoSystem.CORS_REQUESTS_ALLOWED, "false" );
  }

  static String getCorsAllowedOriginsSystemProperty() {
    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig == null ) {
      return null;
    }

    return systemConfig.getProperty( PentahoSystem.CORS_REQUESTS_ALLOWED_ORIGINS );
  }
  // endregion

  // endregion
}
