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
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.VersionSummary;

public class VersionSummaryAdapter extends XmlAdapter<VersionSummaryDto, VersionSummary> {

  @Override
  public VersionSummaryDto marshal( final VersionSummary v ) {
    VersionSummaryDto s = new VersionSummaryDto();
    s.id = v.getId().toString();
    s.author = v.getAuthor();
    s.date = v.getDate();
    s.message = v.getMessage();
    s.versionedFileId = v.getVersionedFileId().toString();
    s.labels = v.getLabels();
    s.aclOnlyChange = v.isAclOnlyChange();
    return s;
  }

  @Override
  public VersionSummary unmarshal( final VersionSummaryDto v ) {
    return new VersionSummary( v.id, v.versionedFileId, v.aclOnlyChange, v.date, v.author, v.message, v.labels );
  }

}
