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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@code RepositoryFile} into JAXB-safe object and vice-versa.
 *
 * @author mlowery
 */
public class RepositoryFileAdapter extends XmlAdapter<RepositoryFileDto, RepositoryFile> {

  private static Logger logger = LoggerFactory.getLogger( RepositoryFileAdapter.class );

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

  public static Logger getLogger() {
    return logger;
  }

  @Override
  public RepositoryFileDto marshal( final RepositoryFile v ) {
    return toFileDto( v, membersSet, exclude, includeAcls );
  }

  public RepositoryFileDto marshal( final RepositoryFileTree repositoryFileTree ) {
    return toFileDto( repositoryFileTree, membersSet, exclude, includeAcls );
  }

  private static boolean include( String key, Set<String> set, boolean exclude ) {
    return !exclude && ( set == null || set.contains( key ) ) || ( exclude && !set.contains( key ) );
  }

  public static RepositoryFileDto toFileDto( final RepositoryFileTree repositoryFileTree, Set<String> memberSet,
      boolean exclude, boolean includeAcls ) {
    RepositoryFileDto repositoryFileDto = toFileDto( repositoryFileTree.getFile(), memberSet, exclude, includeAcls );
    return repositoryFileDto;
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
    // Will try to read the repository file parameters from the repository, in case it returns NPE, means that the file
    // no longer exists, so it returns null
    try {
      if ( include( "name", memberSet, exclude ) ) {
        f.setName( v.getName() );
      }
      if ( include( "path", memberSet, exclude ) ) {
        f.setPath( v.getPath() );
      }
      if ( include( "hidden", memberSet, exclude ) ) {
        f.setHidden( v.isHidden() );
      }
      if ( include( "aclNode", memberSet, exclude ) ) {
        f.setAclNode( v.isAclNode() );
      }
      if ( include( "createDate", memberSet, exclude ) ) {
        f.setCreatedDate( marshalDate( v.getCreatedDate() ) );
      }
      if ( include( "creatorId", memberSet, exclude ) ) {
        f.setCreatorId( v.getCreatorId() );
      }
      if ( include( "fileSize", memberSet, exclude ) ) {
        f.setFileSize( v.getFileSize() );
      }
      if ( include( "description", memberSet, exclude ) ) {
        f.setDescription( v.getDescription() );
      }
      if ( include( "folder", memberSet, exclude ) ) {
        f.setFolder( v.isFolder() );
      }
      //The include check is intentionally omitted on the Id field because
      //it must be present or the tree rest service call will error
      if ( v.getId() != null ) {
        f.setId( v.getId().toString() );
      }
      if ( include( "lastModifiedDate", memberSet, exclude ) ) {
        f.setLastModifiedDate( marshalDate( v.getLastModifiedDate() ) );
      }
      if ( include( "locale", memberSet, exclude ) ) {
        f.setLocale( v.getLocale() );
      }
      if ( include( "originalParentFolderPath", memberSet, exclude ) ) {
        f.setOriginalParentFolderPath( v.getOriginalParentFolderPath() );
      }
      if ( include( "deletedDate", memberSet, exclude ) ) {
        f.setDeletedDate( marshalDate( v.getDeletedDate() ) );
      }
      if ( include( "lockDate", memberSet, exclude ) ) {
        f.setLockDate( marshalDate( v.getLockDate() ) );
      }
      if ( include( "locked", memberSet, exclude ) ) {
        f.setLocked( v.isLocked() );
      }
      if ( include( "lockMessage", memberSet, exclude ) ) {
        f.setLockMessage( v.getLockMessage() );
      }
      if ( include( "lockOwner", memberSet, exclude ) ) {
        f.setLockOwner( v.getLockOwner() );
      }
      if ( include( "title", memberSet, exclude ) ) {
        f.setTitle( v.getTitle() );
      }
      if ( include( "versioned", memberSet, exclude ) ) {
        f.setVersioned( v.isVersioned() );
      }
      if ( include( "versionId", memberSet, exclude ) ) {
        if ( v.getVersionId() != null ) {
          f.setVersionId( v.getVersionId().toString() );
        }
      }
    } catch ( NullPointerException e ) {
      getLogger().warn( "NullPointerException while reading file attributes, returning null. Probable cause: File does not"
        + "exist anymore: " );
      return null;
    }

    if ( includeAcls ) {
      if ( v.getId() != null ) {
        try {
          String id = v.getId().toString();
          f.setRepositoryFileAclDto( getRepoWs().getAcl( id ) );
          if ( f.getRepositoryFileAclDto().isEntriesInheriting() ) {
            List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces( id );
            f.getRepositoryFileAclDto().setAces( aces, f.getRepositoryFileAclDto().isEntriesInheriting() );
          }
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }

      if ( include( "owner", memberSet, exclude ) ) {
        Serializable id = v.getId();
        if ( id != null ) {
          RepositoryFileAclDto acl = getRepoWs().getAcl( "" + id );
          if ( acl != null ) {
            f.setOwner( acl.getOwner() );
          }
        }
      }
    }

    if ( include( "locales", memberSet, exclude ) ) {
      if ( v.getLocalePropertiesMap() != null ) {
        f.setLocalePropertiesMapEntries( new ArrayList<LocaleMapDto>() );
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

          f.getLocalePropertiesMapEntries().add( localeMapDto );
        }
      }
    }

    IRepositoryVersionManager repositoryVersionManager;
    try {
      repositoryVersionManager = JcrRepositoryFileUtils.getRepositoryVersionManager();
      // Not found, must be in Spoon
      if ( repositoryVersionManager == null ) {
        return f;
      }
    } catch ( NoClassDefFoundError ex ) {
      //If this class is not available then we are running this method from Spoon
      //and do not need to populate the versioning flags.  They are populated to
      //support tree and child calls.
      return f;
    }
    if ( include( "versioningEnabled", memberSet, exclude ) ) {
      f.setVersioningEnabled( repositoryVersionManager.isVersioningEnabled( v.getPath() ) );
    }
    if ( include( "versionCommentEnabled", memberSet, exclude ) ) {
      f.setVersionCommentEnabled( repositoryVersionManager.isVersionCommentEnabled( v.getPath() ) );
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
    if ( v.getId() != null ) {
      builder = new RepositoryFile.Builder( v.getId(), v.getName() );
    } else {
      builder = new RepositoryFile.Builder( v.getName() );
    }
    if ( v.getOwnerType() != -1 ) {
      new RepositoryFileSid( v.getOwner(), RepositoryFileSid.Type.values()[ v.getOwnerType() ] );
    }
    if ( v.getLocalePropertiesMapEntries() != null ) {
      for ( LocaleMapDto localeMapDto : v.getLocalePropertiesMapEntries() ) {

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



    return builder.path( v.getPath() ).createdDate( unmarshalDate( v.getCreatedDate() ) ).creatorId( v.getCreatorId() ).description( v.getDescription() )
      .folder( v.isFolder() ).fileSize( v.getFileSize() ).lastModificationDate( unmarshalDate( v.getLastModifiedDate() ) ).locale( v.getLocale() )
      .lockDate( unmarshalDate( v.getLockDate() ) ).locked( v.isLocked() ).lockMessage( v.getLockMessage() ).lockOwner( v.getLockOwner() )
      .title( v.getTitle() ).versioned( v.isVersioned() ).versionId( v.getVersionId() ).originalParentFolderPath(
            v.getOriginalParentFolderPath() ).deletedDate( unmarshalDate( v.getDeletedDate() ) ).hidden( v.isHidden() ).schedulable( !v
                .isNotSchedulable() ).aclNode( v.isAclNode() ).build();
  }

  public static Date unmarshalDate( String date ) {
    DateAdapter adapter = new DateAdapter();
    if ( date == null || date.length() == 0 ) {
      return null;
    }
    try {
      return adapter.unmarshal( date );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  public static String marshalDate( Date date ) {
    DateAdapter adapter = new DateAdapter();
    if ( date == null ) {
      return "";
    }
    try {
      return adapter.marshal( date );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  private static DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

}
