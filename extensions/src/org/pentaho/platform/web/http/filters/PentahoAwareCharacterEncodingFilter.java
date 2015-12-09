/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.http.messages.Messages;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Looks at the <code>context-param</code> named <code>encoding</code> in <code>web.xml</code> for its encoding
 * selection. If not found, falls back to method used by superclass. Finally, defaults to <code>UTF-8</code>.
 * 
 * @author mlowery
 */
public class PentahoAwareCharacterEncodingFilter extends SetCharacterEncodingFilter {

  // ~ Static fields/initializers ============================================

  private static final Log logger = LogFactory.getLog( PentahoAwareCharacterEncodingFilter.class );

  public static final String INIT_PARAM_ENCODING = "encoding"; //$NON-NLS-1$

  public static final String DEFAULT_CHAR_ENCODING = "UTF-8"; //$NON-NLS-1$

  // ~ Instance fields =======================================================

  // ~ Constructors ==========================================================

  public PentahoAwareCharacterEncodingFilter() {
    super();
  }

  // ~ Methods ===============================================================

  @Override
  protected String selectEncoding( final ServletRequest request ) {
    if ( request instanceof HttpServletRequest ) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String enc =
          httpRequest.getSession( true ).getServletContext().getInitParameter(
            PentahoAwareCharacterEncodingFilter.INIT_PARAM_ENCODING );
      if ( StringUtils.isNotBlank( enc ) ) {
        if ( PentahoAwareCharacterEncodingFilter.logger.isDebugEnabled() ) {
          PentahoAwareCharacterEncodingFilter.logger.debug( Messages.getInstance().getString(
            "PentahoAwareCharacterEncodingFilter.ENCODING_IN_CTX", enc ) ); //$NON-NLS-1$
        }
        return enc;
      }
    }
    String enc = super.selectEncoding( request );
    if ( StringUtils.isNotBlank( enc ) ) {
      if ( PentahoAwareCharacterEncodingFilter.logger.isDebugEnabled() ) {
        PentahoAwareCharacterEncodingFilter.logger.debug( Messages.getInstance().getString(
          "PentahoAwareCharacterEncodingFilter.ENCODING_IN_FILTER_INIT", enc ) ); //$NON-NLS-1$
      }
      return enc;
    } else {
      if ( PentahoAwareCharacterEncodingFilter.logger.isWarnEnabled() ) {
        PentahoAwareCharacterEncodingFilter.logger.warn( Messages.getInstance().getString(
            "PentahoAwareCharacterEncodingFilter.COULD_NOT_FIND_ENCODING", //$NON-NLS-1$
            PentahoAwareCharacterEncodingFilter.DEFAULT_CHAR_ENCODING ) );
      }
      return PentahoAwareCharacterEncodingFilter.DEFAULT_CHAR_ENCODING;
    }
  }

}
