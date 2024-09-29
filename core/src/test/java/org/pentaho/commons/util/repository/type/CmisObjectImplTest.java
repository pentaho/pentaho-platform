/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.commons.util.repository.type;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class CmisObjectImplTest {

  @Test
  public void testProperties() {
    CmisObjectImpl testObject = new CmisObjectImpl();
    CmisProperties propertiesBase = new CmisProperties();
    AllowableActions allowableActionsBase = new AllowableActions();
    List<CmisObject> relationshipBase = new ArrayList<>();
    List<CmisObject> childBase = new ArrayList<>();
    testObject.setProperties( propertiesBase );
    testObject.setAllowableActions( allowableActionsBase );
    testObject.setRelationship( relationshipBase );
    testObject.setChild( childBase );
    assertEquals( propertiesBase, testObject.getProperties() );
    assertEquals( allowableActionsBase, testObject.getAllowableActions() );
    assertEquals( relationshipBase, testObject.getRelationship() );
    assertEquals( childBase, testObject.getChild() );

    List<CmisProperty> propertyList = propertiesBase.getProperties();
    propertyList.add( new PropertyXml( "WibbleXml", "Xml Value" ) );
    propertyList.add( new PropertyUri( "WibbleUri", "Uri Value" ) );
    propertyList.add( new PropertyHtml( "WibbleHtml", "Html Value" ) );
    propertyList.add( new PropertyId( "WibbleId", "Id Value" ) );
    propertyList.add( new PropertyString( "WibblePlain", "Plain Value" ) );
    propertyList.add( new PropertyBoolean( "WibbleTrueBoolean", true ) );
    propertyList.add( new PropertyBoolean( "WibbleFalseBoolean", false ) );
    propertyList.add( new PropertyInteger( "WibbleInt", 17 ) );
    Calendar c = Calendar.getInstance();
    c.set( 2017, 1, 17, 12, 35, 20 );
    propertyList.add( new PropertyDateTime( "WibbleDateTime", c ) );
    propertyList.add(  new PropertyDecimal( "WibbleDecimal", new BigDecimal( "123.456" ) ) );

    assertEquals( "Xml Value", testObject.findXmlProperty( "WibbleXml", "Not There" ) );
    assertEquals( "Not There", testObject.findXmlProperty( "Bad Search", "Not There" ) );
    assertEquals( "Uri Value", testObject.findUriProperty( "WibbleUri", "Not There" ) );
    assertEquals( "Not There", testObject.findUriProperty( "Bad Search", "Not There" ) );
    assertEquals( "Html Value", testObject.findHtmlProperty( "WibbleHtml", "Not There" ) );
    assertEquals( "Not There", testObject.findHtmlProperty( "Bad Search", "Not There" ) );
    assertEquals( "Id Value", testObject.findIdProperty( "WibbleId", "Not There" ) );
    assertEquals( "Not There", testObject.findIdProperty( "Bad Search", "Not There" ) );
    assertEquals( "Plain Value", testObject.findStringProperty( "WibblePlain", "Not There" ) );
    assertEquals( "Not There", testObject.findStringProperty( "Bad Search", "Not There" ) );
    assertTrue( testObject.findBooleanProperty( "WibbleTrueBoolean", false ) );
    assertFalse( testObject.findBooleanProperty( "Not There", false ) );
    assertFalse( testObject.findBooleanProperty( "WibbleFalseBoolean", true ) );
    assertEquals( new Integer( 17 ), testObject.findIntegerProperty( "WibbleInt", 93 ) );
    assertEquals( new Integer( 93 ), testObject.findIntegerProperty( "Bad Search", 93 ) );

    assertEquals( new BigDecimal( "123.456" ), testObject.findDecimalProperty( "WibbleDecimal", new BigDecimal( "987.654" ) ) );
    assertEquals( new BigDecimal( "987.654" ), testObject.findDecimalProperty( "Bad Search", new BigDecimal( "987.654" ) ) );

    Calendar notFound = Calendar.getInstance();
    notFound.set( 2016, 5, 5, 5, 5, 5 );
    assertEquals( c, testObject.findDateTimeProperty( "WibbleDateTime", notFound ) );
    assertEquals( notFound, testObject.findDateTimeProperty( "Bad Search", notFound ) );
  }

}
