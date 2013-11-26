/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.solution;

import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFileMetaProvider;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.InputStream;

/**
 * A generic meta adapter for the state files created by SolutionRepoLoadContentGenerator and read by
 * SolutionRepoLoadContentGenerator
 * 
 * @author jamesdixon
 * 
 */
public class SolutionRepoFileInfo implements ISolutionFileMetaProvider {

  private ILogger logger;

  public void setLogger( ILogger logger ) {
    this.logger = logger;
  }

  public IFileInfo getFileInfo( ISolutionFile solutionFile, InputStream in ) {
    try {
      Document doc = XmlDom4JHelper.getDocFromStream( in );

      IFileInfo info = new FileInfo();
      Node node = doc.selectSingleNode( "state-file/documentation/author" ); //$NON-NLS-1$
      if ( node != null ) {
        info.setAuthor( node.getText() );
      } else {
        info.setAuthor( "" ); //$NON-NLS-1$
      }
      info.setDisplayType( "HTML" ); //$NON-NLS-1$
      node = doc.selectSingleNode( "state-file/documentation/title" ); //$NON-NLS-1$
      if ( node != null ) {
        info.setTitle( node.getText() );
      } else {
        info.setTitle( "" ); //$NON-NLS-1$
      }
      node = doc.selectSingleNode( "state-file/documentation/description" ); //$NON-NLS-1$
      if ( node != null ) {
        info.setDescription( node.getText() );
      } else {
        info.setDescription( "" ); //$NON-NLS-1$
      }
      node = doc.selectSingleNode( "state-file/documentation/icon" ); //$NON-NLS-1$
      if ( node != null ) {
        info.setIcon( node.getText() );
      } else {
        info.setIcon( "" ); //$NON-NLS-1$
      }
      return info;
    } catch ( Exception e ) {
      if ( logger != null ) {
        logger.error( getClass().toString(), e );
      }
      return null;
    }
  }

}
