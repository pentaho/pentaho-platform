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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;


public class WebUtil {

    static String ORIGIN_HEADER = "origin";
    static String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    static String CORS_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";

    public static void setCorsResponseHeaders(HttpServletRequest request, HttpServletResponse response ) {
        if ( !WebUtil.isCorsRequestsAllowed() ) {
            return;
        }

        String origin = request.getHeader( ORIGIN_HEADER );
        if ( WebUtil.isCorsRequestOriginAllowed( origin ) ) {
            response.setHeader( CORS_ALLOW_ORIGIN_HEADER, origin );
            response.setHeader( CORS_ALLOW_CREDENTIALS_HEADER, "true" );
        }
    }

    private static boolean isCorsRequestsAllowed() {
        String isCorsAllowed = WebUtil.getCorsRequestsAllowedSystemProperty();
        return "true".equals( isCorsAllowed );
    }

    private static List<String> getCorsRequestsAllowedDomains() {
        String allowedDomains = WebUtil.getCorsAllowedDomainsSystemProperty();
        boolean hasDomains = !StringUtil.isEmpty( allowedDomains );

        return hasDomains ? Arrays.asList( allowedDomains.split( "\\s*,\\s*" ) ) : null;
    }

    private static boolean isCorsRequestOriginAllowed( String domain ) {
        List<String> allowedDomains = WebUtil.getCorsRequestsAllowedDomains();
        return allowedDomains != null && allowedDomains.contains( domain );
    }

    // region package-private methods for unit testing mock/spying
    static String getCorsRequestsAllowedSystemProperty() {
        return PentahoSystem.getSystemSetting( PentahoSystem.CORS_REQUESTS_ALLOWED, "false" );
    }

    static String getCorsAllowedDomainsSystemProperty() {
        return PentahoSystem.getSystemSetting( PentahoSystem.CORS_REQUESTS_ALLOWED_DOMAINS, null );
    }
    // endregion
}
