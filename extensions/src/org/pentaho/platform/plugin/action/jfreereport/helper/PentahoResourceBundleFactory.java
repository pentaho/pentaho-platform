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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * The resource-bundle factory is responsible for loading referenced resourcebundles. The default action is to load
 * these bundles using the standard JDK methods. If no bundle-name is given, the default name for the xaction's assigned
 * resource-bundle is used instead.
 */
public class PentahoResourceBundleFactory implements ResourceBundleFactory {
  private static final long serialVersionUID = -1555502100120929073L;

  private String path;

  private String baseName;

  private ClassLoader loader;

  public PentahoResourceBundleFactory( final String inPath, final String inBaseName, final IPentahoSession inSession ) {
    path = inPath;
    baseName = inBaseName;
    loader = getClassLoader( path );
  }

  private ClassLoader getClassLoader( final String path ) {
    File localeDir = new File( PentahoSystem.getApplicationContext().getSolutionPath( path ) );
    try {
      URLClassLoader loader = new URLClassLoader( new URL[] { localeDir.toURL() }, null );
      return loader;
    } catch ( Exception e ) {
      Logger.error( getClass().getName(), Messages.getInstance().getErrorString(
          "PentahoResourceBundleFactory.ERROR_0024_CREATING_CLASSLOADER" ), e ); //$NON-NLS-1$
    }
    return null;
  }

  public Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  public ResourceBundle getResourceBundle( String resourceName ) {
    if ( resourceName == null ) {
      resourceName = baseName;
    }
    try {
      return ResourceBundle.getBundle( resourceName, getLocale(), loader );
    } catch ( Exception e ) {
      Logger.error( getClass().getName(), Messages.getInstance().getErrorString(
          "JFreeReport.ERROR_0024_COULD_NOT_READ_PROPERTIES", path + File.separator + baseName ), e ); //$NON-NLS-1$
    }
    return null;
  }

  public TimeZone getTimeZone() {
    return TimeZone.getDefault();
  }

}
