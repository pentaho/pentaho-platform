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

import java.io.Serializable;
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

  @Override
  public RepositoryFileDto marshal( final RepositoryFile v ) {
    return toFileDto( v, membersSet, exclude, includeAcls );
  }

  private static boolean include( String key, Set<String> set, boolean exclude ) {
    return !exclude && ( set == null || set.contains( key ) ) || ( exclude && !set.contains( key ) );
  }

  public static RepositoryFileDto toFileDto( final RepositoryFile v, Set<String> memberSet, boolean exclude ) {
    return toFileDto( v, memberSet, exclude, false );
  }

  public static RepositoryFileDto toFileDto( final RepositoryFile v, Set<String> memberSet, boolean exclude,
                                             boolean includeAcls ) {
    if ( v == null ) {
      return null;
    }
    RepositoryFileDto f = new RepositoryFileDto();
    if ( include( "name", memberSet, exclude ) ) {
      f.name = v.getName();
    }
    if ( include( "path", memberSet, exclude ) ) {
      f.path = v.getPath();
    }
    if ( include( "hidden", memberSet, exclude ) ) {
      f.hidden = v.isHidden();
    }
    if ( include( "aclNode", memberSet, exclude ) ) {
      f.aclNode = v.isAclNode();
    }
    if ( include( "createDate", memberSet, exclude ) ) {
      f.createdDate = v.getCreatedDate();
    }
    if ( include( "creatorId", memberSet, exclude ) ) {
      f.creatorId = v.getCreatorId();
    }

    if ( include( "owner", memberSet, exclude ) ) {
      Serializable id = v.getId();
      if( id != null ) {
        RepositoryFileAclDto acl = getRepoWs().getAcl( "" + id );
        if ( acl != null ) {
          f.owner = acl.getOwner();
        }
      }
    }

    if ( include( "fileSize", memberSet, exclude ) ) {
      f.fileSize = v.getFileSize();
    }
    if ( include( "description", memberSet, exclude ) ) {
      f.description = v.getDescription();
    }
    if ( include( "folder", memberSet, exclude ) ) {
      f.folder = v.isFolder();
    }
    //The include check is intentionally omitted on the Id field because
    //it must be present or the tree rest service call will error
    if ( v.getId() != null ) {
      f.id = v.getId().toString();
    }
    if ( include( "lastModifiedDate", memberSet, exclude ) ) {
      f.lastModifiedDate = v.getLastModifiedDate();
    }
    if ( include( "locale", memberSet, exclude ) ) {
      f.locale = v.getLocale();
    }
    if ( include( "originalParentFolderPath", memberSet, exclude ) ) {
      f.originalParentFolderPath = v.getOriginalParentFolderPath();
    }
    if ( include( "deletedDate", memberSet, exclude ) ) {
      f.deletedDate = v.getDeletedDate();
    }
    if ( include( "lockDate", memberSet, exclude ) ) {
      f.lockDate = v.getLockDate();
    }
    if ( include( "locked", memberSet, exclude ) ) {
      f.locked = v.isLocked();
    }
    if ( include( "lockMessage", memberSet, exclude ) ) {
      f.lockMessage = v.getLockMessage();
    }
    if ( include( "lockOwner", memberSet, exclude ) ) {
      f.lockOwner = v.getLockOwner();
    }
    if ( include( "title", memberSet, exclude ) ) {
      f.title = v.getTitle();
    }
    if ( include( "versioned", memberSet, exclude ) ) {
      f.versioned = v.isVersioned();
    }
    if ( include( "versionId", memberSet, exclude ) ) {
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

    if ( include( "locales", memberSet, exclude ) ) {
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
        v.originalParentFolderPath ).deletedDate( v.deletedDate ).hidden( v.hidden ).aclNode( v.aclNode ).build();
  }

  private static DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

}
