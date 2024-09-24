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

package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.webservices.VersionSummaryDto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

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
