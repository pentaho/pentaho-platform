/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
