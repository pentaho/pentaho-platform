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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;

/**
 * Consider using {@code MockUnifiedRepository} instead.
 * <p>
 * Test utilities for {@code IUnifiedRepository}. Assists with two phases of mocking: stubbing and verification.
 * Uses the <a href="http://code.google.com/p/mockito/">Mockito</a> mocking framework.
 * 
 * <p>
 * Stubbing Examples:
 * </p>
 * <p>
 * 1. Stub getFile and getDataForRead:
 * </p>
 * 
 * <pre>
 * stubGetFile( repo, &quot;/public/file.txt&quot; );
 * stubGetData( repo, &quot;/public/file.txt&quot;, &quot;abcdefg&quot; );
 * // do test after stubbing
 * </pre>
 * 
 * <p>
 * Verification Examples:
 * </p>
 * <p>
 * 1. Verify that createFile was called:
 * </p>
 * 
 * <pre>
 * // do test before verifying
 * verify( repo ).createFile( eq( publicFolderId ),
 *     argThat( isLikeFile( new RepositoryFile.Builder( fileName ).build() ) ),
 *     argThat( hasData( bytes, APPLICATION_OCTET_STREAM ) ), anyString() );
 * </pre>
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class UnifiedRepositoryTestUtils {

  /**
   * Generates an ID from the given path.
   */
  public static Serializable makeIdObject( final String path ) {
    // paths are unique; so we just need to differentiate between paths and ids
    return "id(" + path + ")";
  }

  /**
   * Stubs a {@code createFile} call.
   */
  public static void stubCreateFile( final IUnifiedRepository repo, final String path ) {
    final String parentPath = StringUtils.substringBeforeLast( path, RepositoryFile.SEPARATOR );
    doReturn( makeFileObject( path, true ) ).when( repo ).createFile( eq( makeIdObject( parentPath ) ),
        argThat( isLikeFile( makeFileObject( path, false ) ) ), any( IRepositoryFileData.class ), anyString() );
  }

  /**
   * Stubs a {@code createFolder} call.
   */
  public static void stubCreateFolder( final IUnifiedRepository repo, final String path ) {
    final String parentPath = StringUtils.substringBeforeLast( path, RepositoryFile.SEPARATOR );
    doReturn( makeFileObject( path, true ) ).when( repo ).createFolder( eq( makeIdObject( parentPath ) ),
        argThat( isLikeFile( makeFolderObject( path, false ) ) ), anyString() );
  }

  /**
   * Creates a {@code RepositoryFile}.
   */
  public static RepositoryFile makeFileObject( final String path ) {
    return makeFileObject( path, false );
  }

  /**
   * Creates a {@code RepositoryFile}. If {@code fromRepo} is {@code true}, then it is populated as if it had been
   * returned from the repository (e.g. non-null ID).
   */
  public static RepositoryFile makeFileObject( final String path, final boolean fromRepo ) {
    final String fileName = StringUtils.substringAfterLast( path, RepositoryFile.SEPARATOR );
    RepositoryFile.Builder b = new RepositoryFile.Builder( fileName );
    // files returned from repo have non-null id and path properties
    if ( fromRepo ) {
      b.id( makeIdObject( path ) ).path( path );
    }
    return b.build();
  }

  /**
   * Same as {@link #makeFileObject(String)} except that the file is a folder.
   */
  public static RepositoryFile makeFolderObject( final String path ) {
    return makeFolderObject( path, false );
  }

  /**
   * Same as {@link #makeFileObject(String, boolean)} except that the file is a folder.
   */
  public static RepositoryFile makeFolderObject( final String path, final boolean fromRepo ) {
    RepositoryFile.Builder b = new RepositoryFile.Builder( makeFileObject( path, fromRepo ) );
    b.folder( true );
    return b.build();
  }

  /**
   * Stubs a {@code getFile} call.
   */
  public static void stubGetFile( final IUnifiedRepository repo, final String path ) {
    stubGetFile( repo, path, false );
  }

  /**
   * Stubs a {@code getFile} call where the resulting {@code RepositoryFile} should be a folder.
   */
  public static void stubGetFolder( final IUnifiedRepository repo, final String path ) {
    stubGetFile( repo, path, true );
  }

  /**
   * Stubs a {@code getFile} call.
   */
  private static void stubGetFile( final IUnifiedRepository repo, final String path, final boolean folder ) {
    final String fileName = StringUtils.substringAfterLast( path, RepositoryFile.SEPARATOR );
    RepositoryFile file =
        new RepositoryFile.Builder( makeIdObject( path ), fileName ).path( path ).folder( folder ).build();
    doReturn( file ).when( repo ).getFile( path );
  }

  /**
   * Stubs a {@code getChildren} call. {@code childrenNames} is zero or more file/folder names. A folder is
   * indicated by a trailing forward slash.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * stubGetChildren( repo, &quot;/public&quot;, &quot;hello/&quot;, &quot;file1.txt&quot; );
   * </pre>
   */

  public static void stubGetChildren( final IUnifiedRepository repo, RepositoryRequest request, final String... childrenNames ) {
    List<RepositoryFile> children = new ArrayList<RepositoryFile>( childrenNames.length );
    for ( String childName : childrenNames ) {
      if ( childName.startsWith( RepositoryFile.SEPARATOR ) ) {
        throw new IllegalArgumentException( "child names must not begin with a forward slash" );
      }
      final String fullChildPath =
          request.getPath()
              + RepositoryFile.SEPARATOR
              + ( childName.endsWith( RepositoryFile.SEPARATOR ) ? StringUtils.substringBefore( childName,
                  RepositoryFile.SEPARATOR ) : childName );
      RepositoryFile child = null;
      if ( childName.endsWith( RepositoryFile.SEPARATOR ) ) {
        child = makeFolderObject( fullChildPath, true );
      } else {
        child = makeFileObject( fullChildPath, true );
      }
      children.add( child );
    }
    doReturn( children ).when( repo ).getChildren( request );

  }

  /**
   * Stubs a {@code getChildren} call. {@code childrenNames} is zero or more file/folder names. A folder is
   * indicated by a trailing forward slash.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * stubGetChildren( repo, &quot;/public&quot;, &quot;hello/&quot;, &quot;file1.txt&quot; );
   * </pre>
   */
  public static void stubGetChildren( final IUnifiedRepository repo, final String path,
                                      final String... childrenNames ) {
    List<RepositoryFile> children = new ArrayList<RepositoryFile>( childrenNames.length );
    for ( String childName : childrenNames ) {
      if ( childName.startsWith( RepositoryFile.SEPARATOR ) ) {
        throw new IllegalArgumentException( "child names must not begin with a forward slash" );
      }
      final String fullChildPath =
          path
              + RepositoryFile.SEPARATOR
              + ( childName.endsWith( RepositoryFile.SEPARATOR ) ? StringUtils.substringBefore( childName,
                  RepositoryFile.SEPARATOR ) : childName );
      RepositoryFile child = null;
      if ( childName.endsWith( RepositoryFile.SEPARATOR ) ) {
        child = makeFolderObject( fullChildPath, true );
      } else {
        child = makeFileObject( fullChildPath, true );
      }
      children.add( child );
    }
    doReturn( children ).when( repo ).getChildren( makeIdObject( path ) );
  }

  /**
   * Stubs a {@code getDataForRead} call. The encoding is always {@code UTF-8}. The MIME type is auto-detected.
   */
  public static void stubGetData( final IUnifiedRepository repo, final String path, final String text ) {
    final String encoding = "UTF-8";
    byte[] bytes;
    try {
      bytes = text.getBytes( encoding );
    } catch ( UnsupportedEncodingException e ) {
      throw new RuntimeException();
    }
    doReturn( new AutoResetSimpleRepositoryFileData( new ByteArrayInputStream( bytes ),
      encoding, getMimeType( path ) ) )
        .when( repo ).getDataForRead( makeIdObject( path ), SimpleRepositoryFileData.class );
  }

  /**
   * Stubs a {@code getFile} call with a return value of {@code null}. While the mock framework will do this by
   * default, this method makes the stubbed call more explicit.
   */
  public static void stubGetFileDoesNotExist( final IUnifiedRepository repo, final String path ) {
    doReturn( null ).when( repo ).getFile( path );
  }

  /**
   * Stubs a {@code getDataForRead} call. The pairs specified will be used to construct a
   * {@code NodeRepositoryFileData} .
   */
  public static void stubGetData( final IUnifiedRepository repo, final String path, final String rootNodeName,
      final PathPropertyPair... pairs ) {
    final String prefix = RepositoryFile.SEPARATOR + rootNodeName;
    DataNode rootNode = new DataNode( rootNodeName );
    for ( PathPropertyPair pair : pairs ) {
      if ( !pair.getPath().startsWith( prefix ) ) {
        throw new IllegalArgumentException( "all paths must have a common prefix" );
      }
      String[] pathSegments = pair.getPath().substring( prefix.length() + 1 ).split( "/" );
      addChild( rootNode, pair.getProperty(), pathSegments, 0 );
    }
    doReturn( new NodeRepositoryFileData( rootNode ) ).when( repo ).getDataForRead( makeIdObject( path ),
        NodeRepositoryFileData.class );
  }

  private static void addChild( final DataNode rootNode, final DataProperty property, final String[] pathSegments,
      final int currentSegmentIndex ) {
    int currentSegmentIdx = currentSegmentIndex;
    if ( rootNode.hasNode( pathSegments[currentSegmentIdx] ) ) {
      addChild( rootNode.getNode( pathSegments[currentSegmentIdx] ), property, pathSegments, ++currentSegmentIdx );
      return;
    }
    if ( ( currentSegmentIdx < pathSegments.length - 1 )
        || ( currentSegmentIdx == pathSegments.length - 1 && property == null ) ) {
      rootNode.addNode( pathSegments[currentSegmentIdx] );
    } else {
      switch ( property.getType() ) {
        case BOOLEAN:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getBoolean() );
          break;
        case DATE:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getDate() );
          break;
        case DOUBLE:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getDouble() );
          break;
        case LONG:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getLong() );
          break;
        case REF:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getRef() );
          break;
        default:
          rootNode.setProperty( pathSegments[currentSegmentIdx], property.getString() );
          break;
      }
    }
    if ( currentSegmentIdx < pathSegments.length - 1 ) {
      addChild( rootNode.getNode( pathSegments[currentSegmentIdx] ), property, pathSegments, ++currentSegmentIdx );
    }
  }

  /**
   * Stubs a {@code getTree} call. {@code rootPath} is the root of the tree to return. {@code paths} is zero or
   * more paths, relative to {@code rootPath} (i.e. they must not begin with a forward slash). Intermediate folders
   * are created automatically. To specify an empty folder, end the path with a forward slash.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * stubGetTree( repo, &quot;/public&quot;, &quot;relUrlTest.url&quot;, &quot;hello/file.txt&quot;,
   * &quot;hello/file2.txt&quot;, &quot;hello2/&quot; );
   * </pre>
   */
  public static void stubGetTree( final IUnifiedRepository repo, final String rootPath,
                                  final String... paths ) {
    RepositoryFileTree.Builder root = new RepositoryFileTree.Builder( makeFolderObject( rootPath, true ) );
    for ( String path : paths ) {
      if ( path.startsWith( RepositoryFile.SEPARATOR ) ) {
        throw new IllegalArgumentException( "all paths must be relative" );
      }
      String[] pathSegments = path.split( RepositoryFile.SEPARATOR );
      boolean leafIsFolder = path.endsWith( RepositoryFile.SEPARATOR );
      addChild( root, leafIsFolder, pathSegments, 0 );
    }
    doReturn( root.build() ).when( repo ).getTree( eq( rootPath ), anyInt(), anyString(), anyBoolean() );
  }

  /**
   * Helper method to construct a {@link RepositoryFileTree} from a list of paths.
   */
  private static void addChild( final RepositoryFileTree.Builder root, final boolean leafIsFolder,
      final String[] pathSegments, final int currentSegmentIndex ) {
    int currentSegmentIdx = currentSegmentIndex;
    for ( RepositoryFileTree.Builder child : root.getChildren() ) {
      if ( child.getFile().isFolder() && child.getFile().getName().equals( pathSegments[currentSegmentIdx] ) ) {
        if ( currentSegmentIdx < pathSegments.length - 1 ) {
          addChild( child, leafIsFolder, pathSegments, ++currentSegmentIdx );
          return;
        }
      }
    }
    String reconstructedPath =
        root.getFile().getPath() + RepositoryFile.SEPARATOR
            + StringUtils.join( Arrays.copyOfRange( pathSegments, 0, currentSegmentIdx + 1 ),
              RepositoryFile.SEPARATOR );
    RepositoryFile file = null;
    if ( ( currentSegmentIdx < pathSegments.length - 1 )
        || ( currentSegmentIdx == pathSegments.length - 1 && leafIsFolder ) ) {
      file = makeFolderObject( reconstructedPath, true );
    } else {
      file = makeFileObject( reconstructedPath, true );
    }
    RepositoryFileTree.Builder newChild = new RepositoryFileTree.Builder( file );
    root.child( newChild );
    if ( currentSegmentIdx < pathSegments.length - 1 ) {
      addChild( newChild, leafIsFolder, pathSegments, ++currentSegmentIdx );
    }
  }

  /**
   * A {@link SimpleRepositoryFileData} that resets its stream every time the stream is requested.
   */
  @SuppressWarnings( "serial" )
  private static class AutoResetSimpleRepositoryFileData extends SimpleRepositoryFileData {

    public AutoResetSimpleRepositoryFileData( final InputStream stream, final String encoding, final String mimeType ) {
      super( stream, encoding, mimeType );
      if ( !stream.markSupported() ) {
        throw new RuntimeException( "mark() must be supported" );
      }
      stream.mark( Integer.MAX_VALUE );
    }

    @Override
    public InputStream getStream() {
      InputStream stream = super.getStream();
      try {
        stream.reset();
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
      return stream;
    }

  }

  /**
   * Helper method to determine MIME type given a path.
   */
  private static String getMimeType( final String path ) {
    String mimeType = MimeHelper.getMimeTypeFromFileName( path );
    if ( mimeType == null ) {
      return "text/plain";
    } else {
      return mimeType;
    }
  }

  /**
   * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matcher for {@link NodeRepositoryFileData}. Use
   * factory method to create.
   */
  private static class NodeRepositoryFileDataMatcher extends TypeSafeMatcher<NodeRepositoryFileData> {

    private static final String shortName = "hasData";

    private PathPropertyPair[] pairs;

    public NodeRepositoryFileDataMatcher( PathPropertyPair... pairs ) {
      for ( PathPropertyPair pair : pairs ) {
        checkPath( pair.getPath() );
      }
      // defensive copy
      this.pairs = Arrays.copyOf( pairs, pairs.length );
    }

    @Override
    public boolean matchesSafely( final NodeRepositoryFileData data ) {
      for ( PathPropertyPair pair : pairs ) {
        DataProperty expectedProperty = pair.getProperty();
        String[] pathSegments = pair.getPath().substring( 1 ).split( "/" );
        DataNode currentNode = data.getNode();
        if ( !currentNode.getName().equals( pathSegments[0] ) ) {
          return false;
        }
        for ( int i = 1; i < pathSegments.length - 1; i++ ) {
          currentNode = currentNode.getNode( pathSegments[i] );
          if ( currentNode == null ) {
            return false;
          }
        }
        DataProperty actualProperty = currentNode.getProperty( pathSegments[pathSegments.length - 1] );
        if ( !expectedProperty.equals( actualProperty ) ) {
          return false;
        }
      }
      return true;
    }

    @Override
    public void describeTo( final Description description ) {
      description.appendText( shortName );
      description.appendText( "(" );
      description.appendText( "pathPropertyPairs=" );
      description.appendText( Arrays.toString( pairs ) );
      description.appendText( ")" );
    }

  }

  /**
   * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matcher for {@link SimpleRepositoryFileData}. Use
   * factory method to create.
   */
  private static class SimpleRepositoryFileDataMatcher extends TypeSafeMatcher<SimpleRepositoryFileData> {

    private static final String shortName = "hasData";

    private String expectedMimeType;

    private String expectedEncoding;

    private byte[] expectedBytes;

    public SimpleRepositoryFileDataMatcher( final byte[] expectedBytes, final String expectedEncoding,
        final String expectedMimeType ) {
      this.expectedBytes = expectedBytes;
      this.expectedEncoding = expectedEncoding;
      this.expectedMimeType = expectedMimeType;
    }

    @Override
    public boolean matchesSafely( final SimpleRepositoryFileData data ) {
      return streamMatches( data.getStream() ) && ObjectUtils.equals( expectedMimeType, data.getMimeType() )
          && ObjectUtils.equals( expectedEncoding, data.getEncoding() );
    }

    private boolean streamMatches( final InputStream stream ) {
      if ( stream == null ) {
        return expectedBytes.length == 0;
      }
      if ( !stream.markSupported() ) {
        throw new RuntimeException( "cannot test for match on stream that cannot be reset" );
      }
      stream.mark( Integer.MAX_VALUE );
      byte[] actualBytes = null;
      try {
        actualBytes = IOUtils.toByteArray( stream );
        stream.reset(); // leave it like we found it
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
      return Arrays.equals( expectedBytes, actualBytes );
    }

    public void describeTo( final Description description ) {
      final int MAX_EXCERPT_LENGTH = 10;

      description.appendText( shortName );
      description.appendText( "(" );

      if ( StringUtils.isNotBlank( expectedEncoding ) ) {
        description.appendText( "text=" );

        String text = null;
        try {
          text = new String( expectedBytes, expectedEncoding );
        } catch ( UnsupportedEncodingException e ) {
          throw new RuntimeException( e );
        }
        description.appendText( head( text, MAX_EXCERPT_LENGTH ) );
        description.appendText( "," );
        description.appendText( "encoding=" );
        description.appendText( expectedEncoding );
      } else {
        description.appendText( "bytes=" );
        description.appendText( head( expectedBytes, MAX_EXCERPT_LENGTH ) );
      }
      description.appendText( "," );
      description.appendText( "mimeType=" );
      description.appendText( expectedMimeType );
      description.appendText( ")" );
    }

    /**
     * Returns at most {@code count} characters from {@code str}.
     */
    private String head( final String str, final int count ) {
      if ( str.length() > count ) {
        return str.substring( 0, count ) + "...";
      } else {
        return str;
      }
    }

    /**
     * Returns {@code String} representation of array consisting of at most {@code count} bytes from {@code bytes}.
     */
    private String head( final byte[] bytes, final int count ) {
      if ( bytes.length > count ) {
        StringBuilder buf = new StringBuilder();
        buf.append( "[" );
        for ( int i = 0; i < count; i++ ) {
          if ( i > 0 ) {
            buf.append( ", " );
          }
          buf.append( bytes[i] );
        }
        buf.append( "..." );
        buf.append( "]" );
        return buf.toString();
      } else {
        return Arrays.toString( bytes );
      }
    }

  }

  /**
   * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matcher for {@link RepositoryFileAcl}. Will only
   * attempt to match non-null properties. Use factory method to create.
   */
  private static class SelectiveRepositoryFileAclMatcher extends TypeSafeMatcher<RepositoryFileAcl> {

    private static final String shortName = "isLikeAcl";

    private RepositoryFileAcl expectedAcl;

    private boolean testAcesUsingEquals;

    /**
     * Creates an instance where {@code testAcesUsingEquals} is {@code false}.
     * 
     * @param expectedAcl
     */
    public SelectiveRepositoryFileAclMatcher( RepositoryFileAcl expectedAcl ) {
      this( expectedAcl, false );
    }

    /**
     * @param expectedAcl
     *          expected ACL
     * @param testAcesUsingEquals
     *          if {@code true}, use {@code acl.getAces().equals(expectedAcl.getAces())} else
     *          {@code acl.getAces().containsAll(expectedAcl.getAces())}
     */
    public SelectiveRepositoryFileAclMatcher( final RepositoryFileAcl expectedAcl, final boolean testAcesUsingEquals ) {
      super();
      this.expectedAcl = expectedAcl;
      this.testAcesUsingEquals = testAcesUsingEquals;
    }

    /*
     * RepositoryFileAcl.getAces() never returns null. Also, isEntriesInheriting is always tested.
     */
    @Override
    public boolean matchesSafely( final RepositoryFileAcl acl ) {
      return ( expectedAcl.getId() != null ? expectedAcl.getId().equals( acl.getId() ) : true )
          && expectedAcl.isEntriesInheriting() == acl.isEntriesInheriting()
          && ( testAcesUsingEquals ? acl.getAces().equals( expectedAcl.getAces() ) : acl.getAces().containsAll(
              expectedAcl.getAces() ) );
    }

    @Override
    public void describeTo( final Description description ) {
      boolean appended = false;
      description.appendText( shortName );
      description.appendText( "(" );
      if ( expectedAcl.getId() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "id=" );
        description.appendText( expectedAcl.getId().toString() );
        appended = true;
      }
      description.appendText( appended ? "," : "" );
      description.appendText( "isEntriesInheriting=" );
      description.appendText( String.valueOf( expectedAcl.isEntriesInheriting() ) );
      appended = true;
      if ( expectedAcl.getAces() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "aces=" );
        description.appendText( expectedAcl.getAces().toString() );
        appended = true;
      }
      description.appendText( ")" );
    }

  }

  /**
   * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matcher for {@link RepositoryFile}. Will only
   * attempt to match non-null properties. Use factory method to create.
   */
  private static class SelectiveRepositoryFileMatcher extends TypeSafeMatcher<RepositoryFile> {

    private static final String shortName = "isLikeFile";

    private RepositoryFile expectedFile;

    public SelectiveRepositoryFileMatcher( final RepositoryFile expectedFile ) {
      this.expectedFile = expectedFile;
    }

    /*
     * If you add comparisons here, add them in describeTo as well.
     */
    @Override
    public boolean matchesSafely( final RepositoryFile file ) {
      return ( expectedFile.getId() != null ? expectedFile.getId().equals( file.getId() ) : true )
          && ( expectedFile.getName() != null ? expectedFile.getName().equals( file.getName() ) : true )
          && ( expectedFile.getTitle() != null ? expectedFile.getTitle().equals( file.getTitle() ) : true )
          && ( expectedFile.getPath() != null ? expectedFile.getPath().equals( file.getPath() ) : true )
          && ( expectedFile.getCreatedDate() != null ? expectedFile.getCreatedDate().equals( file.getCreatedDate() )
              : true )
          && ( expectedFile.getLastModifiedDate() != null ? expectedFile.getLastModifiedDate().equals(
              file.getLastModifiedDate() ) : true )
          && ( expectedFile.getVersionId() != null ? expectedFile.getVersionId().equals( file.getVersionId() ) : true )
          && ( expectedFile.getDeletedDate() != null ? expectedFile.getDeletedDate().equals( file.getDeletedDate() )
              : true );
    }

    @Override
    public void describeTo( final Description description ) {
      boolean appended = false;
      description.appendText( shortName );
      description.appendText( "(" );
      if ( expectedFile.getId() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "id=" );
        description.appendText( expectedFile.getId().toString() );
        appended = true;
      }
      if ( expectedFile.getName() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "name=" );
        description.appendText( expectedFile.getName() );
        appended = true;
      }
      if ( expectedFile.getTitle() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "title=" );
        description.appendText( expectedFile.getTitle() );
        appended = true;
      }
      if ( expectedFile.getPath() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "path=" );
        description.appendText( expectedFile.getPath() );
        appended = true;
      }
      if ( expectedFile.getCreatedDate() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "createdDate=" );
        description.appendText( expectedFile.getCreatedDate().toString() );
        appended = true;
      }
      if ( expectedFile.getLastModifiedDate() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "lastModifiedDate=" );
        description.appendText( expectedFile.getLastModifiedDate().toString() );
        appended = true;
      }
      if ( expectedFile.getVersionId() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "versionId=" );
        description.appendText( expectedFile.getVersionId().toString() );
        appended = true;
      }
      if ( expectedFile.getDeletedDate() != null ) {
        description.appendText( appended ? "," : "" );
        description.appendText( "deletedDate=" );
        description.appendText( expectedFile.getDeletedDate().toString() );
        appended = true;
      }
      description.appendText( ")" );
    }

  }

  /**
   * Factory for binary {@code SimpleRepositoryFileData} matcher.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( simpleRepositoryFileData, hasData( byteArray, &quot;application/pdf&quot; ) );
   * </pre>
   * 
   * @param expectedBytes
   *          expected bytes
   * @param expectedMimeType
   *          expected MIME type
   * @return matcher
   */
  public static <T> Matcher<SimpleRepositoryFileData>
  hasData( final byte[] expectedBytes, final String expectedMimeType ) {
    return new SimpleRepositoryFileDataMatcher( expectedBytes, null, expectedMimeType );
  }

  /**
   * Factory for textual {@code SimpleRepositoryFileData} matcher.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( simpleRepositoryFileData, hasData( &quot;test123&quot;, &quot;UTF-8&quot;, &quot;text/plain&quot; ) );
   * </pre>
   * 
   * @param expectedText
   *          expected text
   * @param encoding
   *          expected encoding
   * @param expectedMimeType
   *          expected MIME type
   * @return matcher
   */
  public static <T> Matcher<SimpleRepositoryFileData> hasData( final String expectedText, final String encoding,
      final String expectedMimeType ) {
    byte[] expectedBytes = null;
    try {
      expectedBytes = expectedText.getBytes( encoding );
    } catch ( UnsupportedEncodingException e ) {
      throw new RuntimeException( e );
    }
    return new SimpleRepositoryFileDataMatcher( expectedBytes, encoding, expectedMimeType );
  }

  /**
   * Factory for {@code RepositoryFile} matcher. Only attempts to match on non-null properties.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( repositoryFile, isLikeFile( new RepositoryFile.Builder( &quot;123&quot;,
   * &quot;test.txt&quot; ).build() ) );
   * </pre>
   * 
   * @param expectedFile
   *          expected file
   * @return matcher
   */
  public static <T> Matcher<RepositoryFile> isLikeFile( final RepositoryFile expectedFile ) {
    return new SelectiveRepositoryFileMatcher( expectedFile );
  }

  /**
   * Factory for {@code RepositoryFileAcl} matcher. Only attempts to match on non-null properties.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( repositoryFileAcl, isLikeAcl( new RepositoryFileAcl.Builder( &quot;admin&quot;,
   * RepositoryFileSid.Type.USER ).ace(
   *     &quot;suzy&quot;, RepositoryFileSid.Type.USER, RepositoryFilePermission.WRITE ).build() ), true );
   * </pre>
   * 
   * @param expectedAcl
   *          expected ACL
   * @param testAcesUsingEquals
   *          if {@code true}, use {@code acl.getAces().equals(expectedAcl.getAces())} else
   *          {@code acl.getAces().containsAll(expectedAcl.getAces())}
   * @return
   */
  public static <T> Matcher<RepositoryFileAcl> isLikeAcl( final RepositoryFileAcl expectedAcl,
      final boolean testAcesUsingEquals ) {
    return new SelectiveRepositoryFileAclMatcher( expectedAcl, testAcesUsingEquals );
  }

  /**
   * Factory for {@code RepositoryFileAcl} matcher. Only attempts to match on non-null properties. Aces are tested
   * using {@code containsAll(Collection)}.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( repositoryFileAcl,
   *     isLikeAcl( new RepositoryFileAcl.Builder( &quot;admin&quot;, RepositoryFileSid.Type.USER ).build() ) );
   * </pre>
   * 
   * @param expectedAcl
   * @return
   */
  public static <T> Matcher<RepositoryFileAcl> isLikeAcl( final RepositoryFileAcl expectedAcl ) {
    return new SelectiveRepositoryFileAclMatcher( expectedAcl );
  }

  /**
   * Factory for {@code NodeRepositoryFileData} matcher. Only attempts to match pairs given.
   * 
   * <p>
   * Example:
   * </p>
   * 
   * <pre>
   * assertThat( nodeRepositoryFileData, hasData( pathPropertyPair( &quot;/databaseMeta/HOST_NAME&quot;,
   * &quot;localhost&quot; ) ) );
   * </pre>
   * 
   * @param pairs
   *          path property pairs
   * @return matcher
   */
  public static <T> Matcher<NodeRepositoryFileData> hasData( final PathPropertyPair... pairs ) {
    return new NodeRepositoryFileDataMatcher( pairs );
  }

  /**
   * Helper method that throws {@code IllegalArgumentException} if path does not meet certain criteria.
   * 
   * @param path
   *          path to check
   */
  private static void checkPath( final String path ) {
    if ( path == null ) {
      throw new IllegalArgumentException( "path cannot be null" );
    }
    if ( !path.startsWith( "/" ) ) {
      throw new IllegalArgumentException( "paths must start with a slash" );
    }
    if ( path.endsWith( "/" ) ) {
      throw new IllegalArgumentException( "paths must not end with a slash" );
    }
    if ( path.trim().equals( "/" ) ) {
      throw new IllegalArgumentException( "path must be path to property" );
    }
  }

  /**
   * Factory for {@link PathPropertyPair} instances. Creates a path property pair that represents an empty node.
   */
  public static PathPropertyPair emptyNode( final String path ) {
    checkPath( path );
    return new PathPropertyPair( path, null );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final String value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.STRING ) );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final boolean value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.BOOLEAN ) );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final long value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.LONG ) );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final double value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.DOUBLE ) );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final Date value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.DATE ) );
  }

  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair( final String path, final Serializable value ) {
    checkPath( path );
    String[] pathSegments = path.split( "/" );
    return new PathPropertyPair( path, new DataProperty( pathSegments[pathSegments.length - 1], value,
        DataPropertyType.REF ) );
  }

  /**
   * A path and property pair. A {@code null} property represents a node.
   */
  public static class PathPropertyPair {

    private String path;

    private DataProperty property;

    /**
     * Constructs a path property pair. If {@code property} is {@code null} then this path represents a node.
     */
    public PathPropertyPair( final String path, final DataProperty property ) {
      this.path = path;
      this.property = property;
    }

    private String getPath() {
      return path;
    }

    private DataProperty getProperty() {
      return property;
    }

    @Override
    public String toString() {
      return "PathPropertyPair [path=" + path + ", property=" + property + "]";
    }

  }

}
