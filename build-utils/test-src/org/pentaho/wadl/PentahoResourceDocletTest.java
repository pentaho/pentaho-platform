package org.pentaho.wadl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.wadl.helpers.MockRootDoc;

public class PentahoResourceDocletTest {

  private final String EXPECTED_WADL_FILE_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + "<resourceDoc><classDocs><classDoc>"
          + "<methodDocs><methodDoc><methodName>methodName</methodName><commentText><![CDATA[<supported>true"
          + "</supported><deprecated>true</deprecated><documentation>null</documentation>]]></commentText>"
          + "</methodDoc></methodDocs></classDoc></classDocs></resourceDoc>" + System.lineSeparator();

  private final String FILE_NAME = "wadlExtension.xml";

  @Test
  public void testStart() {
    PentahoResourceDoclet doclet = new PentahoResourceDoclet();
    MockRootDoc rootDoc = new MockRootDoc();
    doclet.start( rootDoc );

    String valueFromFile = readFromFile();
    Assert.assertEquals( EXPECTED_WADL_FILE_CONTENT, valueFromFile );
  }

  private String readFromFile() {
    try {
      BufferedReader reader = new BufferedReader( new FileReader( FILE_NAME ) );
      StringBuilder strBuilder = new StringBuilder();

      String line = reader.readLine();
      while ( line != null ) {
        strBuilder.append( line );
        strBuilder.append( System.lineSeparator() );
        line = reader.readLine();
      }
      return strBuilder.toString();
    } catch ( FileNotFoundException e ) {
      Assert.fail( "expected file not found" );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return null;
  }

}
