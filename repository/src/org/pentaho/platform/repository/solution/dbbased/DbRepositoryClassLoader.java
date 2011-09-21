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
 * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Mar 29, 2006 
 * @author wseyler
 */
package org.pentaho.platform.repository.solution.dbbased;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.solution.SolutionRepositoryBase;

public class DbRepositoryClassLoader extends ClassLoader {
  protected static final Log logger = LogFactory.getLog(SolutionRepositoryBase.class);

  private static final HashMap resourceMap = new HashMap();

  String path;

  DbBasedSolutionRepository repository;

  public DbRepositoryClassLoader(final String inPath, final DbBasedSolutionRepository inRepository) {
    path = inPath;
    repository = inRepository;
  }

  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An input stream for reading the resource, or <code>null</code> if the resource could not be found 
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
    String key = path + ISolutionRepository.SEPARATOR + name;
    byte[] bytes = (byte[]) DbRepositoryClassLoader.resourceMap.get(key);
    if (bytes == null) {
      try {
        bytes = IOUtils.toByteArray(ActionSequenceResource.getInputStream(key, null));
        DbRepositoryClassLoader.resourceMap.put(key, bytes);
      } catch (IOException ignored) {
        // This situation indicates the resource could not be found. This is a common and correct situation 
        // and this exception should be ignored.
        if (DbRepositoryClassLoader.logger.isTraceEnabled()) {
          DbRepositoryClassLoader.logger.trace(Messages.getInstance().getString("DbRepositoryClassLoader.RESOURCE_NOT_FOUND", name)); //$NON-NLS-1$
        }

        // Return null to indicate that the resource could not be found (and this is ok) 
        return null;
      }
    }
    return new ByteArrayInputStream(bytes);
  }

  public static void clearResourceCache() {
    DbRepositoryClassLoader.resourceMap.clear();
  }

}
