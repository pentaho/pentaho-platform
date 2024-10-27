/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.wadl;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;

public class PentahoResourceDocletTest {

  private final String EXPECTED_WADL_FILE_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + "<resourceDoc><classDocs><classDoc>"
          + "<methodDocs><methodDoc><methodName>methodName</methodName><commentText><![CDATA[<supported>true"
          + "</supported><deprecated>true</deprecated><documentation>null</documentation>]]></commentText>"
          + "</methodDoc></methodDocs></classDoc></classDocs></resourceDoc>" + System.lineSeparator();

  private final String FILE_NAME = "wadlExtension.xml";

  private RootDoc rootDoc;
  private AnnotationTypeDoc annotationTypeDeprecated;
  private AnnotationDesc annotationDescDeprecated;
  private AnnotationDesc annotationDescPath;
  private MethodDoc methodDoc;
  private ClassDoc classDoc;

  @Before
  public void before() {
    annotationTypeDeprecated = mock( AnnotationTypeDoc.class );
    when( annotationTypeDeprecated.toString() ).thenReturn( "deprecated" );

    annotationDescDeprecated = mock( AnnotationDesc.class );
    when( annotationDescDeprecated.annotationType() ).thenReturn( annotationTypeDeprecated );
    when( annotationDescDeprecated.toString() ).thenReturn( "@java.lang.Deprecated" );

    annotationDescPath = mock( AnnotationDesc.class );
    when( annotationDescPath.annotationType() ).thenReturn( annotationTypeDeprecated );
    when( annotationDescPath.toString() ).thenReturn( "@jakarta.ws.rs.Path" );

    methodDoc = mock( MethodDoc.class );
    when( methodDoc.name() ).thenReturn( "methodName" );
    when( methodDoc.commentText() ).thenReturn( "comment text" );
    when( methodDoc.annotations() ).thenReturn( new AnnotationDesc[] { annotationDescDeprecated, annotationDescPath } );

    classDoc = mock( ClassDoc.class );
    when( classDoc.methods() ).thenReturn( new MethodDoc[] { methodDoc } );
    when( classDoc.annotations() ).thenReturn( new AnnotationDesc[] { annotationDescDeprecated, annotationDescPath } );

    rootDoc = mock( RootDoc.class );
    when( rootDoc.classes() ).thenReturn( new ClassDoc[] { classDoc } );
  }

  @Test
  public void testStart() {
    PentahoResourceDoclet.start( rootDoc );

    String valueFromFile = readFromFile();
    Assert.assertEquals( EXPECTED_WADL_FILE_CONTENT, valueFromFile );
  }

  private String readFromFile() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader( new FileReader( FILE_NAME ) );
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
    } finally {
      if ( reader != null ) {
        try {
          reader.close();
        } catch ( IOException e ) {
          // noop
        }
      }
    }
    return null;
  }
}
