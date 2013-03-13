package org.pentaho.platform.repository2.unified.webservices;

import java.io.StringReader;
import java.io.StringWriter;
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
		RepositoryFile empty = new RepositoryFile.Builder("Empty").build();
		RepositoryFileTree emptyDir = new RepositoryFileTree(empty, Collections.<RepositoryFileTree>emptyList());
		// to DTO
		RepositoryFileTreeAdapter adapter = new RepositoryFileTreeAdapter();
		RepositoryFileTreeDto dtoThere = adapter.marshal(emptyDir);
		assertNotNull(dtoThere.getChildren());
		// serialize
		final JAXBContext jaxbContext = JAXBContext.newInstance(RepositoryFileTreeDto.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
	    StringWriter sw = new StringWriter();
	    marshaller.marshal(dtoThere, sw);
	    // and bring it back
	    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	    StringReader sr = new StringReader(sw.toString());
	    RepositoryFileTreeDto dtoBackAgain = (RepositoryFileTreeDto) unmarshaller.unmarshal(sr);
	    assertNotNull(dtoBackAgain.getChildren());
	    // unmarshall
	    RepositoryFileTree stillEmptyDir = adapter.unmarshal(dtoBackAgain);
	    assertNotNull(stillEmptyDir.getChildren());
	    assertEquals(emptyDir, stillEmptyDir);
	  }
	
}
