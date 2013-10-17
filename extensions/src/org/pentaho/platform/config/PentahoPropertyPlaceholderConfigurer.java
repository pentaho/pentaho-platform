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

package org.pentaho.platform.config;

import org.pentaho.platform.api.engine.ISystemConfig;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * User: nbaker Date: 4/2/13
 */
public class PentahoPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
  private ISystemConfig config;

  public PentahoPropertyPlaceholderConfigurer( ISystemConfig config ) {
    if ( config == null ) {
      throw new IllegalArgumentException( "ISystemConfig was null" );
    }
    this.config = config;
    this.setIgnoreUnresolvablePlaceholders( true );
  }

  @Override
  protected String resolvePlaceholder( String placeholder, Properties props ) {
    // placeholder must be in the form of ID.PROP where
    String val = this.resolveValue( placeholder );
    if ( val == null ) {
      val = super.resolvePlaceholder( placeholder, props );
    }
    return val;
  }

  private String resolveValue( String placeholder ) {
    return config.getProperty( placeholder );
  }

}
