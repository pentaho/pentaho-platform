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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import com.pentaho.pdi.messages.Messages;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.jcr.JcrTemplate;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * User: nbaker Date: 5/28/13
 */
public class RepositoryFileProxy extends RepositoryFile {

  private static final long serialVersionUID = 5244310953843118329L;
  private static Logger logger = LoggerFactory.getLogger( RepositoryFileProxy.class );

  private static final String INVALID_ITEM_STATE_EXCEPTION_MESSAGE = "RepositoryFileProxy.INVALID_ITEM_STATE_EXCEPTION";
  private static final String REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE =
    "RepositoryFileProxy.REPOSITORY_EXCEPTION_WAS_FOUND";
  private Node node;
  private PentahoJcrConstants constants;
  private JcrTemplate template;
  private Map<String, Serializable> metadata;
  private String creatorId;
  private Map<String, Properties> localeMap;
  private String description;
  private String title;
  private IPentahoLocale pentahoLocale;
  private long fileSize = -1;
  private Date lastModifiedDate;
  private Boolean locked;
  private ILockHelper lockHelper;
  private String lockMessage;
  private String lockOwner;
  private String path;
  private String absPath; //This path is intentionally in Jcr Encoded form (the raw path)
  private Boolean folder;
  private Boolean hidden;
  private Boolean schedulable;
  private Boolean versioned;
  private Serializable id;
  private Lock lock;
  private Date lockDate;
  private String name;
  private String versionId;
  private Date createdDate;
  private Boolean aclNode;

  public RepositoryFileProxy( final Node node, final JcrTemplate template, IPentahoLocale pentahoLocale ) {
    super( null, null, false, HIDDEN_BY_DEFAULT, SCHEDULABLE_BY_DEFAULT, false, null, null, null, null, false, null,
      null, null, null, null, null,
      null,
      null, -1, null, null, false );
    this.node = node;
    this.pentahoLocale = pentahoLocale;
    try {
      this.absPath = node.getPath();
    } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
      getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "constructor" ) );
    } catch ( RepositoryException e ) {
      getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
    }
    this.template = template;
    this.lockHelper = PentahoSystem.get( ILockHelper.class );
  }

  public static Logger getLogger() {
    return logger;
  }

  private PentahoJcrConstants getPentahoJcrConstants() {
    if ( constants == null ) {
      this.executeOperation( session -> constants = new PentahoJcrConstants( session ) );
    }
    return constants;
  }

  @Override
  public int compareTo( RepositoryFile other ) {
    if ( other == null ) {
      throw new NullPointerException(); // per Comparable contract
    }
    if ( equals( other ) ) {
      return 0;
    }
    // either this or other has a null id; fall back on name
    return getTitle().compareTo( other.getTitle() );
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }

    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }

    RepositoryFile other = (RepositoryFile) obj;

    if ( this.getId() == null ) {
      if ( other.getId() != null ) {
        return false;
      } else {
        return !( this.getPath() != null && !other.getPath().equals( this.getPath() ) );
      }
    } else if ( !this.getId().equals( other.getId() ) ) {
      return false;
    }
    if ( this.getLocale() == null ) {
      if ( other.getLocale() != null ) {
        return false;
      }
    } else if ( !this.getLocale().equals( other.getLocale() ) ) {
      return false;
    }
    if ( this.getVersionId() == null ) {
      if ( other.getVersionId() != null ) {
        return false;
      }
    } else {
      return this.getVersionId().equals( other.getVersionId() );
    }
    return true;
  }

  @Override
  public Date getCreatedDate() {
    if ( createdDate == null ) {
      this.executeOperation( session -> {

        try {
          if ( node.hasProperty( getPentahoJcrConstants().getJCR_CREATED() ) ) {
            Calendar tmpCal = node.getProperty( getPentahoJcrConstants().getJCR_CREATED() ).getDate();
            if ( tmpCal != null ) {
              createdDate = tmpCal.getTime();
            }
          }
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger()
            .warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getCreatedDate" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( null, e );
        }
      } );
    }
    return createdDate;
  }

  public Map<String, Serializable> getMetadata() {
    if ( metadata == null ) {
      this.executeOperation( session -> {
        try {
          metadata = JcrRepositoryFileUtils.getFileMetadata( session, getId() );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getMetadata" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return metadata;
  }

  public void setMetadata( Map<String, Serializable> metadata ) {
    this.executeOperation( session -> {
      try {
        JcrRepositoryFileUtils.setFileMetadata( session, getId(), metadata );
      } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
        getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "setMetadata" ) );
      } catch ( RepositoryException e ) {
        getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
      }
    } );

  }


  @Override
  public String getCreatorId() {
    try {
      if ( creatorId == null ) {
        Map<String, Serializable> metadataCreatorId = getMetadata();
        if ( metadataCreatorId != null ) {
          creatorId = (String) metadataCreatorId.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
        }
      }
    } catch ( Exception e ) {
      getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
    }
    return creatorId;
  }

  private void getTitleAndDescription() {
    if ( title == null ) {
      this.executeOperation( session -> {
        try {
          if ( JcrRepositoryFileUtils.isPentahoHierarchyNode( session, getPentahoJcrConstants(), node ) ) {
            if ( node.hasNode( getPentahoJcrConstants().getPHO_LOCALES() ) ) {

              // [BISERVER-8337] localize title and description
              LocalePropertyResolver lpr = new LocalePropertyResolver( getName() );
              Locale loc = getPentahoLocale() != null ? getPentahoLocale().getLocale() : null;
              LocalizationUtil localizationUtil = new LocalizationUtil( getLocalePropertiesMap(), loc );
              title = localizationUtil.resolveLocalizedString( lpr.resolveDefaultTitleKey(), null );
              if ( StringUtils.isBlank( title ) ) {
                title = localizationUtil.resolveLocalizedString( lpr.resolveTitleKey(), null );
                if ( StringUtils.isBlank( title ) ) {
                  title = localizationUtil.resolveLocalizedString( lpr.resolveNameKey(), title );
                }
              }
              description = localizationUtil.resolveLocalizedString( lpr.resolveDefaultDescriptionKey(), null );
              if ( StringUtils.isBlank( description ) ) {
                description = localizationUtil.resolveLocalizedString( lpr.resolveDescriptionKey(), description );
              }
            }

            // BISERVER-8609 - Backwards compatibility. Fallback to the old data structure if title/description are
            // not found
            if ( title == null && node.hasNode( getPentahoJcrConstants().getPHO_TITLE() ) ) {
              title =
                JcrRepositoryFileUtils.getLocalizedString( session, getPentahoJcrConstants(), node
                  .getNode( getPentahoJcrConstants().getPHO_TITLE() ), pentahoLocale );
            }
            if ( description == null && node.hasNode( getPentahoJcrConstants().getPHO_DESCRIPTION() ) ) {
              description =
                JcrRepositoryFileUtils.getLocalizedString( session, getPentahoJcrConstants(), node
                  .getNode( getPentahoJcrConstants().getPHO_DESCRIPTION() ), pentahoLocale );
            }

          }
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger()
            .warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getTitleAndDescription" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
  }

  @Override
  public String getDescription() {
    if ( description != null ) {
      return description;
    }
    getTitleAndDescription();
    return description;
  }

  private IPentahoLocale getPentahoLocale() {
    return pentahoLocale;
  }

  @Override
  public Long getFileSize() {
    if ( fileSize == -1 ) {
      this.executeOperation( session -> {
        try {
          if ( node.hasProperty( getPentahoJcrConstants().getPHO_FILESIZE() ) ) {
            fileSize = node.getProperty( getPentahoJcrConstants().getPHO_FILESIZE() ).getLong();
          }
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getFileSize" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return fileSize;
  }

  @Override
  public Serializable getId() {

    if ( id == null ) {
      this.executeOperation( session -> {
        try {
          id = JcrRepositoryFileUtils.getNodeId( session, getPentahoJcrConstants(), node );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getId" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return id;
  }

  @Override
  public Date getLastModifiedDate() {
    if ( lastModifiedDate != null ) {
      return lastModifiedDate;
    }

    this.executeOperation( session -> {
      try {
        lastModifiedDate = getLastModifiedDatePentahoFile( lastModifiedDate );
      } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
        getLogger()
          .warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLastModifiedDate" ) );
      } catch ( RepositoryException e ) {
        getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
      }
    } );


    return lastModifiedDate;
  }

  private Date getLastModifiedDatePentahoFile( Date lastModified ) throws RepositoryException {
    // pho:lastModified nodes have OnParentVersion values of IGNORE; i.e. they don't exist in frozen nodes
    if ( JcrRepositoryFileUtils.isPentahoFile( getPentahoJcrConstants(), node )
      && !node.isNodeType( getPentahoJcrConstants().getNT_FROZENNODE() ) ) {
      Calendar tmpCal = node.getProperty( getPentahoJcrConstants().getPHO_LASTMODIFIED() ).getDate();
      if ( tmpCal != null ) {
        lastModified = tmpCal.getTime();
      }
    }
    return lastModified;
  }

  @Override
  public String getLocale() {
    IPentahoLocale loc = getPentahoLocale();
    return ( loc != null ) ? loc.toString() : null;
  }

  @Override
  public Map<String, Properties> getLocalePropertiesMap() {
    if ( localeMap == null ) {
      this.executeOperation( session -> {

        try {
          localeMap =
            JcrRepositoryFileUtils.getLocalePropertiesMap( session, getPentahoJcrConstants(), node.getNode(
              getPentahoJcrConstants().getPHO_LOCALES() ) );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLocale" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return localeMap;
  }

  private Lock getLock() {
    if ( lock == null ) {
      this.executeOperation( session -> {

        try {
          lock = session.getWorkspace().getLockManager().getLock( node.getPath() );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLock" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return lock;
  }

  @Override
  public Date getLockDate() {
    if ( isLocked() ) {
      this.executeOperation( session -> {
        try {
          lockDate = lockHelper.getLockDate( session, getPentahoJcrConstants(), getLock() );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLockDate" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return lockDate;
  }

  @Override
  public String getLockMessage() {
    if ( isLocked() ) {
      this.executeOperation( session -> {
        try {
          lockMessage = lockHelper.getLockMessage( session, getPentahoJcrConstants(), getLock() );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger()
            .warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLockMessage" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return lockMessage;
  }

  @Override
  public String getLockOwner() {
    if ( isLocked() ) {
      this.executeOperation( session -> {
        try {
          lockOwner = lockHelper.getLockOwner( session, getPentahoJcrConstants(), getLock() );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getLockOwner" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return lockOwner;
  }

  @Override
  public String getName() {
    if ( name == null ) {
      this.executeOperation( session -> {
        try {
          name =
            RepositoryFile.SEPARATOR.equals( getPath() )
              ? "" : JcrRepositoryFileUtils.getNodeName( session, getPentahoJcrConstants(), node ); //$NON-NLS-1$
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getName" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return name;
  }

  @Override
  public String getPath() {
    if ( path == null ) {
      this.executeOperation( session -> {
        try {
          path =
            new DefaultPathConversionHelper().absToRel( ( JcrRepositoryFileUtils.getAbsolutePath( session,
              getPentahoJcrConstants(), node ) ) );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getPath" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return path;
  }

  @Override
  public String getTitle() {
    getTitleAndDescription();
    return title != null ? title : getName();
  }

  @Override
  public Serializable getVersionId() {
    if ( versionId == null && isVersioned() ) {
      this.executeOperation( session -> {
        try {
          versionId = JcrRepositoryFileUtils.getVersionId( session, getPentahoJcrConstants(), node );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "getVersionId" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return versionId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( this.getId() == null ) ? 0 : this.getId().hashCode() );
    result = prime * result + ( ( this.getLocale() == null ) ? 0 : this.getLocale().hashCode() );
    result = prime * result + ( ( this.getVersionId() == null ) ? 0 : this.getVersionId().hashCode() );
    return result;
  }

  @Override
  public boolean isFolder() {
    if ( folder == null ) {

      this.executeOperation( session -> {
        try {
          folder = JcrRepositoryFileUtils.isPentahoFolder( getPentahoJcrConstants(), node );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "isFolder" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return folder;
  }

  @Override
  public Boolean isHidden() {
    if ( hidden == null ) {

      this.executeOperation( session -> {
        try {
          if ( node.hasProperty( getPentahoJcrConstants().getPHO_HIDDEN() ) ) {
            hidden = node.getProperty( getPentahoJcrConstants().getPHO_HIDDEN() ).getBoolean();
          }
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "isHidden" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    // exclude NPE
    return hidden == null ? HIDDEN_BY_DEFAULT : hidden;
  }

  @Override
  public Boolean isSchedulable() {
    if ( schedulable == null ) {
      this.executeOperation( session -> {
        try {
          Map<String, Serializable> metadataIsSchedulable = getMetadata();
          if ( metadataIsSchedulable != null ) {
            schedulable = metadataIsSchedulable.get( SCHEDULABLE_KEY ) != null
              ? BooleanUtils.toBoolean( (String) metadataIsSchedulable.get( SCHEDULABLE_KEY ) ) : null;
          }
        } catch ( Exception e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return schedulable == null ? SCHEDULABLE_BY_DEFAULT : schedulable;
  }

  @Override
  public boolean isLocked() {
    if ( locked == null ) {

      this.executeOperation( session -> {
        try {
          locked = JcrRepositoryFileUtils.isLocked( getPentahoJcrConstants(), node );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "isLocked" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return locked;
  }

  @Override
  public boolean isVersioned() {
    if ( versioned == null ) {
      this.executeOperation( session -> {
        try {
          versioned = JcrRepositoryFileUtils.isVersioned( session, getPentahoJcrConstants(), node );
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "isVersioned" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    return versioned;
  }

  @Override
  public boolean isAclNode() {
    if ( aclNode == null ) {
      this.executeOperation( session -> {
        try {
          if ( node.hasProperty( getPentahoJcrConstants().getPHO_ACLNODE() ) ) {
            aclNode = node.getProperty( getPentahoJcrConstants().getPHO_ACLNODE() ).getBoolean();
          }
        } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
          getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "isAclNode" ) );
        } catch ( RepositoryException e ) {
          getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
        }
      } );
    }
    // exclude NPE
    return aclNode != null && aclNode;
  }

  @Override
  public String toString() {
    return super.toString(); // To change body of overridden methods use File | Settings | File Templates.
  }

  private void executeOperation( final SessionOperation op ) {
    try {
      if ( node.getSession().isLive() ) {
        op.execute( node.getSession() );
      } else {
        template.execute( session -> {
          node = (Node) session.getItem( absPath );
          op.execute( node.getSession() );
          return null;
        } );
      }
    } catch ( InvalidItemStateException | ItemNotFoundException | PathNotFoundException e ) {
      getLogger().warn( Messages.getInstance().getString( INVALID_ITEM_STATE_EXCEPTION_MESSAGE, "toString" ) );
    } catch ( RepositoryException e ) {
      getLogger().error( Messages.getInstance().getString( REPOSITORY_EXCEPTION_WAS_FOUND_MESSAGE ), e );
    }
  }

  private interface SessionOperation {
    void execute( Session session );
  }

}
