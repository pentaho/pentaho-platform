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

import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository2.unified.RepositoryUtils;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CachingPentahoMetadataDomainRepositoryTest extends PentahoMetadataDomainRepositoryIT {
  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository ) {
    return new CachingPentahoMetadataDomainRepository( repository );
  }

  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository,
      final RepositoryUtils repositoryUtils, final XmiParser xmiParser, final LocalizationUtil localizationUtil ) {
    return new CachingPentahoMetadataDomainRepository( repository, repositoryUtils, xmiParser, localizationUtil );
  }
}
