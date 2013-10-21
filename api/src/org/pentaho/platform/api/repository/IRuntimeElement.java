/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRuntimeElement {
  /**
   * @return Returns the parent ID
   */
  public String getParentId();

  /**
   * @param parentId
   *          The parentId to set.
   */
  public void setParentId( String parentId );

  /**
   * @return Returns the parent Type
   */
  public String getParentType();

  /**
   * @param parentType
   *          The parent type to set
   */
  public void setParentType( String parentType );

  /**
   * @return Returns the instance Id
   */
  public String getInstanceId();

  /**
   * @param instanceId
   *          The instance Id to set
   */
  public void setInstanceId( String instanceId );

  /**
   * @return Returns the solution Id.
   */
  public String getSolutionId();

  /**
   * @param solutionId
   *          The solution Id to set
   */
  public void setSolutionId( String solutionId );

  /**
   * @return Returns the revision (updated by Hibernate)
   */
  public int getRevision();

  /**
   * Gets a string property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @return The value of the property, or NULL if the property doesn't exist.
   */
  public String getStringProperty( String key );

  /**
   * Gets a string property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @param defaultValue
   *          The value to return if the property doesn't exist
   * @return The value of the property.
   */
  public String getStringProperty( String key, String defaultValue );

  /**
   * Sets a string property in the Runtime Element. Special implementation note - Null values aren't supported in
   * the Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The value to associate with the key
   */
  public void setStringProperty( String key, String value );

  /**
   * Gets a BigDecimal property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @return The value of the property, or null if the property doesn't exist.
   */
  public BigDecimal getBigDecimalProperty( String key );

  /**
   * Gets a BigDecimal property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @param defaultValue
   *          The value to return if the property doesn't exist
   * @return The value of the property, or defaultValue if the property doesn't exist.
   */
  public BigDecimal getBigDecimalProperty( String key, BigDecimal defaultValue );

  /**
   * Sets a BigDecimal property in the Runtime Element. Special implementation note - Null values aren't supported
   * in the Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime
   * Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The value to associate with the key.
   */
  public void setBigDecimalProperty( String key, BigDecimal value );

  /**
   * Gets a Date property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @return The value of the property, or NULL if the property doesn't exist.
   */
  public Date getDateProperty( String key );

  /**
   * Gets a Date property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @param defaultValue
   *          The value to return if the property doesn't exist
   * @return The value of the property, or defaultValue if the property doesn't exist.
   */
  public Date getDateProperty( String key, Date defaultValue );

  /**
   * Sets a Date property in the Runtime Element. Special implementation note - Null values aren't supported in the
   * Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The value to associate with the key.
   */
  public void setDateProperty( String key, Date value );

  /**
   * Gets a Long property from the Runtime Element.
   * 
   * @param key
   *          The key of the property
   * @param defaultValue
   *          The value to return if the property doesn't exist
   * @return The value of the property, or defaultValue if the property doesn't exist.
   */
  public Long getLongProperty( String key, Long defaultValue );

  /**
   * Gets a property from the paramMap as a long using a default value if it doesn't exist in the map
   * 
   * @param key
   *          Key in the paramMap
   * @param defaultValue
   *          The default value if the property doesn't exist in the paramMap.
   * @return The property in the map.
   */
  public long getLongProperty( String key, long defaultValue );

  /**
   * Sets a Long property in the Runtime Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The value to associate with the key.
   */
  public void setLongProperty( String key, Long value );

  /**
   * Sets a Long property in the Runtime Element. Special implementation note - Null values aren't supported in the
   * Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The value to associate with the key. Note - A new Long object is constructed and stored.
   */
  public void setLongProperty( String key, long value );

  /**
   * Gets a List property from the Runtime Element
   * 
   * @param key
   *          The key of the property to get
   * @return The list associated with the key, or NULL if it doesn't exist.
   */
  @SuppressWarnings( "rawtypes" )
  public List getListProperty( String key );

  /**
   * Gets a Map property from the Runtime Element Special implementation note - Null values aren't supported in the
   * Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime Element.
   * 
   * @param key
   *          The key of the property to get
   * @return The Map associated with the key, or NULL if it doesn't exist.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getMapProperty( String key );

  /**
   * Sets a List Property in the Runtime Element
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The List value to associate with the key.
   */
  @SuppressWarnings( "rawtypes" )
  public void setListProperty( String key, List value );

  /**
   * Sets a Map Property in the Runtime Element Special implementation note - Null values aren't supported in the
   * Map. So, if a null value is passed in, this implementation will remove the entry from the Runtime Element.
   * 
   * @param key
   *          The key of the property to set
   * @param value
   *          The Map value to associate with the key.
   */
  @SuppressWarnings( "rawtypes" )
  public void setMapProperty( String key, Map value );

  /**
   * @return A string containing the XML representation of the Runtime Element
   */
  public String toXML();

  /**
   * @return Returns the loggingLevel.
   */
  public int getLoggingLevel();

  /**
   * @param allowableReadAttributeNames
   *          The names of the attributes that this process is allowed to read.
   */
  @SuppressWarnings( "rawtypes" )
  public void setAllowableAttributeNames( Collection allowableReadAttributeNames );

  /**
   * @return The set of currently defined parameter names
   */
  @SuppressWarnings( "rawtypes" )
  public Set getParameterNames();

  /**
   * Gets the type of the parameter
   * 
   * @param name
   *          Parameter name
   * @return String type of the parameter
   */
  public String getParameterType( String name );

  public void setLoaded( boolean value );

  public boolean getLoaded();

  /**
   * Causes an immediate call to the underlying persistence mechanism's write-now method. For Hibernate, this
   * results in a call to HibernateUtil.flush().
   */
  public void forceSave();

}
