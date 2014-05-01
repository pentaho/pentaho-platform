package org.pentaho.platform.repository2.unified.jcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

/**
 * User: nbaker Date: 5/28/13
 */
public class RepositoryFileProxy extends RepositoryFile {
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
  private Boolean versioned;
  private Serializable id;
  private Lock lock;
  private Date lockDate;
  private String name;
  private String versionId;
  private Date createdDate;

  public RepositoryFileProxy( final Node node, final JcrTemplate template, IPentahoLocale pentahoLocale ) {
    super( null, null, false, false, false, null, null, null, null, false, null, null, null, null, null, null, null,
        null, -1, null, null );
    this.node = node;
    this.pentahoLocale = pentahoLocale;
    try {
      this.absPath = node.getPath();
    } catch ( RepositoryException e ) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
    }
    this.template = template;
    this.lockHelper = PentahoSystem.get( ILockHelper.class );
  }

  private PentahoJcrConstants getPentahoJcrConstants() {
    if ( constants == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          constants = new PentahoJcrConstants( session );
        }
      } );
    }
    return constants;
  }

  @Override
  public RepositoryFile clone() {
    return super.clone(); // To change body of overridden methods use File | Settings | File Templates.
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
      } else if ( this.getPath() != null ) {
        if ( !other.getPath().equals( this.getPath() ) ) {
          return false;
        }
      }
    } else if ( !this.getId().equals( other.getId() ) ) {
      return false;
    }
    if ( this.getLocale() == null ) {
      if ( other.getLocale() != null ) {
        return false;
      }
    } else if ( !this.getLocale().equals( other.getLocale()) ) {
      return false;
    }
    if ( this.getVersionId() == null ) {
      if ( other.getVersionId() != null ) {
        return false;
      }
    } else if ( !this.getVersionId().equals( other.getVersionId()) ) {
      return false;
    }
    return true;
//    return super.equals( obj ); // To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public Date getCreatedDate() {
    if ( createdDate == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {

          try {
            if ( node.hasProperty( getPentahoJcrConstants().getJCR_CREATED() ) ) {
              Calendar tmpCal = node.getProperty( getPentahoJcrConstants().getJCR_CREATED() ).getDate();
              if ( tmpCal != null ) {
                createdDate = tmpCal.getTime();
              }
            }
          } catch ( PathNotFoundException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          } catch ( ValueFormatException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return createdDate;
  }

  private Map<String, Serializable> getMetadata() throws RepositoryException {
    if ( metadata == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            metadata = JcrRepositoryFileUtils.getFileMetadata( session, getId() );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return metadata;
  }

  @Override
  public String getCreatorId() {
    try {
      if ( creatorId == null ) {
        Map<String, Serializable> metadata;
        metadata = getMetadata();
        if ( metadata != null ) {
          creatorId = (String) metadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
        }
      }
    } catch ( RepositoryException e ) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
    }
    return creatorId;
  }

  @Override
  public Date getDeletedDate() {
    return super.getDeletedDate(); // To change body of overridden methods use File | Settings | File Templates.
  }

  private void getTitleAndDescription() {
    if ( title == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            if ( JcrRepositoryFileUtils.isPentahoHierarchyNode( session, getPentahoJcrConstants(), node ) ) {
              if ( node.hasNode( getPentahoJcrConstants().getPHO_LOCALES() ) ) {

                // [BISERVER-8337] localize title and description
                LocalePropertyResolver lpr = new LocalePropertyResolver( getName() );
                Locale loc = getPentahoLocale() != null ? getPentahoLocale().getLocale() : null;
                LocalizationUtil localizationUtil = new LocalizationUtil( getLocalePropertiesMap(), loc );
                title = localizationUtil.resolveLocalizedString( lpr.resolveDefaultTitleKey(), null );
                if ( org.apache.commons.lang.StringUtils.isBlank( title ) ) {
                  title = localizationUtil.resolveLocalizedString( lpr.resolveTitleKey(), null );
                  if ( org.apache.commons.lang.StringUtils.isBlank( title ) ) {
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
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
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
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            if ( node.hasProperty( getPentahoJcrConstants().getPHO_FILESIZE() ) ) {
              fileSize = node.getProperty( getPentahoJcrConstants().getPHO_FILESIZE() ).getLong();
            }
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return fileSize;
  }

  @Override
  public Serializable getId() {

    if ( id == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            id = JcrRepositoryFileUtils.getNodeId( session, getPentahoJcrConstants(), node );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return id;
  }

  @Override
  public Date getLastModifiedDate() {
    if ( lastModifiedDate == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            if ( JcrRepositoryFileUtils.isPentahoFile( getPentahoJcrConstants(), node ) ) {
              // pho:lastModified nodes have OnParentVersion values of IGNORE; i.e. they don't exist in frozen nodes
              if ( !node.isNodeType( getPentahoJcrConstants().getNT_FROZENNODE() ) ) {
                Calendar tmpCal = node.getProperty( getPentahoJcrConstants().getPHO_LASTMODIFIED() ).getDate();
                if ( tmpCal != null ) {
                  lastModifiedDate = tmpCal.getTime();
                }
              }
            }
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }

    return lastModifiedDate;
  }

  @Override
  public String getLocale() {
    IPentahoLocale loc = getPentahoLocale();
    return ( loc != null ) ? loc.toString() : null;
  }

  @Override
  public Map<String, Properties> getLocalePropertiesMap() {
    if ( localeMap == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {

          try {
            localeMap =
                JcrRepositoryFileUtils.getLocalePropertiesMap( session, getPentahoJcrConstants(), node
                    .getNode( getPentahoJcrConstants().getPHO_LOCALES() ) );
          } catch (javax.jcr.PathNotFoundException e) {
            //Do not throw a stack trace if the locale file is missing.
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return localeMap;
  }

  private Lock getLock() {
    if ( lock == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {

          try {
            lock = session.getWorkspace().getLockManager().getLock( node.getPath() );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return lock;
  }

  @Override
  public Date getLockDate() {
    if ( isLocked() ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            lockDate = lockHelper.getLockDate( session, getPentahoJcrConstants(), getLock() );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return lockDate;
  }

  @Override
  public String getLockMessage() {
    if ( isLocked() ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            lockMessage = lockHelper.getLockMessage( session, getPentahoJcrConstants(), getLock() );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return lockMessage;
  }

  @Override
  public String getLockOwner() {
    if ( isLocked() ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            lockOwner = lockHelper.getLockOwner( session, getPentahoJcrConstants(), getLock() );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return lockOwner;
  }

  @Override
  public String getName() {
    if ( name == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            name =
                RepositoryFile.SEPARATOR.equals( getPath() )
                    ? "" : JcrRepositoryFileUtils.getNodeName( session, getPentahoJcrConstants(), node ); //$NON-NLS-1$
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return name;
  }

  @Override
  public String getOriginalParentFolderPath() {
    return super.getOriginalParentFolderPath(); // To change body of overridden methods use File | Settings | File
                                                // Templates.
  }

  @Override
  public String getPath() {
    if ( path == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            path =
                new DefaultPathConversionHelper().absToRel( ( JcrRepositoryFileUtils.getAbsolutePath( session,
                    getPentahoJcrConstants(), node ) ) );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
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
    if ( versionId == null ) {
      if ( isVersioned() ) {
        this.executeOperation( new SessionOperation() {
          @Override
          public void execute( Session session ) {
            try {
              versionId = JcrRepositoryFileUtils.getVersionId( session, getPentahoJcrConstants(), node );
            } catch ( RepositoryException e ) {
              e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            }
          }
        } );
      }
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

      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            folder = JcrRepositoryFileUtils.isPentahoFolder( getPentahoJcrConstants(), node );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return folder;
  }

  @Override
  public boolean isHidden() {
    if ( hidden == null ) {

      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            if ( node.hasProperty( getPentahoJcrConstants().getPHO_HIDDEN() ) ) {
              hidden = node.getProperty( getPentahoJcrConstants().getPHO_HIDDEN() ).getBoolean();
            }
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return hidden;
  }

  @Override
  public boolean isLocked() {
    if ( locked == null ) {

      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            locked = JcrRepositoryFileUtils.isLocked( getPentahoJcrConstants(), node );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return locked;
  }

  @Override
  public boolean isVersioned() {
    if ( versioned == null ) {
      this.executeOperation( new SessionOperation() {
        @Override
        public void execute( Session session ) {
          try {
            versioned = JcrRepositoryFileUtils.isVersioned( session, getPentahoJcrConstants(), node );
          } catch ( RepositoryException e ) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
          }
        }
      } );
    }
    return versioned;
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
        template.execute( new JcrCallback() {
          @Override
          public Object doInJcr( Session session ) throws IOException, RepositoryException {
            node = (Node) session.getItem( absPath );
            op.execute( node.getSession() );
            return null;
          }
        } );
      }
    } catch ( RepositoryException e ) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
    }
  }

  private interface SessionOperation {
    void execute( Session session );
  }

}
