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

package org.pentaho.platform.util.versionchecker;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class PentahoVersionCheckReflectHelper {

  public static boolean isVersionCheckerAvailable() {
    try {
      Class.forName( "org.pentaho.versionchecker.VersionChecker" ); //$NON-NLS-1$
      return true;
    } catch ( ClassNotFoundException e ) {
      // ignore
    }
    return false;
  }

  public static List performVersionCheck( final boolean ignoreExistingUpdates, final int versionRequestFlags ) {
    // check to see if jar is loaded before continuing
    if ( PentahoVersionCheckReflectHelper.isVersionCheckerAvailable() ) {
      try {

        // use reflection so anyone can delete the version checker jar without pain

        // PentahoVersionCheckHelper helper = new PentahoVersionCheckHelper();
        Class helperClass =
            Class.forName( "org.pentaho.platform.util.versionchecker.PentahoVersionCheckHelper" ); //$NON-NLS-1$
        Object helper = helperClass.getConstructors()[0].newInstance( new Object[] {} );

        // helper.setIgnoreExistingUpdates(ignoreExistingUpdates);
        Method setIgnoreExistingUpdatesMethod =
            helperClass.getDeclaredMethod( "setIgnoreExistingUpdates", new Class[] { Boolean.TYPE } ); //$NON-NLS-1$
        setIgnoreExistingUpdatesMethod.invoke( helper, new Object[] { new Boolean( ignoreExistingUpdates ) } );

        // helper.setVersionRequestFlags(versionRequestFlags);
        Method setVersionRequestFlagsMethod =
            helperClass.getDeclaredMethod( "setVersionRequestFlags", new Class[] { Integer.TYPE } ); //$NON-NLS-1$
        setVersionRequestFlagsMethod.invoke( helper, new Object[] { new Integer( versionRequestFlags ) } );

        // helper.performUpdate();
        Method performUpdateMethod = helperClass.getDeclaredMethod( "performUpdate", new Class[] {} ); //$NON-NLS-1$
        performUpdateMethod.invoke( helper, new Object[] {} );

        // List results = helper.getResults();
        Method getResultsMethod = helperClass.getDeclaredMethod( "getResults", new Class[] {} ); //$NON-NLS-1$
        List results = (List) getResultsMethod.invoke( helper, new Object[] {} );
        return results;

      } catch ( Exception e ) {
        // ignore errors
      }
    }

    return null;
  }

  public static String logVersionCheck( final List results, final Log logger ) {
    String output = null;
    if ( ( results != null ) && ( results.size() > 0 ) ) {
      String result = results.get( 0 ).toString();
      try {
        Document doc = XmlDom4JHelper.getDocFromString( result, new PentahoEntityResolver() );
        if ( doc != null ) {
          List nodes = doc.selectNodes( "//update" ); //$NON-NLS-1$
          Iterator nodeIter = nodes.iterator();
          while ( nodeIter.hasNext() ) {
            Element updateElement = (Element) nodeIter.next();

            String title = updateElement.attributeValue( "title" ); //$NON-NLS-1$
            String version = updateElement.attributeValue( "version" ); //$NON-NLS-1$
            String type = updateElement.attributeValue( "type" ); //$NON-NLS-1$
            String downloadurl = XmlDom4JHelper.getNodeText( "downloadurl", updateElement ); //$NON-NLS-1$
            if ( downloadurl != null ) {
              downloadurl = downloadurl.trim();
            }
            logger.info( Messages.getInstance().getString(
                "VersionCheck.UPDATE_MESSAGE", title, version, type, downloadurl ) ); //$NON-NLS-1$
          }

          nodes = doc.selectNodes( "//error" ); //$NON-NLS-1$
          nodeIter = nodes.iterator();
          while ( nodeIter.hasNext() ) {
            Element errorElement = (Element) nodeIter.next();
            String message = errorElement.getText();
            logger.info( Messages.getInstance().getString( "VersionCheck.ERROR_MESSAGE", message ) ); //$NON-NLS-1$
          }
        }
        output = result;
      } catch ( Exception e ) {
        output =
            "<vercheck><error><[!CDATA[" + Messages.getInstance().getString( "VersionCheck.ERROR_MESSAGE", e.getMessage() ) + "]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } else {
      output =
          "<vercheck><error><[!CDATA[" + Messages.getInstance().getString( "VersionCheck.NO_RESULT_MESSAGE" ) + "]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return output;
  }
}
