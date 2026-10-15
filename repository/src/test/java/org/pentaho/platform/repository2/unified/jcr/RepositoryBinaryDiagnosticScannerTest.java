package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.jackrabbit.api.JackrabbitValue;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.junit.Test;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class RepositoryBinaryDiagnosticScannerTest {
  private final Log logger = mock( Log.class );

  @Test
  public void continuesAfterBadValueAndReportsIdentifierWithoutReadingContent() throws Exception {
    Value broken = mock( Value.class, withSettings().extraInterfaces( JackrabbitValue.class ) );
    when( ( (JackrabbitValue) broken ).getContentIdentity() ).thenReturn( "missing-record-id" );
    Binary brokenBinary = mock( Binary.class );
    when( broken.getBinary() ).thenReturn( brokenBinary );
    when( brokenBinary.getSize() ).thenReturn( -1L );
    Value healthy = value( 12 );
    Node brokenNode = node( "/broken" );
    Node healthyNode = node( "/healthy" );
    properties( brokenNode, property( "/broken/data", broken ) );
    properties( healthyNode, property( "/healthy/data", healthy ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( node( "/", brokenNode, healthyNode ) );

    verify( healthy.getBinary() ).getSize();
    verify( brokenBinary ).dispose();
    verify( healthy.getBinary() ).dispose();
    verify( brokenBinary, never() ).getStream();
    verify( broken, never() ).getString();
    verify( logger ).warn( contains( "property=[/broken/data], nodeId=[/broken-id], file=[/broken], valueIndex=0,"
      + " dataStoreId=[missing-record-id]" ) );
    verifySummary( 3, 2, 1, 0 );
  }

  @Test
  public void continuesThroughMultipleValuesAndPreservesExceptions() throws Exception {
    Value unavailable = mock( Value.class );
    RepositoryException failure = new RepositoryException( "Datastore unavailable" );
    when( unavailable.getBinary() ).thenThrow( failure );
    Value negative = value( -1 );
    Value healthy = value( 12 );
    Node root = node( "/" );
    properties( root, property( "/data", unavailable, negative, healthy ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( root );

    verify( logger ).warn( contains( "valueIndex=0" ), same( failure ) );
    verify( logger ).warn( contains( "valueIndex=1" ) );
    verify( healthy.getBinary() ).getSize();
    verify( negative.getBinary() ).dispose();
    verifySummary( 1, 1, 2, 0 );
  }

  @Test
  public void continuesAfterPropertyCannotBeLoaded() throws Exception {
    Property unavailable = property( "/broken", value( 1 ) );
    RepositoryException failure = new RepositoryException( "Cannot load property" );
    when( unavailable.getValue() ).thenThrow( failure );
    Value healthy = value( 12 );
    Node root = node( "/" );
    properties( root, unavailable, property( "/healthy", healthy ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( root );

    verify( healthy.getBinary() ).getSize();
    verify( logger ).warn( contains( "Cannot inspect property [/broken]" ), same( failure ) );
    verifySummary( 1, 2, 0, 1 );
  }

  @Test
  public void continuesToChildrenAfterPropertyIteratorFails() throws Exception {
    Node child = node( "/child" );
    Node root = node( "/", child );
    PropertyIterator iterator = mock( PropertyIterator.class );
    when( root.getProperties() ).thenReturn( iterator );
    when( iterator.hasNext() ).thenReturn( true );
    IllegalStateException failure = new IllegalStateException( "Iterator failed" );
    when( iterator.nextProperty() ).thenThrow( failure );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( root );

    verify( child ).getProperties();
    verify( logger ).warn( contains( "Property iteration failed" ), same( failure ) );
    verifySummary( 2, 0, 0, 1 );
  }

  @Test
  public void continuesToAncestorSiblingAfterChildIteratorFails() throws Exception {
    Node broken = node( "/broken" );
    Node sibling = node( "/sibling" );
    NodeIterator iterator = mock( NodeIterator.class );
    when( broken.getNodes() ).thenReturn( iterator );
    when( iterator.hasNext() ).thenReturn( true );
    IllegalStateException failure = new IllegalStateException( "Cannot load child" );
    when( iterator.nextNode() ).thenThrow( failure );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( node( "/", broken, sibling ) );

    verify( sibling ).getProperties();
    verify( iterator ).nextNode();
    verify( logger ).warn( contains( "Child iteration failed" ), same( failure ) );
    verifySummary( 3, 0, 0, 1 );
  }

  @Test
  public void continuesAfterChildrenCannotBeEnumerated() throws Exception {
    Node broken = node( "/broken" );
    Node sibling = node( "/sibling" );
    when( broken.getNodes() ).thenThrow( new RepositoryException( "No children available" ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( node( "/", broken, sibling ) );

    verify( sibling ).getProperties();
    verifySummary( 3, 0, 0, 1 );
  }

  @Test
  public void mapsContentNodeToFile() throws Exception {
    Node content = node( "/report/jcr:content" );
    Node file = node( "/report", content );
    properties( content, property( "/report/jcr:content/jcr:data", value( -1 ) ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( node( "/", file ) );

    verify( logger ).warn( contains( "file=[/report]" ) );
    verifySummary( 3, 1, 1, 0 );
  }

  @Test
  public void mapsVersionContentToLiveFile() throws Exception {
    Session session = mock( Session.class );
    Node liveFile = node( "/public/report" );
    when( session.getNodeByIdentifier( "frozen-file-id" ) ).thenReturn( liveFile );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( versionTree( session ) );

    verify( logger ).warn( contains( "file=[/public/report]" ) );
  }

  @Test
  public void reportsMissingVersionTarget() throws Exception {
    Session session = mock( Session.class );
    when( session.getNodeByIdentifier( "frozen-file-id" ) ).thenThrow( new ItemNotFoundException() );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( versionTree( session ) );

    verify( logger ).warn( contains( "file=[missing live node for frozenUuid=frozen-file-id]" ) );
  }

  @Test
  public void doesNotClassifyRepositoryLookupFailureAsMissingNode() throws Exception {
    Session session = mock( Session.class );
    when( session.getNodeByIdentifier( "frozen-file-id" ) ).thenThrow( new RepositoryException( "Offline" ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( versionTree( session ) );

    verify( logger ).warn( contains( "file=[unresolved; node=" ) );
    verify( logger, never() ).warn( contains( "missing live node" ) );
  }

  @Test
  public void releasesBinaryWhenSizeLookupThrows() throws Exception {
    Value value = value( 1 );
    RepositoryException failure = new RepositoryException( "Cannot determine size" );
    when( value.getBinary().getSize() ).thenThrow( failure );
    Node root = node( "/" );
    properties( root, property( "/data", value ) );

    new RepositoryBinaryDiagnosticScanner( logger ).scan( root );

    verify( value.getBinary() ).dispose();
    verify( logger ).warn( contains( "Unreadable binary" ), same( failure ) );
    verifySummary( 1, 1, 1, 0 );
  }

  private Node versionTree( Session session ) throws Exception {
    String frozenPath = "/jcr:system/jcr:versionStorage/history/1.0/jcr:frozenNode";
    Node content = node( frozenPath + "/jcr:content" );
    properties( content, property( frozenPath + "/jcr:content/jcr:data", value( -1 ) ) );
    Node frozen = node( frozenPath, content );
    when( frozen.hasProperty( "jcr:frozenUuid" ) ).thenReturn( true );
    Property uuid = mock( Property.class );
    when( uuid.getString() ).thenReturn( "frozen-file-id" );
    when( frozen.getProperty( "jcr:frozenUuid" ) ).thenReturn( uuid );
    when( frozen.getSession() ).thenReturn( session );
    return node( "/", node( "/jcr:system", node( "/jcr:system/jcr:versionStorage", frozen ) ) );
  }

  private Node node( String path, Node... children ) throws Exception {
    Node node = mock( Node.class );
    when( node.getPath() ).thenReturn( path );
    when( node.getIdentifier() ).thenReturn( path + "-id" );
    when( node.getName() ).thenReturn( path.substring( path.lastIndexOf( '/' ) + 1 ) );
    when( node.getDepth() ).thenReturn( "/".equals( path ) ? 0 : path.split( "/" ).length - 1 );
    when( node.getNodes() ).thenReturn( new NodeIteratorAdapter( Arrays.asList( children ) ) );
    when( node.getProperties() ).thenReturn( PropertyIteratorAdapter.EMPTY );
    for ( Node child : children ) {
      when( child.getParent() ).thenReturn( node );
    }
    return node;
  }

  private void properties( Node node, Property... properties ) throws Exception {
    when( node.getProperties() ).thenReturn( new PropertyIteratorAdapter( Arrays.asList( properties ) ) );
  }

  private Property property( String path, Value... values ) throws Exception {
    Property property = mock( Property.class );
    when( property.getPath() ).thenReturn( path );
    when( property.getType() ).thenReturn( PropertyType.BINARY );
    when( property.isMultiple() ).thenReturn( values.length != 1 );
    if ( values.length == 1 ) {
      when( property.getValue() ).thenReturn( values[ 0 ] );
    } else {
      when( property.getValues() ).thenReturn( values );
    }
    return property;
  }

  private Value value( long size ) throws Exception {
    Value value = mock( Value.class );
    Binary binary = mock( Binary.class );
    when( value.getBinary() ).thenReturn( binary );
    when( binary.getSize() ).thenReturn( size );
    return value;
  }

  private void verifySummary( long nodes, long properties, long unreadable, long incomplete ) {
    verify( logger ).warn( "Binary diagnostic scan finished: nodes=" + nodes + ", binaryProperties=" + properties
      + ", unreadableValues=" + unreadable + ", incompleteBranches=" + incomplete
      + ". Diagnostics did not modify content or sweep the datastore." );
  }
}
