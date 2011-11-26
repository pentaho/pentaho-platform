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
package org.pentaho.platform.datasource;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;

@XmlRootElement
public class GenericDatasourceInfo implements IGenericDatasourceInfo {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  String name;
  
  String id;

  String type;

  public GenericDatasourceInfo() {
    super();
    // TODO Auto-generated constructor stub
  }

  public GenericDatasourceInfo(String name, String id, String type) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
  }
  @Override
  public String getName() {
    return name;
  }
  
  
  @Override
  public String getId() {
    return id;
  }
  
  @Override
  public String getType() {
    return type;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "GenericDatasourceInfo [name=" + name + "id=" + id + ", type=" + type + "]";
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }
  @Override
  public void setName(String name) {
    this.name = name;
  }

}
