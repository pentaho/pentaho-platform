/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

/**
 * @author Marc Batchelor
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class JcrAclNodeHelperCallTester extends JcrAclNodeHelper {

  private int aclNodeCallCounter;
  
  public JcrAclNodeHelperCallTester( IUnifiedRepository unifiedRepository ) {
    super( unifiedRepository );
  }

  protected void resetAclNodeCallCounter() {
    this.aclNodeCallCounter = 0;
  }
  
  protected int getAclNodeCallCounter() {
    return this.aclNodeCallCounter;
  }

  protected RepositoryFile getAclNode( final RepositoryFile file ) {
    this.aclNodeCallCounter++;
    return super.getAclNode( file );
  }
  
}
