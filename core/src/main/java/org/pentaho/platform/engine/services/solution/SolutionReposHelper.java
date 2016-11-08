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
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.ISolutionAttributeContributor;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;

import java.util.ArrayList;
import java.util.List;

public class SolutionReposHelper {

  public static final String TREE_NODE_NAME = "tree"; //$NON-NLS-1$

  public static final String ENTRY_NODE_NAME = "entry"; //$NON-NLS-1$

  public static final String TYPE_ATTR_NAME = "type"; //$NON-NLS-1$

  public static final String NAME_ATTR_NAME = "name"; //$NON-NLS-1$

  public static final String DIRECTORY_ATTR = "directory"; //$NON-NLS-1$

  public static final String FILE_ATTR = "file"; //$NON-NLS-1$

  public static final String BRANCH_NODE_NAME = "branch"; //$NON-NLS-1$

  public static final String BRANCH_TEXT_NODE_NAME = "branchText"; //$NON-NLS-1$

  public static final String ID_ATTR_NAME = "id"; //$NON-NLS-1$

  public static final String IS_DIR_ATTR_NAME = "isDir"; //$NON-NLS-1$

  public static final String LEAF_NODE_NAME = "leaf"; //$NON-NLS-1$

  public static final String LEAF_TEXT_NODE_NAME = "leafText"; //$NON-NLS-1$

  public static final String LINK_NODE_NAME = "link"; //$NON-NLS-1$

  public static final String PATH_TEXT_NODE_NAME = "path"; //$NON-NLS-1$

  public static final ISolutionFilter KEEP_ALL_FILTER = new ISolutionFilter() {
    public boolean keepFile( ISolutionFile solutionFile, int actionOperation ) {
      return solutionFile != null;
    }
  };

  public static final ISolutionFilter DEFAULT_FILTER = new ISolutionFilter() {
    public boolean keepFile( ISolutionFile solutionFile, int actionOperation ) {
      return solutionFile != null && !ignoreFile( solutionFile );
    }
  };

  public static final ISolutionAttributeContributor ADD_NOTHING_CONTRIBUTOR = new ISolutionAttributeContributor() {
    public void contributeAttributes( ISolutionFile solutionFile, Element childNode ) {

    }
  };

  static List ignoreDirectories = new ArrayList();

  static List ignoreFiles = new ArrayList();

  static {
    // all names in lower case to work correctly
    SolutionReposHelper.ignoreDirectories.add( "cvs" ); //$NON-NLS-1$
    SolutionReposHelper.ignoreDirectories.add( "system" ); //$NON-NLS-1$
    SolutionReposHelper.ignoreDirectories.add( ".svn" ); //$NON-NLS-1$

    SolutionReposHelper.ignoreFiles.add( ".cvsignore" ); //$NON-NLS-1$
  }

  private SolutionReposHelper() {
  }

  public static boolean ignoreFile( final ISolutionFile solutionFile ) {
    if ( solutionFile.isDirectory() ) {
      return ignoreDirectories.contains( solutionFile.getFileName().toLowerCase() );
    }
    return ignoreFiles.contains( solutionFile.getFileName().toLowerCase() );
  }

  public static boolean ignoreFile( final String fileName ) {
    return ( SolutionReposHelper.ignoreFiles.contains( fileName.toLowerCase() ) );
  }

  public static boolean ignoreDirectory( final String dirName ) {
    return ( SolutionReposHelper.ignoreDirectories.contains( dirName.toLowerCase() ) );
  }

  public static boolean isActionSequence( final ISolutionFile solutionFile ) {
    String fileName = solutionFile.getFileName();
    return ( ( fileName != null ) ? fileName.toLowerCase().endsWith( ".xaction" ) : false ); //$NON-NLS-1$
  }

  public static Document getActionSequences( final ISolutionFile targetFile, final int actionOperation ) {
    return SolutionReposHelper.getActionSequences( targetFile, SolutionReposHelper.ADD_NOTHING_CONTRIBUTOR,
        actionOperation );
  }

  public static Document getActionSequences( final ISolutionFile targetFile,
      final ISolutionAttributeContributor contributor, final int actionOperation ) {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement( SolutionReposHelper.TREE_NODE_NAME );
    SolutionReposHelper.processSolutionTree( root, targetFile, new ISolutionFilter() {
      public boolean keepFile( final ISolutionFile solutionFile, final int actOperation ) {
        return ( solutionFile.isDirectory() || SolutionReposHelper.isActionSequence( solutionFile ) )
            && DEFAULT_FILTER.keepFile( solutionFile, actionOperation );
      }
    }, contributor, actionOperation );
    return ( document );
  }

  public static Document processSolutionTree( final ISolutionFile targetFile, final int actionOperation ) {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement( SolutionReposHelper.TREE_NODE_NAME );
    SolutionReposHelper.processSolutionTree( root, targetFile, null, actionOperation );
    return ( document );
  }

  public static void processSolutionTree( final Element parentNode, final ISolutionFile targetFile,
      final int actionOperation ) {
    SolutionReposHelper.processSolutionTree( parentNode, targetFile, null, actionOperation );
  }

  public static void processSolutionStructure( final Element parentNode, final ISolutionFile targetFile,
      final int actionOperation ) {
    SolutionReposHelper.processSolutionStructure( parentNode, targetFile, null, actionOperation );
  }

  public static void processSolutionTree( final Element parentNode, final ISolutionFile targetFile,
      final ISolutionFilter solutionFilter, final int actionOperation ) {
    SolutionReposHelper.processSolutionTree( parentNode, targetFile, solutionFilter,
        SolutionReposHelper.ADD_NOTHING_CONTRIBUTOR, actionOperation );
  }

  public static void processSolutionTree( final Element parentNode, final ISolutionFile targetFile,
      final ISolutionFilter solutionFilter, final ISolutionAttributeContributor contributor,
      final int actionOperation ) {
    ISolutionFilter sFilter = solutionFilter == null ? DEFAULT_FILTER : solutionFilter;
    if ( targetFile != null ) {
      if ( sFilter.keepFile( targetFile, actionOperation ) ) {
        if ( targetFile.isDirectory() ) {
          Element childNode =
              parentNode.addElement( SolutionReposHelper.BRANCH_NODE_NAME ).addAttribute(
                  SolutionReposHelper.ID_ATTR_NAME, targetFile.getFullPath() ).addAttribute(
                  SolutionReposHelper.IS_DIR_ATTR_NAME, "true" ); //$NON-NLS-1$
          contributor.contributeAttributes( targetFile, childNode );
          if ( targetFile.isRoot() ) {
            childNode.addElement( SolutionReposHelper.BRANCH_TEXT_NODE_NAME ).setText( "/" ); //$NON-NLS-1$
          } else {
            childNode.addElement( SolutionReposHelper.BRANCH_TEXT_NODE_NAME ).setText( targetFile.getFileName() );
          }
          ISolutionFile[] files = targetFile.listFiles();
          for ( ISolutionFile file : files ) {
            SolutionReposHelper.processSolutionTree( childNode, file, sFilter, contributor, actionOperation );
          }
        } else {
          Element childNode =
              parentNode.addElement( SolutionReposHelper.LEAF_NODE_NAME ).addAttribute(
                  SolutionReposHelper.IS_DIR_ATTR_NAME, "false" ); //$NON-NLS-1$
          contributor.contributeAttributes( targetFile, childNode );
          childNode.addElement( SolutionReposHelper.LEAF_TEXT_NODE_NAME ).setText( targetFile.getFileName() );
          childNode.addElement( SolutionReposHelper.PATH_TEXT_NODE_NAME ).setText( targetFile.getFullPath() );
          childNode.addElement( SolutionReposHelper.LINK_NODE_NAME ).setText( "#" ); //$NON-NLS-1$
        }
      }
    }
  }

  // //
  // Only analyzer uses this.. is this necessary?
  private static final ThreadLocal threadSolutionRepositories = new ThreadLocal();

  public static void setSolutionRepositoryThreadVariable( Object repository ) {
    SolutionReposHelper.threadSolutionRepositories.set( repository );
  }

  public static Object getSolutionRepositoryThreadVariable() {
    return SolutionReposHelper.threadSolutionRepositories.get();
  }

  //
  // //

  public static void processSolutionStructure( final Element parentNode, final ISolutionFile targetFile,
      final ISolutionFilter solutionFilter, final int actionOperation ) {
    SolutionReposHelper.processSolutionStructure( parentNode, targetFile, solutionFilter,
        SolutionReposHelper.ADD_NOTHING_CONTRIBUTOR, actionOperation );
  }

  public static void processSolutionStructure( final Element parentNode, final ISolutionFile targetFile,
      final ISolutionFilter solutionFilter, final ISolutionAttributeContributor contributor,
      final int actionOperation ) {
    ISolutionFilter sFilter = solutionFilter == null ? DEFAULT_FILTER : solutionFilter;
    if ( targetFile.isDirectory() ) {
      if ( !SolutionReposHelper.ignoreDirectories.contains( targetFile.getFileName() )
          && sFilter.keepFile( targetFile, actionOperation ) ) {
        Element childNode =
            parentNode.addElement( SolutionReposHelper.ENTRY_NODE_NAME ).addAttribute(
                SolutionReposHelper.TYPE_ATTR_NAME, SolutionReposHelper.DIRECTORY_ATTR ).addAttribute(
                SolutionReposHelper.NAME_ATTR_NAME, targetFile.getFileName() );
        contributor.contributeAttributes( targetFile, childNode );
        ISolutionFile[] files = targetFile.listFiles();
        for ( ISolutionFile file : files ) {
          SolutionReposHelper.processSolutionStructure( childNode, file, actionOperation );
        }
      }
    } else {
      if ( !SolutionReposHelper.ignoreFiles.contains( targetFile.getFileName() )
          && sFilter.keepFile( targetFile, actionOperation ) ) {
        Element childNode =
            parentNode.addElement( SolutionReposHelper.ENTRY_NODE_NAME ).addAttribute(
                SolutionReposHelper.TYPE_ATTR_NAME, SolutionReposHelper.FILE_ATTR ).addAttribute(
                SolutionReposHelper.NAME_ATTR_NAME, targetFile.getFileName() );
        contributor.contributeAttributes( targetFile, childNode );
      }
    }
  }

}
