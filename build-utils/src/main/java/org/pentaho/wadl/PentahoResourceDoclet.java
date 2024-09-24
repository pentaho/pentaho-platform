/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.wadl;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.jersey.wadl.resourcedoc.DocProcessorWrapper;
import com.sun.jersey.wadl.resourcedoc.ResourceDoclet;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

public class PentahoResourceDoclet extends ResourceDoclet {
  private static final String OUTPUT_FILE_NAME = "wadlExtension.xml";
  private static final String OUTPUT_PATH_PARAM = "-output";

  private static final String SUPPORTED_TAG = "<supported>{0}</supported>";
  private static final String DEPRECATED_TAG = "<deprecated>{0}</deprecated>";
  private static final String DOCUMENTATION_TAG = "<documentation>{0}</documentation>";

  private static final String PATH_ANNOTATION = "@javax.ws.rs.Path";
  private static final String DEPRECATED_ANNOTATION = "@java.lang.Deprecated";
  private static final String FACET_ANNOTATION = "@org.codehaus.enunciate.Facet";

  private static final String UNSUPPORTED = "Unsupported";

  private static boolean isDeprecated( AnnotationDesc[] annotationDescs ) {
    for ( AnnotationDesc annotationDesc : annotationDescs ) {
      if ( annotationDesc.toString().equals( DEPRECATED_ANNOTATION ) ) {
        return true;
      }
    } return false;
  }

  private static boolean isSupported( AnnotationDesc[] annotationDescs ) {
    for ( AnnotationDesc annotationDesc : annotationDescs ) {
      if ( annotationDesc.toString().contains( FACET_ANNOTATION ) ) {
        if ( annotationDesc.elementValues()[0].value().value().equals( UNSUPPORTED ) ) {
          return false;
        }
      }
    } return true;
  }

  private static String generateComment( MethodDoc methodDoc ) {
    String documentation = methodDoc.getRawCommentText();
    StringBuilder comment = new StringBuilder();
    comment.append( MessageFormat.format( SUPPORTED_TAG, isSupported( methodDoc.annotations() ) ) )
        .append( MessageFormat.format( DEPRECATED_TAG, isDeprecated( methodDoc.annotations() ) ) )
        .append( MessageFormat.format( DOCUMENTATION_TAG, documentation ) );

    return comment.toString();
  }

  public static boolean start( RootDoc root ) {
    final String outputPath = getOutputPath( root.options() );
    final DocProcessorWrapper docProcessor = new DocProcessorWrapper();

    final ResourceDocType result = new ResourceDocType();
    final ClassDoc[] classes = root.classes();

    for ( ClassDoc classDoc : classes ) {
      final ClassDocType classDocType = new ClassDocType();

      AnnotationDesc[] annotationDescs = classDoc.annotations();
      boolean toBeProcessed = false;
      if ( annotationDescs != null ) {

        for ( AnnotationDesc annotation : annotationDescs ) {
          if ( annotation.toString().contains( PATH_ANNOTATION ) ) {
            toBeProcessed = true;
          }
        }

        if ( !toBeProcessed ) {
          continue;
        }

        classDocType.setClassName( classDoc.qualifiedTypeName() );
        classDocType.setCommentText( classDoc.commentText() );
        docProcessor.processClassDoc( classDoc, classDocType );

        for ( MethodDoc methodDoc : classDoc.methods() ) {
          final MethodDocType methodDocType = new MethodDocType();

          annotationDescs = methodDoc.annotations();
          toBeProcessed = false;
          if( annotationDescs != null ) {
            for ( AnnotationDesc annotation : annotationDescs ) {
              if ( annotation.toString().contains( PATH_ANNOTATION ) ) {
                toBeProcessed = true;
              }
            }

            if ( !toBeProcessed ) {
              continue;
            }

            methodDocType.setMethodName( methodDoc.name() );
            methodDocType.setCommentText( generateComment( methodDoc ) );

            docProcessor.processMethodDoc( methodDoc, methodDocType );
            classDocType.getMethodDocs().add( methodDocType );
          }
        }
      } result.getDocs().add( classDocType );
    }

    try {
      final Class<?>[] clazzes = { result.getClass() };
      final JAXBContext c = JAXBContext.newInstance( clazzes );
      final Marshaller m = c.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
      final OutputStream out = new BufferedOutputStream( new FileOutputStream( outputPath ) );
      final XMLSerializer serializer = getXMLSerializer( out );
      m.marshal( result, serializer );
      out.close();
    } catch ( Exception e ) {
      return false;
    }

    return true;
  }

  private static XMLSerializer getXMLSerializer( OutputStream os )
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    OutputFormat of = new OutputFormat();
    of.setCDataElements( new String[] { "ns1^commentText", "ns2^commentText", "^commentText" } );
    XMLSerializer serializer = new XMLSerializer( of );
    serializer.setOutputByteStream( os );
    return serializer;
  }

  private static String getOutputPath( String[][] optionsMap ) {
    if ( optionsMap != null ) {
      for ( int i = 0; i < optionsMap.length; i++ ) {
        String[] option = optionsMap[i];

        if ( option[0].equals( OUTPUT_PATH_PARAM ) ) {
          return option[1];
        }
      }
    }

    return OUTPUT_FILE_NAME;
  }

}
