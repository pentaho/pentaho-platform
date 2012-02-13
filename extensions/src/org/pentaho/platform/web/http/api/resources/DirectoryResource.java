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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Apr 12, 2011 
 * @author wseyler
 *
 */
package org.pentaho.platform.web.http.api.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

import static javax.ws.rs.core.MediaType.WILDCARD;

/**
 * A class that can manipulate directories
 * @author wseyler
 *
 */

@Path("/repo/dirs/")
public class DirectoryResource extends AbstractJaxRSResource {
  protected DefaultUnifiedRepositoryWebService repoWs;
  
  public DirectoryResource() {
    super();
    
    repoWs = new DefaultUnifiedRepositoryWebService();
  }

  @PUT
  @Path("{pathId : .+}")
  @Consumes( { WILDCARD })
  public Response createDirs(@PathParam("pathId") String pathId) {
      String path = FileResource.idToPath(pathId);
      String[] folders = path.split("[" + FileResource.PATH_SEPERATOR + "]");  //$NON-NLS-1$//$NON-NLS-2$
      RepositoryFileDto parentDir = repoWs.getFile(FileResource.PATH_SEPERATOR);
      for (String folder : folders) {
        String currentFolderPath = (parentDir.getPath() + FileResource.PATH_SEPERATOR + folder).substring(1);
        if (!currentFolderPath.startsWith(FileResource.PATH_SEPERATOR)) {
          currentFolderPath = FileResource.PATH_SEPERATOR + currentFolderPath;
        }
        RepositoryFileDto currentFolder = repoWs.getFile(currentFolderPath);
        if (currentFolder == null) {
          currentFolder = new RepositoryFileDto();
          currentFolder.setFolder(true);
          currentFolder.setName(folder);
          currentFolder.setPath(parentDir.getPath() + FileResource.PATH_SEPERATOR + folder);
          currentFolder = repoWs.createFolder(parentDir.getId(), currentFolder, currentFolderPath);
        }
        parentDir = currentFolder;
      }
    return Response.ok().build();    
  }
}
