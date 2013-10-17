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

package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.core.util.PropertyLookupParser;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.config.DefaultConfiguration;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Creation-Date: 05.07.2007, 19:48:16
 * 
 * @author Thomas Morgner
 */
public class PentahoReportConfiguration extends PropertyLookupParser implements Configuration {
  private static final long serialVersionUID = -1191667017348135395L;

  private DefaultConfiguration config;

  public PentahoReportConfiguration() {
    setOpeningBraceChar( '(' );
    setClosingBraceChar( ')' );
    config = new DefaultConfiguration();
    final ISystemSettings cfg = PentahoSystem.getSystemSettings();
    if ( cfg == null ) {
      return;
    }
    final List reportSettings = cfg.getSystemSettings( "report-config/entry" ); //$NON-NLS-1$
    for ( int i = 0; i < reportSettings.size(); i++ ) {
      final Element element = (Element) reportSettings.get( i );
      final Attribute name = element.attribute( "name" ); //$NON-NLS-1$
      final Attribute value = element.attribute( "value" ); //$NON-NLS-1$
      if ( ( name != null ) && ( value != null ) ) {
        this.config.setConfigProperty( name.getValue(), translateAndLookup( value.getValue() ) );
      }
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    final PentahoReportConfiguration cfg = (PentahoReportConfiguration) super.clone();
    cfg.config = (DefaultConfiguration) cfg.clone();
    return cfg;
  }

  public Iterator findPropertyKeys( final String prefix ) {
    return config.findPropertyKeys( prefix );
  }

  public Enumeration getConfigProperties() {
    return config.getConfigProperties();
  }

  public String getConfigProperty( final String key ) {
    return getConfigProperty( key, null );
  }

  public String getConfigProperty( final String key, final String defaultvalue ) {
    return config.getConfigProperty( key, defaultvalue );
  }

  @Override
  protected String lookupVariable( final String property ) {
    final IApplicationContext context = PentahoSystem.getApplicationContext();
    if ( context != null ) {
      final String contextParam = context.getProperty( property );
      if ( contextParam != null ) {
        return contextParam;
      }
    }

    final IParameterProvider globalParameters = PentahoSystem.getGlobalParameters();
    if ( globalParameters != null ) {
      final String globalParam = globalParameters.getStringParameter( property, null );
      if ( globalParam != null ) {
        return globalParam;
      }
    }

    final String systemSetting = PentahoSystem.getSystemSetting( property, null );
    if ( systemSetting != null ) {
      return systemSetting;
    }

    if ( context != null ) {
      if ( "base-url".equals( property ) ) { //$NON-NLS-1$
        return context.getFullyQualifiedServerURL();
      }
    }
    return null;
  }
}
