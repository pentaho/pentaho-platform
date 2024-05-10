/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

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

