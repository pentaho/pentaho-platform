/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
