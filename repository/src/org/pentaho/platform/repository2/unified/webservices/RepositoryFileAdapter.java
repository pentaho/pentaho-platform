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
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

/**
 * Converts {@code RepositoryFile} into JAXB-safe object and vice-versa.
 *
 * @author mlowery
 */
public class RepositoryFileAdapter extends XmlAdapter<RepositoryFileDto, RepositoryFile> {
  private static DefaultUnifiedRepositoryWebService repoWs;
  private Set<String> membersSet;
  private boolean exclude;
  private boolean includeAcls;

  public RepositoryFileAdapter() {
    this.exclude = false;
    this.includeAcls = false;
    this.membersSet = null;
  }

  public RepositoryFileAdapter( RepositoryRequest repositoryRequest ) {
    if ( repositoryRequest.getExcludeMemberSet() != null && !repositoryRequest.getExcludeMemberSet().isEmpty() ) {
      this.exclude = true;
      this.membersSet = repositoryRequest.getExcludeMemberSet();
    } else {
      this.exclude = false;
      this.membersSet = repositoryRequest.getIncludeMemberSet();
    }
    this.includeAcls = repositoryRequest.isIncludeAcls();
  }

  public RepositoryFileAdapter( Set<String> membersSet, boolean exclude, boolean includeAcls ) {
    this.exclude = exclude;
    this.membersSet = membersSet;
    this.includeAcls = includeAcls;
  }

  @Override
  public RepositoryFileDto marshal( final RepositoryFile v ) {
    return toFileDto( v );
  }

  private boolean include( String key, Set<String> set, boolean exclude ) {
    return !exclude && ( set == null || set.contains( key ) ) || ( exclude && !set.contains( key ) );
  }

  public RepositoryFileDto toFileDto( final RepositoryFile v ) {
    if ( v == null ) {
      return null;
    }
    RepositoryFileDto f = new RepositoryFileDto();
    if ( include( "name", membersSet, exclude ) ) {
      f.name = v.getName();
    }
    if ( include( "path", membersSet, exclude ) ) {
      f.path = v.getPath();
    }
    if ( include( "hidden", membersSet, exclude ) ) {
      f.hidden = v.isHidden();
    }
    if ( include( "createDate", membersSet, exclude ) ) {
      f.createdDate = v.getCreatedDate();
    }
    if ( include( "creatorId", membersSet, exclude ) ) {
      f.creatorId = v.getCreatorId();
    }
    if ( include( "fileSize", membersSet, exclude ) ) {
      f.fileSize = v.getFileSize();
    }
    if ( include( "description", membersSet, exclude ) ) {
      f.description = v.getDescription();
    }
    if ( include( "folder", membersSet, exclude ) ) {
      f.folder = v.isFolder();
    }
    //The include check is intentionally omitted on the Id field because
    //it must be present or the tree rest service call will error
    if ( v.getId() != null ) {
      f.id = v.getId().toString();
    }
    if ( include( "lastModifiedDate", membersSet, exclude ) ) {
      f.lastModifiedDate = v.getLastModifiedDate();
    }
    if ( include( "locale", membersSet, exclude ) ) {
      f.locale = v.getLocale();
    }
    if ( include( "originalParentFolderPath", membersSet, exclude ) ) {
      f.originalParentFolderPath = v.getOriginalParentFolderPath();
    }
    if ( include( "deletedDate", membersSet, exclude ) ) {
      f.deletedDate = v.getDeletedDate();
    }
    if ( include( "lockDate", membersSet, exclude ) ) {
      f.lockDate = v.getLockDate();
    }
    if ( include( "locked", membersSet, exclude ) ) {
      f.locked = v.isLocked();
    }
    if ( include( "lockMessage", membersSet, exclude ) ) {
      f.lockMessage = v.getLockMessage();
    }
    if ( include( "lockOwner", membersSet, exclude ) ) {
      f.lockOwner = v.getLockOwner();
    }
    if ( include( "title", membersSet, exclude ) ) {
      f.title = v.getTitle();
    }
    if ( include( "versioned", membersSet, exclude ) ) {
      f.versioned = v.isVersioned();
    }
    if ( include( "versionId", membersSet, exclude ) ) {
      if ( v.getVersionId() != null ) {
        f.versionId = v.getVersionId().toString();
      }
    }

    if ( includeAcls ) {
      if ( v.getId() != null ) {
        try {
          f.repositoryFileAclDto = getRepoWs().getAcl( v.getId().toString() );
          if ( f.repositoryFileAclDto.isEntriesInheriting() ) {
            List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces( v.getId().toString() );
            f.repositoryFileAclDto.setAces( aces, f.repositoryFileAclDto.isEntriesInheriting() );
          }
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
    }

    if ( include( "locales", membersSet, exclude ) ) {
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
    }

    return f;
  }

  @Override
  public RepositoryFile unmarshal( final RepositoryFileDto v ) {
    return toFile( v );
  }

  public RepositoryFile toFile( final RepositoryFileDto v ) {
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
      new RepositoryFileSid( v.owner, RepositoryFileSid.Type.values()[ v.ownerType ] );
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

  private DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

}
