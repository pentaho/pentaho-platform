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


//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.31 at 02:27:39 PM EDT 
//

package org.pentaho.platform.plugin.services.importer.mimeType.bindings;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ImportHandlerMimeTypeDefinitionsDto complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImportHandlerMimeTypeDefinitionsDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ImportHandler" type="{http://www.pentaho.com/schema/}ImportHandlerDto" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ImportHandlerMimeTypeDefinitionsDto", propOrder = { "importHandler" } )
public class ImportHandlerMimeTypeDefinitionsDto {

  @XmlElement( name = "ImportHandler", required = true )
  protected List<ImportHandlerDto> importHandler;

  /**
   * Gets the value of the importHandler property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the importHandler property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getImportHandler().add( newItem );
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link ImportHandlerDto }
   * 
   * 
   */
  public List<ImportHandlerDto> getImportHandler() {
    if ( importHandler == null ) {
      importHandler = new ArrayList<ImportHandlerDto>();
    }
    return this.importHandler;
  }

}
