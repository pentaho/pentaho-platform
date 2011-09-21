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
 */
package org.pentaho.platform.api.repository2.unified.data.node;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

public class NodeRepositoryFileData implements IRepositoryFileData {

  private static final long serialVersionUID = 3986247263739435232L;

  private DataNode node;

  public NodeRepositoryFileData(DataNode node) {
    super();
    this.node = node;
  }

  public DataNode getNode() {
    return node;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.IRepositoryFileData#getDataSize()
   */
  @Override
  public long getDataSize() {
    return 0;
  }

}
