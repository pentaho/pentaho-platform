/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util.client;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.commons.util.repository.GetCheckedoutDocsResponse;
import org.pentaho.commons.util.repository.INavigationService;
import org.pentaho.commons.util.repository.exception.ConstraintViolationException;
import org.pentaho.commons.util.repository.exception.FilterNotValidException;
import org.pentaho.commons.util.repository.exception.FolderNotValidException;
import org.pentaho.commons.util.repository.exception.InvalidArgumentException;
import org.pentaho.commons.util.repository.exception.ObjectNotFoundException;
import org.pentaho.commons.util.repository.exception.OperationNotSupportedException;
import org.pentaho.commons.util.repository.exception.PermissionDeniedException;
import org.pentaho.commons.util.repository.exception.RuntimeException;
import org.pentaho.commons.util.repository.exception.UpdateConflictException;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.CmisObjectImpl;
import org.pentaho.commons.util.repository.type.CmisProperties;
import org.pentaho.commons.util.repository.type.CmisProperty;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.commons.util.repository.type.PropertiesDocument;
import org.pentaho.commons.util.repository.type.PropertyBoolean;
import org.pentaho.commons.util.repository.type.PropertyDateTime;
import org.pentaho.commons.util.repository.type.PropertyId;
import org.pentaho.commons.util.repository.type.PropertyString;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class BiPlatformRepositoryClientNavigationService implements INavigationService {

  private Document doc;

  public Document getDoc() {
    return doc;
  }

  public void setDoc( Document doc ) {
    this.doc = doc;
  }

  public List<CmisObject> getObjectParent( String repositoryId, String objectId, String filter,
      boolean includeAllowableActions, boolean includeRelationships ) throws InvalidArgumentException,
    ConstraintViolationException, FilterNotValidException, RuntimeException, UpdateConflictException,
    ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException, FolderNotValidException {

    // TODO add support for filters

    if ( !repositoryId.equals( BiPlatformRepositoryClient.PLATFORMORIG ) ) {
      throw new InvalidArgumentException();
    }

    List<CmisObject> objects = new ArrayList<CmisObject>();

    // get the element for the specified object
    Element objectElement = getObjectElement( objectId );
    if ( objectElement == null ) {
      return objects;
    }

    // get the parent node
    Element parentElement = objectElement.getParent();
    if ( parentElement == null ) {
      return objects;
    }

    CmisObject parent = createCmisObjectFromElement( parentElement, 0 );

    objects.add( parent );

    return objects;

  }

  private CmisObject createCmisObjectFromElement( Element element, int depth ) {

    CmisObject object = new CmisObjectImpl();
    CmisProperties properties = new CmisProperties();
    List<CmisProperty> propList = properties.getProperties();

    // is this a folder or a file?
    boolean isDirectory = false;
    Attribute attr = element.attribute( "isDirectory" ); //$NON-NLS-1$
    if ( attr != null ) {
      isDirectory = "true".equalsIgnoreCase( attr.getText() ); //$NON-NLS-1$
    }
    // set the base properties
    String objectId = getObjectId( element );
    Calendar lastModifiedDate = getLastModifiedDate( element );
    String name = getName( element );
    String localizedName = getLocalizedName( element );
    String extension = getExtension( element );
    boolean visible = getVisible( element );

    propList.add( new PropertyId( PropertiesBase.OBJECTID, objectId ) );
    propList.add( new PropertyDateTime( PropertiesBase.LASTMODIFICATIONDATE, lastModifiedDate ) );

    if ( isDirectory ) {
      propList.add( new PropertyString( PropertiesBase.OBJECTTYPEID, CmisObject.OBJECT_TYPE_FOLDER ) );
    } else {
      propList.add( new PropertyString( PropertiesBase.OBJECTTYPEID, extension ) );
      propList.add( new PropertyBoolean( PropertiesDocument.CONTENTSTREAMALLOWED, true ) );
    }

    propList.add( new PropertyString( CmisObject.NAME, name ) );
    propList.add( new PropertyString( CmisObject.LOCALIZEDNAME, localizedName ) );
    propList.add( new PropertyBoolean( CmisObject.VISIBLE, visible ) );

    object.setProperties( properties );
    return object;
  }

  private String getObjectId( Element element ) {

    // get a list of the ancestors
    List<Element> pathElements = new ArrayList<Element>();
    Element current = element;
    while ( current != null ) {
      pathElements.add( 0, current );
      current = current.getParent();
    }

    // now create a path string
    StringBuilder sb = new StringBuilder();
    int idx = 0;
    for ( Element pathElement : pathElements ) {
      if ( idx > 1 ) {
        sb.append( RepositoryFile.SEPARATOR );
      }
      if ( idx > 0 ) {
        // ignore the first element
        sb.append( pathElement.attribute( "name" ).getText() ); //$NON-NLS-1$
      }
      idx++;
    }
    String objectId = sb.toString();
    return objectId;

  }

  private Calendar getLastModifiedDate( Element element ) {
    String lastModifiedDateStr = ""; //$NON-NLS-1$
    Attribute attr = element.attribute( "lastModifiedDate" ); //$NON-NLS-1$
    if ( attr != null ) {
      lastModifiedDateStr = attr.getText();
      long millis = new Long( lastModifiedDateStr );
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis( millis );
      return calendar;
    }
    return null;
  }

  private String getLocalizedName( Element element ) {
    String name = ""; //$NON-NLS-1$
    Attribute attr = element.attribute( "localized-name" ); //$NON-NLS-1$
    if ( attr != null ) {
      name = attr.getText();
    }
    return name;
  }

  private String getName( Element element ) {
    String name = ""; //$NON-NLS-1$
    Attribute attr = element.attribute( "name" ); //$NON-NLS-1$
    if ( attr != null ) {
      name = attr.getText();
    }
    return name;
  }

  private boolean getVisible( Element element ) {
    boolean visible = true;
    Attribute attr = element.attribute( "visible" ); //$NON-NLS-1$
    if ( attr != null ) {
      visible = "true".equalsIgnoreCase( attr.getText() ); //$NON-NLS-1$
    }
    return visible;
  }

  private String getExtension( Element element ) {
    String name = ""; //$NON-NLS-1$
    Attribute attr = element.attribute( "name" ); //$NON-NLS-1$
    if ( attr != null ) {
      name = attr.getText();
    }
    int idx = name.indexOf( '.' );
    String extension = ""; //$NON-NLS-1$
    if ( idx != -1 ) {
      extension = name.substring( idx + 1 );
    }
    return extension;
  }

  private Element getObjectElement( String path ) throws ObjectNotFoundException {
    // parse out the path
    StringTokenizer tokenizer = new StringTokenizer( path, RepositoryFile.SEPARATOR ); //$NON-NLS-1$

    StringBuilder sb = new StringBuilder();
    sb.append( "/repository" ); //$NON-NLS-1$
    String folderName;
    int tokenCount = tokenizer.countTokens();
    for ( int idx = 0; idx < tokenCount - 1; idx++ ) {
      folderName = tokenizer.nextToken();
      sb.append( "/file[@isDirectory='true' and @name='" ) //$NON-NLS-1$
          .append( folderName ).append( "']" ); //$NON-NLS-1$
    }
    if ( tokenizer.hasMoreTokens() ) {
      folderName = tokenizer.nextToken();
      sb.append( "/file[@name='" ) //$NON-NLS-1$
          .append( folderName ).append( "']" ); //$NON-NLS-1$
    }
    String xPath = sb.toString();
    Element element = (Element) doc.selectSingleNode( xPath );
    if ( element == null ) {
      throw new ObjectNotFoundException();
    }
    return element;

  }

  private Element getFolderElement( String path ) throws FolderNotValidException {

    // parse out the path
    StringTokenizer tokenizer = new StringTokenizer( path, "" + RepositoryFile.SEPARATOR ); //$NON-NLS-1$

    StringBuilder sb = new StringBuilder();
    sb.append( "/repository" ); //$NON-NLS-1$
    String folderName;
    while ( tokenizer.hasMoreTokens() ) {
      folderName = tokenizer.nextToken();
      sb.append( "/file[@isDirectory='true' and @name='" ) //$NON-NLS-1$
          .append( folderName ).append( "']" ); //$NON-NLS-1$
    }
    String xPath = sb.toString();
    Element element = (Element) doc.selectSingleNode( xPath );
    if ( element == null ) {
      throw new FolderNotValidException();
    }
    return element;
  }

  public List<CmisObject> getFolderParent( String repositoryId, String folderId, String filter,
      boolean includeAllowableActions, boolean includeRelationships, boolean returnToRoot )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException {

    // TODO add support for filters

    if ( !repositoryId.equals( BiPlatformRepositoryClient.PLATFORMORIG ) ) {
      throw new InvalidArgumentException();
    }

    List<CmisObject> objects = new ArrayList<CmisObject>();

    // get the element for the specified object
    Element objectElement = getFolderElement( folderId );

    // get the parent node
    Element parentElement = objectElement.getParent();

    if ( parentElement == null ) {
      return objects;
    }
    CmisObject parent = createCmisObjectFromElement( parentElement, 0 );

    objects.add( parent );

    return objects;
  }

  public List<CmisObject> getDescendants( String repositoryId, String folderId, TypesOfFileableObjects type, int depth,
      String filter, boolean includeAllowableActions, boolean includeRelationships ) throws InvalidArgumentException,
    ConstraintViolationException, FilterNotValidException, RuntimeException, UpdateConflictException,
    ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException, FolderNotValidException {

    Collection filters = null;
    if ( !StringUtil.isEmpty( filter ) ) {
      filters = getFilterCollection( filter );
    }

    if ( !repositoryId.equals( BiPlatformRepositoryClient.PLATFORMORIG ) ) {
      throw new InvalidArgumentException();
    }

    List<CmisObject> objects = new ArrayList<CmisObject>();

    // get the element for the specified object
    Element objectElement = getFolderElement( folderId );

    addChildren( objects, objectElement, type, filters, 0, 0, depth, 1 );

    return objects;
  }

  protected void addChildren( List<CmisObject> objects, Element objectElement, TypesOfFileableObjects type,
      Collection filters, int maxItems, int skipCount, int depth, int level ) {
    if ( objectElement == null ) {
      return;
    }
    CmisObject object;
    int skipped = 0;
    boolean ok = false;
    for ( Object element : objectElement.elements() ) {

      ok = false;
      object = createCmisObjectFromElement( (Element) element, 0 );
      if ( type == null || type.getValue().equals( TypesOfFileableObjects.ANY ) ) {
        ok = true;
      } else if ( TypesOfFileableObjects.FOLDERS.equals( type.getValue() )
          && CmisObject.OBJECT_TYPE_FOLDER.equals( object.findStringProperty( PropertiesBase.OBJECTTYPEID, null ) ) ) {
        ok = true;
      } else if ( TypesOfFileableObjects.DOCUMENTS.equals( type.getValue() )
          && !CmisObject.OBJECT_TYPE_FOLDER.equals( object.findStringProperty( PropertiesBase.OBJECTTYPEID, null ) ) ) {
        // TODO support policies
        ok = true;
      }
      if ( ok && filters != null ) {
        String objectType = object.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
        if ( !filters.contains( objectType ) ) {
          ok = false;
        }
      }

      if ( ok && skipCount > 0 ) {
        if ( skipped < skipCount ) {
          ok = false;
          skipped++;
        }
      }

      if ( ok && maxItems > 0 ) {
        if ( objects.size() >= maxItems ) {
          break;
        }
      }

      if ( ok ) {
        objects.add( object );
        // see if we have to recurse
      }
      if ( depth > 0 && level < depth
          && CmisObject.OBJECT_TYPE_FOLDER.equals( object.findStringProperty( PropertiesBase.OBJECTTYPEID, null ) ) ) {

        addChildren( objects, (Element) element, type, filters, maxItems, skipCount, depth, level + 1 );

      }
    }
  }

  public List<CmisObject> getChildren( String repositoryId, String folderId, TypesOfFileableObjects type,
      String filter, boolean includeAllowableActions, boolean includeRelationships, int maxItems, int skipCount )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException {

    Collection filters = null;
    if ( !StringUtil.isEmpty( filter ) ) {
      filters = getFilterCollection( filter );
    }

    if ( !repositoryId.equals( BiPlatformRepositoryClient.PLATFORMORIG ) ) {
      throw new InvalidArgumentException();
    }

    List<CmisObject> objects = new ArrayList<CmisObject>();

    // get the element for the specified object
    Element objectElement = getFolderElement( folderId );

    addChildren( objects, objectElement, type, filters, maxItems, skipCount, 1, 1 );

    return objects;
  }

  private Collection getFilterCollection( String filterstr ) {
    StringTokenizer tokenizer = new StringTokenizer( filterstr, "," ); //$NON-NLS-1$

    HashSet<String> set = new HashSet<String>();
    int tokenCount = tokenizer.countTokens();
    for ( int idx = 0; idx < tokenCount; idx++ ) {
      set.add( tokenizer.nextToken() );
    }
    return set;
  }

  public GetCheckedoutDocsResponse getCheckedoutDocs( String repositoryId, String folderId, String filter,
      boolean includeAllowableActions, boolean includeRelationships, int maxItems, int skipCount )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException {

    throw new OperationNotSupportedException();

  }

  public String getRepositoryPath( CmisObject object ) {

    // the id is the path and file name
    String id = object.findIdProperty( PropertiesBase.OBJECTID, null );
    String typeId = object.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
    if ( CmisObject.OBJECT_TYPE_FOLDER.equals( typeId ) ) {
      return id;
    } else {
      int idx = id.lastIndexOf( RepositoryFile.SEPARATOR );
      if ( idx != -1 ) {
        return id.substring( 0, idx );
      } else {
        return ""; //$NON-NLS-1$
      }
    }
  }

  public String getRepositoryFilename( CmisObject object ) {

    String typeId = object.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
    if ( CmisObject.OBJECT_TYPE_FOLDER.equals( typeId ) ) {
      return ""; //$NON-NLS-1$
    } else {
      String name = object.findStringProperty( CmisObject.NAME, null );
      return name;
    }
  }

}
