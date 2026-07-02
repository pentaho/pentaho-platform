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


package org.pentaho.platform.web.servlet.matchers;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.web.http.api.resources.RepositoryResource;
import org.pentaho.platform.web.servlet.GenericServlet;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A request matcher that matches requests for Require JS configuration files of Pentaho plugins.
 * <p>
 * The matcher checks if the request's method is `GET` and its path info ends with `(*-)require(-js)(-bundles)(-cfg).js`,
 * and, additionally, if the request is for a static resource.
 * <p>
 * This is used to ensure that these files are not accessed in a way that bypasses security restrictions (see PPP-4794).
 */
public class RequireJsConfigRequestMatcher implements RequestMatcher {
  /**
   * This pattern is used to match the `(*-)require(-js)(-bundles)(-cfg).js` files of Pentaho plugins.
   * <p>
   * This pattern is tested against the servlet's path info of the request.
   * Neither the servlet's path nor the query string parameters are included in the match.
   */
  private static final Pattern REQUIRE_JS_CFG_PATTERN =
    Pattern.compile( "\\A/([^/]+/)*+([^/]+-)?require(-js)?(-bundles)?(-cfg)?.js\\Z" );

  private static final String HTTP_METHOD_GET = "GET";

  @NonNull
  private final RepositoryResource repositoryResource;
  @NonNull
  private final GenericServlet genericServlet;

  public RequireJsConfigRequestMatcher() {
    this( new RepositoryResource(), new GenericServlet() );
  }

  public RequireJsConfigRequestMatcher( @NonNull RepositoryResource repositoryResource,
                                        @NonNull GenericServlet genericServlet ) {
    this.repositoryResource = Objects.requireNonNull( repositoryResource );
    this.genericServlet = Objects.requireNonNull( genericServlet );
  }

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
    return isRequireJsConfigRequest( request ) && isStaticResource( request );
  }

  /**
   * Checks if the request's method is `GET` and its path info matches {@link #REQUIRE_JS_CFG_PATTERN}.
   *
   * @param request The request.
   * @return {@code true} if the request method and path info match; {@code false}, otherwise.
   */
  protected boolean isRequireJsConfigRequest( @NonNull HttpServletRequest request ) {
    return HTTP_METHOD_GET.equals( request.getMethod() )
      && ( request.getPathInfo() != null && REQUIRE_JS_CFG_PATTERN.matcher( request.getPathInfo() ).matches() );
  }

  /**
   * Checks if the request is for a static resource of a Pentaho plugin.
   * @param request The request.
   * @return {@code true} if the request is for a static resource; {@code false}, otherwise.
   */
  protected boolean isStaticResource( @NonNull HttpServletRequest request ) {
    return genericServlet.isStaticResource( request )
      || repositoryResource.isStaticResource( request );
  }
}
