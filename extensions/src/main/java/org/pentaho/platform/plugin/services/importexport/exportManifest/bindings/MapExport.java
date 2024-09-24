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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> Java class for anonymous complex type.</p>
 * <p>This class, to be used in the "Export" classes, supports the Map object from the original class.<br/>
 * This exact code was being used in several places; to avoid duplication, it was extracted so that it can be
 * reused.</p>
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = { "entry" } )
public class MapExport {

  protected List<MapExport.Entry> entry;

  /**
   * Gets the value of the entry property.
   * <p/>
   * <p/>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the entry property.
   * <p/>
   * <p/>
   * For example, to add a new item, do as follows:
   * <p/>
   * <pre>
   * getEntry().add( newItem );
   * </pre>
   * <p/>
   * <p/>
   * <p/>
   * Objects of the following type(s) are allowed in the list {@link MapExport.Entry }
   */
  public List<MapExport.Entry> getEntry() {
    if ( entry == null ) {
      entry = new ArrayList<>();
    }
    return this.entry;
  }

  /**
   * <p/>
   * Java class for anonymous complex type.
   * <p/>
   * <p/>
   * The following schema fragment specifies the expected content contained within this class.
   * <p/>
   * <pre>
   * &lt;complexType>
   *   &lt;complexContent>
   *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
   *       &lt;sequence>
   *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
   *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
   *       &lt;/sequence>
   *     &lt;/restriction>
   *   &lt;/complexContent>
   * &lt;/complexType>
   * </pre>
   */
  @XmlAccessorType( XmlAccessType.FIELD )
  @XmlType( name = "", propOrder = { "key", "value" } )
  public static class Entry {

    protected String key;
    protected String value;

    /**
     * Gets the value of the key property.
     *
     * @return possible object is {@link String }
     */
    public String getKey() {
      return key;
    }

    /**
     * Sets the value of the key property.
     *
     * @param value allowed object is {@link String }
     */
    public void setKey( String value ) {
      this.key = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
      return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue( String value ) {
      this.value = value;
    }
  }
}
