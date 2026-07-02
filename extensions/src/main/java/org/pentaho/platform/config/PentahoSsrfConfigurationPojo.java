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


package org.pentaho.platform.config;

import com.hitachivantara.security.web.impl.model.ssrf.SsrfConfigurationPojo;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

public class PentahoSsrfConfigurationPojo extends SsrfConfigurationPojo {
  public PentahoSsrfConfigurationPojo( URI mainFullyQualifiedUrl, List<URI> alternativeFullyQualifiedUrls,
                                         boolean isEnabled ) {
    super( buildAllowedOrigins( mainFullyQualifiedUrl, alternativeFullyQualifiedUrls ), isEnabled );
  }

  private static Set<URI> buildAllowedOrigins( URI mainFullyQualifiedUrl, List<URI> alternativeFullyQualifiedUrls ) {
    Set<URI> allowedOrigins = new LinkedHashSet<>();
    allowedOrigins.add( mainFullyQualifiedUrl );
    allowedOrigins.addAll( alternativeFullyQualifiedUrls );
    return allowedOrigins;
  }
}

