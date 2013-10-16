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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

/**
 * Converts {@code RepositoryFile} into JAXB-safe object and vice-versa.
 * 
 * @author mlowery
 */
public class RepositoryFileAdapter extends XmlAdapter<RepositoryFileDto, RepositoryFile> {

  @Override
  public RepositoryFileDto marshal( final RepositoryFile v ) {
    return toFileDto( v );
  }

  public static RepositoryFileDto toFileDto( final RepositoryFile v ) {
    if ( v == null ) {
      return null;
    }
    RepositoryFileDto f = new RepositoryFileDto();
    f.name = v.getName();
    f.path = v.getPath();
    f.hidden = v.isHidden();
    f.createdDate = v.getCreatedDate();
    f.creatorId = v.getCreatorId();
    f.fileSize = v.getFileSize();
    f.description = v.getDescription();
    f.folder = v.isFolder();
    if ( v.getId() != null ) {
      f.id = v.getId().toString();
    }
    f.lastModifiedDate = v.getLastModifiedDate();
    f.locale = v.getLocale();
    f.originalParentFolderPath = v.getOriginalParentFolderPath();
    f.deletedDate = v.getDeletedDate();
    f.lockDate = v.getLockDate();
    f.locked = v.isLocked();
    f.lockMessage = v.getLockMessage();
    f.lockOwner = v.getLockOwner();
    f.title = v.getTitle();
    f.versioned = v.isVersioned();
    if ( v.getVersionId() != null ) {
      f.versionId = v.getVersionId().toString();
    }
    if ( v.getLocalePropertiesMap() != null ) {
      f.localePropertiesMapEntries = new ArrayList<LocaleMapDto>();
      for ( Map.Entry<String, Properties> entry : v.getLocalePropertiesMap().entrySet() ) {

        LocaleMapDto localeMapDto = new LocaleMapDto();
        List<StringKeyStringValueDto> valuesDto = new ArrayList<StringKeyStringValueDto>();

        Properties properties = entry.getValue();
        if ( properties != null ) {
          for ( String propertyName : properties.stringPropertyNames() ) {
            valuesDto.add( new StringKeyStringValueDto( propertyName, properties.getProperty( propertyName ) ) );
          }
        }

        localeMapDto.setLocale( entry.getKey() );
        localeMapDto.setProperties( valuesDto );

        f.localePropertiesMapEntries.add( localeMapDto );
      }
    }

    return f;
  }

  @Override
  public RepositoryFile unmarshal( final RepositoryFileDto v ) {
    return toFile( v );
  }

  public static RepositoryFile toFile( final RepositoryFileDto v ) {
    if ( v == null ) {
      return null;
    }
    RepositoryFile.Builder builder = null;
    if ( v.id != null ) {
      builder = new RepositoryFile.Builder( v.id, v.name );
    } else {
      builder = new RepositoryFile.Builder( v.name );
    }
    if ( v.ownerType != -1 ) {
      new RepositoryFileSid( v.owner, RepositoryFileSid.Type.values()[v.ownerType] );
    }
    if ( v.localePropertiesMapEntries != null ) {
      for ( LocaleMapDto localeMapDto : v.localePropertiesMapEntries ) {

        String locale = localeMapDto.getLocale();
        Properties localeProperties = new Properties();

        if ( localeMapDto.getProperties() != null ) {
          for ( StringKeyStringValueDto keyValueDto : localeMapDto.getProperties() ) {
            localeProperties.put( keyValueDto.getKey(), keyValueDto.getValue() );
          }
        }

        builder.localeProperties( locale, localeProperties );
      }
    }

    return builder.path( v.path ).createdDate( v.createdDate ).creatorId( v.creatorId ).description( v.description )
        .folder( v.folder ).fileSize( v.fileSize ).lastModificationDate( v.lastModifiedDate ).locale( v.locale )
        .lockDate( v.lockDate ).locked( v.locked ).lockMessage( v.lockMessage ).lockOwner( v.lockOwner )
        .title( v.title ).versioned( v.versioned ).versionId( v.versionId ).originalParentFolderPath(
            v.originalParentFolderPath ).deletedDate( v.deletedDate ).hidden( v.hidden ).build();
  }

}
