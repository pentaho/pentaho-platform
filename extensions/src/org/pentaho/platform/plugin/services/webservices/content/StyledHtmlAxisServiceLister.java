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

package org.pentaho.platform.plugin.services.webservices.content;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.ui.Theme;
import org.pentaho.platform.api.ui.ThemeResource;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContext;
import java.io.OutputStream;

public class StyledHtmlAxisServiceLister extends HtmlAxisServiceLister {

  private static final long serialVersionUID = 6592498636085258801L;

  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out )
    throws Exception {

    // write out the style sheet and the HTML document

    out.write( "<html>\n<head>".getBytes() ); //$NON-NLS-1$

    final IPentahoSession session = PentahoSessionHolder.getSession();
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, session );
    String activeThemeName;
    if ( session == null || settingsService == null ) {
      activeThemeName = PentahoSystem.getSystemSetting( "default-activeThemeName", "onyx" );
    } else {
      activeThemeName = StringUtils.defaultIfEmpty( (String) session.getAttribute( "pentaho-user-activeThemeName" ), settingsService
        .getUserSetting( "pentaho-user-activeThemeName", PentahoSystem.getSystemSetting( "default-activeThemeName", "onyx" ) )
          .getSettingValue() );
    }

    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class, null );
    Theme theme = themeManager.getSystemTheme( activeThemeName );

    final ServletContext servletContext = (ServletContext) PentahoSystem.getApplicationContext().getContext();
    if ( servletContext != null ) {
      for ( ThemeResource res : theme.getResources() ) {
        if ( res.getLocation().endsWith( ".css" ) ) {
          out.write( ( "<link rel=\"stylesheet\" href=\"" + PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + theme.getThemeRootDir() + res.getLocation() + "\">" ).getBytes() );
        }
      }
    }

    out.write( "</head>\n<body>\n".getBytes() ); //$NON-NLS-1$

    // get the list of services from the core ListServices
    super.createContent( axisConfiguration, context, out );

    out.write( "\n</html>\n".getBytes() ); //$NON-NLS-1$

  }

}
