package org.pentaho.platform.repository2.unified.webservices;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;


import junit.framework.TestCase;

public class RepositoryFileTreeAdapterTest extends TestCase {

  /**
   * Assert empty list in RepositoryFileTree#children survives full jaxb serialization roundtrip
   */
  @Test
  public void testBIServer7777() throws Exception
  {
    // file tree with empty children
    RepositoryFile empty = new RepositoryFile.Builder("empty").build();
    RepositoryFileTree emptyDir = new RepositoryFileTree(empty, Collections.<RepositoryFileTree>emptyList());
    RepositoryFile root = new RepositoryFile.Builder("rootDir").build();
    ArrayList<RepositoryFileTree> children = new ArrayList<RepositoryFileTree>(1);
    children.add(emptyDir);
    RepositoryFileTree rootDir = new RepositoryFileTree(root, children);
    // to DTO
    RepositoryFileTreeAdapter adapter = new RepositoryFileTreeAdapter();
    RepositoryFileTreeDto dtoThere = adapter.marshal(rootDir);
    assertNotNull(dtoThere.getChildren().get(0).getChildren());
    // serialize
    final JAXBContext jaxbContext = JAXBContext.newInstance(RepositoryFileTreeDto.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter sw = new StringWriter();
    marshaller.marshal(dtoThere, sw);
    // and bring it back
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    StringReader sr = new StringReader(sw.toString());
    RepositoryFileTreeDto dtoBackAgain = (RepositoryFileTreeDto) unmarshaller.unmarshal(sr);
    assertNotNull(dtoBackAgain.getChildren().get(0).getChildren());
    // unmarshall
    RepositoryFileTree rootDir2 = adapter.unmarshal(dtoBackAgain);
    assertNotNull(rootDir2.getChildren().get(0).getChildren());
    assertEquals(rootDir, rootDir2);
  }

}
