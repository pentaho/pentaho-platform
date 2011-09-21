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
package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import java.util.List;

public class RepositoryFileAclAceDto implements Serializable{
  String recipient;

  /**
   * RepositoryFileSid.Type enum.
   */
  int recipientType = -1;

  /**
   * RepositoryFilePermission enum.
   */
  List<Integer> permissions;

  public RepositoryFileAclAceDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public int getRecipientType() {
    return recipientType;
  }

  public void setRecipientType(int recipientType) {
    this.recipientType = recipientType;
  }

  public List<Integer> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<Integer> permissions) {
    this.permissions = permissions;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "RepositoryFileAclAceDto [recipient=" + recipient + ", recipientType=" + recipientType + ", permissions="
        + permissions + "]";
  }
}
