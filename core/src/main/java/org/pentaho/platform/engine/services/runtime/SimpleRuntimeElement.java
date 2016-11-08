/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.xml.XmlHelper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleRuntimeElement extends PentahoBase implements IRuntimeElement {
  private static final long serialVersionUID = 5024690844237335928L;

  private static final Log logger = LogFactory.getLog( SimpleRuntimeElement.class );

  private String instanceId;

  private String parentId;

  private String solutionId;

  private String parentType;

  private int revision;

  private Map typesMap = new HashMap(); // The total list of properties and

  // their types

  private Map paramMapSS = new HashMap(); // ShortString Map ( VARCHAR(254) )

  private Map paramMapLS = new HashMap(); // LongString Map ( CLOB )

  private Map paramMapBD = new HashMap(); // BigDecimal Map

  private Map paramMapDT = new HashMap(); // Date Map

  private Map paramMapLong = new HashMap(); // Long Map

  private Map paramMapCPLX = new HashMap(); // Complex Map (Serialized as a

  // Blob)

  private static final int MAXSSLENGH = 254;

  private static final ThreadLocal allowableReadAttributeNames = new ThreadLocal();

  private boolean loaded;

  private boolean readOnly;

  // TODO: Implement check on every set and get to make sure that the
  // attribute is allowed to be read/written

  /**
   * Constructor for Hibernate
   */
  protected SimpleRuntimeElement() {

  }

  /**
   * Constructor
   * 
   * @param instId
   *          The Instance Id
   */
  public SimpleRuntimeElement( final String instId ) {
    instanceId = instId;
  }

  /**
   * Constructor
   * 
   * @param instId
   *          The Instance Id
   * @param parId
   *          The Parent Id
   * @param parType
   *          The Parent Type
   */
  public SimpleRuntimeElement( final String instId, final String parId, final String parType ) {
    instanceId = instId;
    parentId = parId;
    parentType = parType;
  }

  /**
   * Constructor
   * 
   * @param instId
   *          The Instance Id
   * @param parId
   *          The Parent Id
   * @param parType
   *          The Parent Type
   * @param solnId
   *          The Solution Id
   */
  public SimpleRuntimeElement( final String instId, final String parId, final String parType, final String solnId ) {
    instanceId = instId;
    parentId = parId;
    parentType = parType;
    solutionId = solnId;
  }

  public List getMessages() {
    return null;
  }

  protected void setPentahoSession( final IPentahoSession sess ) {
    genLogIdFromSession( sess );
  }

  /**
   * @return Returns the parentId.
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @param parentId
   *          The parentId to set.
   */
  public void setParentId( final String parentId ) {
    this.updateOk();
    this.parentId = parentId;
  }

  /**
   * @return Returns the parentType.
   */
  public String getParentType() {
    return parentType;
  }

  /**
   * @param parentType
   *          The parentType to set.
   */
  public void setParentType( final String parentType ) {
    this.updateOk();
    this.parentType = parentType;
  }

  /**
   * @return Returns the instanceId.
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instId
   *          The instanceId to set.
   */
  public void setInstanceId( final String instId ) {
    this.updateOk();
    this.instanceId = instId;
  }

  /**
   * @return Returns the solutionId.
   */
  public String getSolutionId() {
    return solutionId;
  }

  /**
   * @param solutionId
   *          The solutionId to set.
   */
  public void setSolutionId( final String solutionId ) {
    this.updateOk();
    this.solutionId = solutionId;
  }

  /**
   * Auto-handled revision mechanism.
   * 
   * @return The current revision
   */
  public int getRevision() {
    return revision;
  }

  /**
   * Sets the revision of the class
   * 
   * @param rev
   *          New revision to set.
   */
  protected void setRevision( final int rev ) {
    revision = rev;
  }

  /**
   * Uses the instanceId to distinguish equality. The instanceId will never be null, won't change, and is the
   * primary key. Therefore, it's the perfect candidate for equals() and hashcode.
   */
  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( !( other instanceof IRuntimeElement ) ) {
      return false;
    }
    final IRuntimeElement otherRE = (IRuntimeElement) other;
    return this.getInstanceId().equals( otherRE.getInstanceId() );
  }

  @Override
  public int hashCode() {
    return this.getInstanceId().hashCode();
  }

  protected Map getParamMapSS() {
    return paramMapSS;
  }

  protected Map getParamMapLS() {
    return paramMapLS;
  }

  protected Map getParamMapDT() {
    return paramMapDT;
  }

  protected Map getParamMapBD() {
    return paramMapBD;
  }

  protected Map getParamMapLong() {
    return paramMapLong;
  }

  protected Map getParamMapCPLX() {
    return paramMapCPLX;
  }

  protected void setParamMapSS( final Map ss ) {
    paramMapSS = ss;
  }

  protected void setParamMapLS( final Map ls ) {
    paramMapLS = ls;
  }

  protected void setParamMapDT( final Map dt ) {
    paramMapDT = dt;
  }

  protected void setParamMapBD( final Map bd ) {
    paramMapBD = bd;
  }

  protected void setParamMapLong( final Map lng ) {
    paramMapLong = lng;
  }

  protected void setParamMapCPLX( final Map cplx ) {
    paramMapCPLX = cplx;
  }

  /**
   * Gets a property from the paramMap as a string with no default value.
   * 
   * @param key
   *          The key into the map.
   * @return The property.
   */
  public String getStringProperty( final String key ) {
    return getStringProperty( key, null );
  }

  /**
   * Gets a property from the paramMap as a string, using a default value if it doesn't exist in the map.
   * 
   * @param key
   *          The key into the map.
   * @param defaultValue
   *          Default value returned if the key isn't already in the map.
   * @return The property.
   */
  public String getStringProperty( final String key, final String defaultValue ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getString", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapSS().get( key );
    if ( prop == null ) {
      prop = getParamMapLS().get( key );
    }
    return ( prop != null ) ? prop.toString() : defaultValue;
  }

  protected void checkType( final String key, final String type, final boolean setIt ) {
    Map localTypesMap = getTypesMap();
    String curType = (String) localTypesMap.get( key );
    if ( curType != null ) {
      if ( !curType.equals( type ) ) {
        throw new RepositoryException( Messages.getInstance().getErrorString(
            "RTREPO.ERROR_0001_INVALIDTYPE", curType, type ) ); //$NON-NLS-1$
      }
    }
    if ( setIt ) {
      localTypesMap.put( key, type );
    }
  }

  /**
   * Sets a property into the paramMap. Special implementation note - Null values aren't supported in the Map. So,
   * if a null value is passed in, this implementation will remove the entry from the map.
   * 
   * @param key
   *          The key into the map.
   * @param value
   *          The value to set.
   */
  public void setStringProperty( final String key, final String value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setString", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMapSS = getParamMapSS();
    Map theMapLS = getParamMapLS();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      if ( value.length() > SimpleRuntimeElement.MAXSSLENGH ) {
        theMapSS.remove( key ); // Make sure it's not in the short map
        // first.
        theMapLS.put( key, new StringBuffer( value ) );
      } else {
        theMapLS.remove( key );
        theMapSS.put( key, value );
      }
    } else {
      theMapSS.remove( key );
      theMapLS.remove( key );
    }
  }

  /**
   * Gets a BigDecimal property from the paramMap.
   * 
   * @param key
   *          Key in the paramMap.
   * @return BigDecimal property
   */
  public BigDecimal getBigDecimalProperty( final String key ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getBigDecimal", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return getBigDecimalProperty( key, null );
  }

  /**
   * Gets a property from the paramMap as a BigDecimal, using a default value if it doesn't exist in the map.
   * 
   * @param key
   *          Key in the paramMap.
   * @param defaultValue
   *          Detault value if the property doesn't exist in the paramMap.
   * @return Returns the property from the paramMap.
   */
  public BigDecimal getBigDecimalProperty( final String key, final BigDecimal defaultValue ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getBigDecimal", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapBD().get( key );
    return ( prop != null ) ? new BigDecimal( (String) prop ) : defaultValue;
  }

  /**
   * Sets the BigDecimal property in the paramMap. Special implementation note - Null values aren't supported in
   * the Map. So, if a null value is passed in, this implementation will remove the entry from the map.
   * 
   * @param key
   *          Key in the paramMap.
   * @param value
   *          The property value to set.
   */
  public void setBigDecimalProperty( final String key, final BigDecimal value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setBigDecimal", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMap = getParamMapBD();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      theMap.put( key, value.toString() );
    } else {
      theMap.remove( key );
    }
  }

  /**
   * Gets a property from the paramMap as a Date, with no default value.
   * 
   * @param key
   *          Key in the paramMap
   * @return The property in the map.
   */
  public Date getDateProperty( final String key ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getDate", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return getDateProperty( key, null );
  }

  /**
   * Gets a property from the paramMap as a Date using a default value if it doesn't exist in the map
   * 
   * @param key
   *          Key in the paramMap
   * @param defaultValue
   *          The default value if the property doesn't exist in the paramMap.
   * @return The property in the map.
   */
  public Date getDateProperty( final String key, final Date defaultValue ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getDate", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapDT().get( key );
    return ( prop != null ) ? (Date) prop : defaultValue;
  }

  /**
   * Sets a date property in the paramMap. If null comes in, it removes the value from the map. Special
   * implementation note - Null values aren't supported in the Map. So, if a null value is passed in, this
   * implementation will remove the entry from the map.
   * 
   * @param key
   *          Key in the paramMap
   * @param value
   *          The property value to set.
   */
  public void setDateProperty( final String key, final Date value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setDate", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMap = getParamMapDT();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      theMap.put( key, value );
    } else {
      theMap.remove( key );
    }
  }

  /**
   * Gets a property from the paramMap as a Long using a default value if it doesn't exist in the map
   * 
   * @param key
   *          Key in the paramMap
   * @param defaultValue
   *          The default value if the property doesn't exist in the paramMap.
   * @return The property in the map.
   */
  public Long getLongProperty( final String key, final Long defaultValue ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getLong", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapLong().get( key );
    return ( prop != null ) ? (Long) prop : defaultValue;
  }

  /**
   * Gets a property from the paramMap as a long using a default value if it doesn't exist in the map
   * 
   * @param key
   *          Key in the paramMap
   * @param defaultValue
   *          The default value if the property doesn't exist in the paramMap.
   * @return The property in the map.
   */
  public long getLongProperty( final String key, final long defaultValue ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getLong", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapLong().get( key );
    return ( prop != null ) ? ( (Long) prop ).longValue() : defaultValue;
  }

  /**
   * Sets a long property in the paramMap. If null comes in, it removes the value from the map. Special
   * implementation note - Null values aren't supported in the Map. So, if a null value is passed in, this
   * implementation will remove the entry from the map.
   * 
   * @param key
   *          Key in the paramMap
   * @param value
   *          The property value to set.
   */
  public void setLongProperty( final String key, final Long value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setLong", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMap = getParamMapLong();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      theMap.put( key, value );
    } else {
      theMap.remove( key );
    }
  }

  /**
   * Sets a long property in the paramMap.
   * 
   * @param key
   *          Key in the paramMap
   * @param value
   *          The property value to set.
   */
  public void setLongProperty( final String key, final long value ) {
    this.updateOk();
    setLongProperty( key, new Long( value ) );
  }

  /**
   * Gets a list property from the paramMap.
   * 
   * @param key
   *          Key in the map
   * @return The list property in the paramMap.
   */
  public List getListProperty( final String key ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getList", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapCPLX().get( key );
    return (List) prop;
  }

  /**
   * Gets a map property from the paramMap.
   * 
   * @param key
   *          The key in the map
   * @return The map value in the paramMap.
   */
  public Map getMapProperty( final String key ) {
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "getMap", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Object prop = getParamMapCPLX().get( key );
    return (Map) prop;
  }

  /**
   * Sets a list property in the paramMap. Special implementation note - Null values aren't supported in the Map.
   * So, if a null value is passed in, this implementation will remove the entry from the map.
   * 
   * @param key
   *          The key in the map.
   * @param value
   *          The list property to set.
   */
  public void setListProperty( final String key, final List value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setList", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMap = getParamMapCPLX();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      theMap.put( key, value );
    } else {
      theMap.remove( key );
    }
  }

  /**
   * Sets a map property in the paramMap. Special implementation note - Null values aren't supported in the Map.
   * So, if a null value is passed in, this implementation will remove the entry from the map.
   * 
   * @param key
   *          The key in the map.
   * @param value
   *          The map property to set.
   */
  public void setMapProperty( final String key, final Map value ) {
    this.updateOk();
    trace( Messages.getInstance().getString( "RTREPO.DEBUG_PROPERTY_GETSET", "setMap", key ) ); //$NON-NLS-1$ //$NON-NLS-2$
    Map theMap = getParamMapCPLX();
    if ( value != null ) {
      checkType( key, value.getClass().getName(), true );
      theMap.put( key, value );
    } else {
      theMap.remove( key );
    }
  }

  /**
   * Returns an XML representation of the RuntimeElement. Mainly for Debug/Test Cases to make sure that what goes
   * in is what comes out during tests.
   * 
   * @return Returns an XML representation of the RuntimeElement
   */
  public String toXML() {
    StringBuffer rtn = new StringBuffer();
    rtn.append( "<runtime-element>\r" ); //$NON-NLS-1$
    rtn.append( getXMLString( getInstanceId(), "instance-id", "  " ) ); //$NON-NLS-1$ //$NON-NLS-2$
    rtn.append( getXMLString( Integer.toString( getRevision() ), "revision", "  " ) ); //$NON-NLS-1$ //$NON-NLS-2$
    rtn.append( getXMLString( getParentId(), "parent-id", "  " ) ); //$NON-NLS-1$ //$NON-NLS-2$
    rtn.append( getXMLString( getParentType(), "parent-type", "  " ) ); //$NON-NLS-1$ //$NON-NLS-2$
    rtn.append( getXMLString( getSolutionId(), "solution-id", "  " ) ); //$NON-NLS-1$ //$NON-NLS-2$
    rtn.append( getMapXML( this.getParamMapSS(), "small-string-map" ) ); //$NON-NLS-1$
    rtn.append( getMapXML( this.getParamMapLS(), "large-string-map" ) ); //$NON-NLS-1$
    rtn.append( getMapXML( this.getParamMapDT(), "date-map" ) ); //$NON-NLS-1$
    rtn.append( getMapXML( this.getParamMapBD(), "big-decimal-map" ) ); //$NON-NLS-1$
    rtn.append( getMapXML( this.getParamMapLong(), "long-map" ) ); //$NON-NLS-1$
    rtn.append( getMapXML( this.getParamMapCPLX(), "complex-map" ) ); //$NON-NLS-1$
    rtn.append( "</runtime-element>\r" ); //$NON-NLS-1$
    return rtn.toString();
  }

  private String getXMLString( final String str, final String tag, final String indent ) {
    return indent + "<" + tag + "><![CDATA[" + str + "]]></" + tag + ">\r"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  private String getMapXML( final Map theMap, final String tag ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "  <" ).append( tag ).append( ">\r" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( XmlHelper.mapToXML( theMap, "      " ) ); //$NON-NLS-1$
    sb.append( "  </" ).append( tag ).append( ">\r" ); //$NON-NLS-1$ //$NON-NLS-2$
    return sb.toString();
  }

  /* ILogger Needs */
  @Override
  public Log getLogger() {
    return SimpleRuntimeElement.logger;
  }

  public void setAllowableAttributeNames( final Collection allowedReadNames ) {
    SimpleRuntimeElement.allowableReadAttributeNames.set( allowedReadNames );
  }

  /**
   * @return Returns the typesMap.
   */
  protected Map getTypesMap() {
    return typesMap;
  }

  /**
   * @param typesMap
   *          The typesMap to set.
   */
  protected void setTypesMap( final Map typesMap ) {
    this.typesMap = typesMap;
  }

  public Set getParameterNames() {
    return getTypesMap().keySet();
  }

  public String getParameterType( final String parameterName ) {
    return (String) getTypesMap().get( parameterName );
  }

  public void setLoaded( final boolean value ) {
    this.loaded = value;
  }

  public boolean getLoaded() {
    return this.loaded;
  }

  private void updateOk() {
    if ( !loaded ) {
      return;
    }
    if ( readOnly ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString( "RTELEMENT.ERROR_0001_INVALIDUPDATE" ) ); //$NON-NLS-1$
    }
  }

  public boolean getReadOnly() {
    return readOnly;
  }

  public void setReadOnly( final boolean value ) {
    this.readOnly = value;
  }

  public void forceSave() {
  }
}
