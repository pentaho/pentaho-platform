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
 */
package org.pentaho.platform.engine.services.solution;

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;

public class PermissionFilter implements IFileFilter {

  ISolutionRepository repository = null;

  public PermissionFilter(final ISolutionRepository repository) {
    this.repository = repository;
  }

  public boolean accept(final ISolutionFile file) {
    if (repository.hasAccess(file, IAclHolder.ACCESS_TYPE_WRITE)
        || repository.hasAccess(file, IAclHolder.ACCESS_TYPE_UPDATE)) {
      return true;
    }
    return false;
  }

}
