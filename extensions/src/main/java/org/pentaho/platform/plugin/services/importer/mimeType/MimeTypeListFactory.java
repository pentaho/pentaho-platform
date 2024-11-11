/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.plugin.services.importer.mimeType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.mimeType.bindings.ImportHandlerDto;
import org.pentaho.platform.plugin.services.importer.mimeType.bindings.ImportHandlerMimeTypeDefinitionsDto;
import org.pentaho.platform.plugin.services.importer.mimeType.bindings.MimeTypeDefinitionDto;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class MimeTypeListFactory {
  private static final Log log = LogFactory.getLog( MimeTypeListFactory.class );

  private ImportHandlerMimeTypeDefinitionsDto importHandlerMimeTypeDefinitions;

  MimeTypeListFactory( String sourceFile ) {
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath( "" ) + sourceFile;
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream( fullPath );
      importHandlerMimeTypeDefinitions = fromXml( inputStream );
    } catch ( FileNotFoundException e ) {
      log.error( "ImportHandlerMimeTypeDefinition File \"" + fullPath + "\" not found", e );
    } catch ( JAXBException e ) {
      log.error( "Could not marshal the ImportHandlerMimeTypeDefinition file \"" + fullPath + "\"", e );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( Exception e ) {
          log.error( e );
        }
      }
    }
  }

  private ImportHandlerMimeTypeDefinitionsDto fromXml( FileInputStream input ) throws JAXBException {
    SAXParserFactory secureSAXParserFactory;
    try {
      secureSAXParserFactory = XMLParserFactoryProducer.createSecureSAXParserFactory();
    } catch ( SAXNotSupportedException | SAXNotRecognizedException | ParserConfigurationException ex ) {
      throw new JAXBException( ex );
    }
    XMLReader xmlReader;
    try {
      xmlReader = secureSAXParserFactory.newSAXParser().getXMLReader();
      xmlReader.setFeature( "http://xml.org/sax/features/namespaces", true );
    } catch ( SAXException | ParserConfigurationException ex ) {
      throw new JAXBException( ex );
    }
    Source xmlSource = new SAXSource( xmlReader, new InputSource( input ) );
    JAXBContext jc = JAXBContext.newInstance( "org.pentaho.platform.plugin.services.importer.mimeType.bindings" );
    Unmarshaller u = jc.createUnmarshaller();

    JAXBElement<ImportHandlerMimeTypeDefinitionsDto> o = (JAXBElement) ( u.unmarshal( xmlSource ) );
    ImportHandlerMimeTypeDefinitionsDto mimeTypeDefinitions = o.getValue();
    return mimeTypeDefinitions;
  }

  public List<IMimeType> createMimeTypeList( String handlerClass ) {
    List<IMimeType> mimeTypeList = new ArrayList<IMimeType>();
    for ( ImportHandlerDto importHandler : importHandlerMimeTypeDefinitions.getImportHandler() ) {
      if ( importHandler.getClazz().equals( handlerClass ) ) {
        for ( MimeTypeDefinitionDto mimeTypeDef : importHandler.getMimeTypeDefinitions().getMimeTypeDefinition() ) {
          MimeType mimeType = new MimeType( mimeTypeDef.getMimeType(), mimeTypeDef.getExtension() );
          mimeType.setHidden( mimeTypeDef.isHidden() );
          mimeType.setLocale( mimeTypeDef.isLocale() );
          mimeType.setVersionEnabled( mimeTypeDef.isVersionEnabled() );
          mimeType.setVersionCommentEnabled( mimeTypeDef.isVersionCommentEnabled() );

          Converter converter = null;
          String converterBeanName = mimeTypeDef.getConverter() != null && !mimeTypeDef.getConverter().isEmpty()
              ? mimeTypeDef.getConverter() : "streamConverter";
          converter = PentahoSystem.get( Converter.class, /*session*/ null, Collections.singletonMap( "name", converterBeanName ) );
          if ( converter == null ) {
            log.error( "Could not find converter class \"" + converterBeanName + "\" for mimeType \""
                + mimeTypeDef.getMimeType() + "\" in import handler " + handlerClass );
          }
          mimeType.setConverter( converter );
          mimeTypeList.add( mimeType );
        }
      }
    }
    return mimeTypeList;
  }
}
