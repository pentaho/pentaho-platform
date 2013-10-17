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

package org.pentaho.platform.plugin.services.importer;

import java.util.ArrayList;
import java.util.List;

public class SolutionFileImportHelper {

  private List<String> hiddenExtensionList;
  private List<String> approvedExtensionList;

  SolutionFileImportHelper( List<String> hiddenExtensionList, List<String> approvedExtensionList ) {
    this.hiddenExtensionList = new ArrayList<String>( hiddenExtensionList );
    this.approvedExtensionList = new ArrayList<String>( approvedExtensionList );
  }

  public boolean isInApprovedExtensionList( String fileName ) {
    boolean isInTheApprovedExtensionList = false;
    for ( String extension : approvedExtensionList ) {
      if ( fileName.endsWith( extension ) ) {
        isInTheApprovedExtensionList = true;
        break;
      }
    }
    return isInTheApprovedExtensionList;
  }

  public boolean isInHiddenList( String fileName ) {
    boolean isInTheHiddenList = false;
    for ( String extension : hiddenExtensionList ) {
      if ( fileName.endsWith( extension ) ) {
        isInTheHiddenList = true;
        break;
      }
    }
    return isInTheHiddenList;
  }
}
