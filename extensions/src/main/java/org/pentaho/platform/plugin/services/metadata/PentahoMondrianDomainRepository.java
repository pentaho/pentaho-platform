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


package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

public class PentahoMondrianDomainRepository extends PentahoMetadataDomainRepository implements
    IMetadataDomainRepository {

  private static final Log logger = LogFactory.getLog( PentahoMondrianDomainRepository.class );

  public PentahoMondrianDomainRepository( IUnifiedRepository repository ) {
    super( repository );
    // TODO Auto-generated constructor stub
  }

}
