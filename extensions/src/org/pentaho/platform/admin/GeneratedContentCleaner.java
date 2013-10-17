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

package org.pentaho.platform.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 * 
 * @created March 18, 2013
 * @author Michael D'Amour
 * 
 */

public class GeneratedContentCleaner implements IAction {

  private static final Log logger = LogFactory.getLog( GeneratedContentCleaner.class );

  // 180 days
  private long age = ( 180L * 24L * 60L * 60L * 1000L );

  private IUnifiedRepository repository;

  /**
   * This class permanently deletes generated files which are older than the provided age. Generated content is
   * determined by using file metadata. If a file contains the "RESERVEDMAPKEY_LINEAGE_ID" key in the metadata, then
   * when know it is generated content (created from scheduler or run in background)
   */
  public GeneratedContentCleaner() {
    repository = PentahoSystem.get( IUnifiedRepository.class );
  }

  private void findGeneratedContent( List<RepositoryFile> generatedContentList, RepositoryFileTree parent ) {
    RepositoryFile parentFile = parent.getFile();
    if ( !parentFile.isFolder() ) {
      long createTime = parentFile.getCreatedDate().getTime();
      if ( createTime <= ( System.currentTimeMillis() - ( age * 1000 ) ) ) {
        // now check metadata for RESERVEDMAPKEY_LINEAGE_ID (all generated content has)
        Map<String, Serializable> metadata = repository.getFileMetadata( parentFile.getId() );
        if ( metadata.containsKey( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) {
          generatedContentList.add( parentFile );
        }
      }
    } else {
      for ( RepositoryFileTree child : parent.getChildren() ) {
        findGeneratedContent( generatedContentList, child );
      }
    }
  }

  /*
   * This method performs the actual work of the GeneratedContentCleaner by calling deleteFile with 'true' passed down
   * for the 'permanent' flag.
   * 
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.action.IAction#execute()
   */
  public void execute() throws Exception {
    // scan the repository for all files with a RESERVEDMAPKEY_LINEAGE_ID
    RepositoryFileTree tree = repository.getTree( ClientRepositoryPaths.getRootFolderPath(), -1, null, false );
    ArrayList<RepositoryFile> generatedContentList = new ArrayList<RepositoryFile>();
    findGeneratedContent( generatedContentList, tree );
    for ( RepositoryFile deleteMe : generatedContentList ) {
      repository.deleteFile( deleteMe.getId(), true, GeneratedContentCleaner.class.getName() );
      logger.info( "GeneratedContentCleaner deleting: " + deleteMe.getPath() );
    }
  }

  /**
   * @return the age in milliseconds that the cleaner will use to determine if content should be removed
   */
  public long getAge() {
    return age;
  }

  /**
   * This method sets the age in milliseconds that the cleaner will use for checking if content should be removed
   * 
   * @param age
   *          the age in milliseconds
   */
  public void setAge( long age ) {
    this.age = age;
  }

}
