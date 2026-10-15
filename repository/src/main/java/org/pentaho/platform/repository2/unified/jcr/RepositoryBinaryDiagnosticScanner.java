package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.jackrabbit.api.JackrabbitValue;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayDeque;
import java.util.Deque;

class RepositoryBinaryDiagnosticScanner {
  private static final String UNAVAILABLE = "unavailable";
  private final Log logger;
  private long nodesScanned;
  private long binaryPropertiesScanned;
  private long unreadableValues;
  private long incompleteBranches;

  RepositoryBinaryDiagnosticScanner( Log logger ) {
    this.logger = logger;
  }

  void scan( Node root ) {
    Deque<Frame> parents = new ArrayDeque<>();
    Node current = root;
    while ( current != null ) {
      nodesScanned++;
      inspectProperties( current );
      try {
        parents.push( new Frame( current, current.getNodes() ) );
      } catch ( RepositoryException | RuntimeException e ) {
        incomplete( current, "Cannot enumerate children", e );
      }
      current = null;
      while ( !parents.isEmpty() ) {
        Frame frame = parents.peek();
        try {
          if ( frame.children.hasNext() ) {
            current = frame.children.nextNode();
            break;
          }
          parents.pop();
        } catch ( RuntimeException e ) {
          incomplete( frame.parent, "Child iteration failed; remaining children could not be scanned", e );
          parents.pop();
        }
      }
    }
    logger.warn( "Binary diagnostic scan finished: nodes=" + nodesScanned + ", binaryProperties="
      + binaryPropertiesScanned + ", unreadableValues=" + unreadableValues + ", incompleteBranches="
      + incompleteBranches + ". Diagnostics did not modify content or sweep the datastore." );
  }

  private void inspectProperties( Node node ) {
    try {
      PropertyIterator properties = node.getProperties();
      while ( properties.hasNext() ) {
        inspectProperty( node, properties.nextProperty() );
      }
    } catch ( RepositoryException | RuntimeException e ) {
      incomplete( node, "Property iteration failed; remaining properties could not be scanned", e );
    }
  }

  private void inspectProperty( Node node, Property property ) {
    try {
      if ( property.getType() == PropertyType.BINARY ) {
        binaryPropertiesScanned++;
        if ( property.isMultiple() ) {
          Value[] values = property.getValues();
          for ( int index = 0; index < values.length; index++ ) {
            inspectValue( node, property, values[ index ], index );
          }
        } else {
          inspectValue( node, property, property.getValue(), 0 );
        }
      }
    } catch ( RepositoryException | RuntimeException e ) {
      incomplete( node, "Cannot inspect property [" + propertyPath( property ) + "]", e );
    }
  }

  private String dataStoreIdentifier( Value value ) {
    try {
      if ( value instanceof JackrabbitValue jackrabbitValue ) {
        String identity = jackrabbitValue.getContentIdentity();
        if ( identity != null ) {
          return identity;
        }
      }
    } catch ( RuntimeException e ) {
      return UNAVAILABLE;
    }
    return UNAVAILABLE;
  }

  private void inspectValue( Node node, Property property, Value value, int index ) {
    Binary binary = null;
    String identifier = dataStoreIdentifier( value );
    try {
      binary = value.getBinary();
      if ( binary.getSize() < 0 ) {
        unreadable( node, property, index, identifier, null );
      }
    } catch ( RepositoryException | RuntimeException e ) {
      unreadable( node, property, index, identifier, e );
    } finally {
      if ( binary != null ) {
        try {
          binary.dispose();
        } catch ( RuntimeException e ) {
          incomplete( node, "Cannot release diagnostic binary handle", e );
        }
      }
    }
  }

  private void unreadable( Node node, Property property, int index, String identifier, Exception cause ) {
    unreadableValues++;
    String message = "Unreadable binary: property=[" + propertyPath( property ) + "], nodeId=["
      + nodeIdentifier( node ) + "], file=[" + filePath( node ) + "], valueIndex=" + index
      + ", dataStoreId=[" + identifier + "]";
    if ( cause == null ) {
      logger.warn( message + ". Binary size unavailable; see BLOBInDataStore WARN logging for the datastore cause." );
    } else {
      logger.warn( message, cause );
    }
  }

  private void incomplete( Node node, String reason, Exception cause ) {
    incompleteBranches++;
    logger.warn( "Incomplete binary diagnostics at node [" + nodePath( node ) + "], nodeId=["
      + nodeIdentifier( node ) + "], file=[" + filePath( node ) + "]: " + reason, cause );
  }

  private String filePath( Node node ) {
    String contentOwner = nodePath( node );
    try {
      Node current = node;
      while ( current.getDepth() > 0 ) {
        if ( "jcr:frozenNode".equals( current.getName() ) && current.hasProperty( "jcr:frozenUuid" ) ) {
          return liveVersionPath( current );
        }
        if ( "jcr:content".equals( current.getName() ) ) {
          contentOwner = current.getParent().getPath();
        }
        current = current.getParent();
      }
      return contentOwner;
    } catch ( RepositoryException | RuntimeException e ) {
      return "unresolved; node=" + nodePath( node );
    }
  }

  private String liveVersionPath( Node frozen ) throws RepositoryException {
    String frozenUuid = frozen.getProperty( "jcr:frozenUuid" ).getString();
    try {
      return frozen.getSession().getNodeByIdentifier( frozenUuid ).getPath();
    } catch ( ItemNotFoundException e ) {
      return "missing live node for frozenUuid=" + frozenUuid;
    }
  }

  private String nodePath( Node node ) {
    try {
      return node.getPath();
    } catch ( RepositoryException | RuntimeException e ) {
      return UNAVAILABLE;
    }
  }

  private String nodeIdentifier( Node node ) {
    try {
      return node.getIdentifier();
    } catch ( RepositoryException | RuntimeException e ) {
      return UNAVAILABLE;
    }
  }

  private String propertyPath( Property property ) {
    try {
      return property.getPath();
    } catch ( RepositoryException | RuntimeException e ) {
      return UNAVAILABLE;
    }
  }

  private static class Frame {
    private final Node parent;
    private final NodeIterator children;

    private Frame( Node parent, NodeIterator children ) {
      this.parent = parent;
      this.children = children;
    }
  }
}
