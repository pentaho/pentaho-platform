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


package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.webservices.VersionSummaryDto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class VersionSummaryAdapter extends XmlAdapter<VersionSummaryDto, VersionSummary> {

  @Override
  public VersionSummaryDto marshal( final VersionSummary v ) {
    VersionSummaryDto s = new VersionSummaryDto();
    s.setId( v.getId().toString() );
    s.setAuthor( v.getAuthor() );
    s.setDate( v.getDate() );
    s.setMessage( v.getMessage() );
    s.setVersionedFileId( v.getVersionedFileId().toString() );
    s.setLabels( v.getLabels() );
    s.setAclOnlyChange( v.isAclOnlyChange() );
    return s;
  }

  @Override
  public VersionSummary unmarshal( final VersionSummaryDto v ) {
    return new VersionSummary( v.getId(), v.getVersionedFileId(), v.isAclOnlyChange(),
            v.getDate(), v.getAuthor(), v.getMessage(), v.getLabels() );
  }

}
