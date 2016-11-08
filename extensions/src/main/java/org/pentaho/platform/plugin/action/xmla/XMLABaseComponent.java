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

package org.pentaho.platform.plugin.action.xmla;

import org.apache.commons.logging.Log;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.data.IDataComponent;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Locale;

public abstract class XMLABaseComponent extends ComponentBase implements IDataComponent {

  private static final long serialVersionUID = 8405489984774339891L;

  private static final String MDD_URI = "urn:schemas-microsoft-com:xml-analysis:mddataset"; //$NON-NLS-1$

  private static final String ROWS_URI = "urn:schemas-microsoft-com:xml-analysis:rowset"; //$NON-NLS-1$

  private static final String XMLA_URI = "urn:schemas-microsoft-com:xml-analysis"; //$NON-NLS-1$

  private static final String EXECUTE_ACTION = "\"urn:schemas-microsoft-com:xml-analysis:Execute\""; //$NON-NLS-1$

  private static final String ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/"; //$NON-NLS-1$

  private static final String URI = "uri"; //$NON-NLS-1$

  private static final String USER = "user-id"; //$NON-NLS-1$

  private static final String PASSWORD = "password"; //$NON-NLS-1$

  private static final String CATALOG = "catalog"; //$NON-NLS-1$

  private static final String QUERY = "query"; //$NON-NLS-1$

  private static final int AXIS_COLUMNS = 0;

  private static final int AXIS_ROWS = 1;

  private IPentahoResultSet rSet;

  private SOAPConnectionFactory scf = null;

  private MessageFactory mf = null;

  private URL url = null;

  private int provider = 0;

  private String dataSource = null;

  public static final int PROVIDER_MICROSOFT = 1;

  public static final int PROVIDER_SAP = 2;

  public static final int PROVIDER_MONDRIAN = 3;

  public static final int PROVIDER_ESSBASE = 4;

  @Override
  public abstract boolean validateSystemSettings();

  public abstract String getResultOutputName();

  @Override
  public abstract Log getLogger();

  interface Rowhandler {
    void handleRow( SOAPElement eRow, SOAPEnvelope envelope );
  }

  public IPentahoResultSet getResultSet() {
    return rSet;
  }

  @Override
  protected boolean validateAction() {

    try {
      if ( !isDefinedInput( XMLABaseComponent.URI ) ) {
        error( Messages.getInstance().getErrorString(
          "XMLABaseComponent.ERROR_0001_CONNECTION_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      if ( !isDefinedInput( XMLABaseComponent.USER ) ) {
        error( Messages.getInstance().getErrorString(
          "XMLABaseComponent.ERROR_0002_USER_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      if ( !isDefinedInput( XMLABaseComponent.PASSWORD ) ) {
        error( Messages.getInstance().getErrorString(
          "XMLABaseComponent.ERROR_0003_PASSWORD_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      if ( !isDefinedInput( XMLABaseComponent.CATALOG ) ) {
        error( Messages.getInstance().getErrorString(
          "XMLABaseComponent.ERROR_0004_CATALOG_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      if ( !isDefinedInput( XMLABaseComponent.QUERY ) ) {
        error( Messages.getInstance().getErrorString(
          "XMLABaseComponent.ERROR_0005_QUERY_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }

      String outputName = getResultOutputName();
      if ( outputName != null ) {
        if ( !getOutputNames().contains( outputName ) ) {
          error( Messages.getInstance().getErrorString(
            "XMLABaseComponent.ERROR_0006_OUTPUT_NOT_SPECIFIED", getActionName() ) ); //$NON-NLS-1$
          return false;
        }
      }
      return true;
    } catch ( Exception e ) {
      error(
        Messages.getInstance().getErrorString( "XMLABaseComponent.ERROR_0007_VALIDATION_FAILED", getActionName() ),
        e ); //$NON-NLS-1$
    }

    return false;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() {
    try {
      scf = SOAPConnectionFactory.newInstance();
      mf = MessageFactory.newInstance();
    } catch ( UnsupportedOperationException e ) {
      e.printStackTrace();
    } catch ( SOAPException e ) {
      e.printStackTrace();
    }

    String uri = this.getInputStringValue( XMLABaseComponent.URI );
    String user = this.getInputStringValue( XMLABaseComponent.USER );
    String password = this.getInputStringValue( XMLABaseComponent.PASSWORD );
    String catalog = this.getInputStringValue( XMLABaseComponent.CATALOG );
    String query = this.getInputStringValue( XMLABaseComponent.QUERY );

    buildURl( uri, user, password );
    try {
      setProviderAndDataSource( discoverDS() );
      return executeQuery( query, catalog );
    } catch ( XMLAException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }

  private void buildURl( final String uri, final String user, final String password ) {
    try {
      this.url = new URL( uri );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }

    if ( ( user != null ) && ( user.length() > 0 ) ) {
      String newUri = this.url.getProtocol() + "://" + user; //$NON-NLS-1$
      if ( ( password != null ) && ( password.length() > 0 ) ) {
        newUri += ":" + password; //$NON-NLS-1$
      }
      newUri += "@" + this.url.getHost() + ":" + this.url.getPort() //$NON-NLS-1$ //$NON-NLS-2$
        + this.url.getPath();

      try {
        this.url = new URL( newUri );
      } catch ( MalformedURLException e ) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Execute query
   *
   * @param query   - MDX to be executed
   * @param catalog
   * @param handler Callback handler
   * @throws XMLAException
   */
  public boolean executeQuery( final String query, final String catalog ) throws XMLAException {

    Object[][] columnHeaders = null;
    Object[][] rowHeaders = null;
    Object[][] data = null;

    int columnCount = 0;
    int rowCount = 0;

    SOAPConnection connection = null;
    SOAPMessage reply = null;

    try {
      connection = scf.createConnection();
      SOAPMessage msg = mf.createMessage();

      MimeHeaders mh = msg.getMimeHeaders();
      mh.setHeader( "SOAPAction", XMLABaseComponent.EXECUTE_ACTION ); //$NON-NLS-1$

      SOAPPart soapPart = msg.getSOAPPart();
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.setEncodingStyle( XMLABaseComponent.ENCODING_STYLE );

      SOAPBody body = envelope.getBody();
      Name nEx = envelope.createName( "Execute", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$//$NON-NLS-2$

      SOAPElement eEx = body.addChildElement( nEx );
      eEx.setEncodingStyle( XMLABaseComponent.ENCODING_STYLE );

      // add the parameters

      // COMMAND parameter
      // <Command>
      // <Statement>select [Measures].members on Columns from
      // Sales</Statement>
      // </Command>
      Name nCom = envelope.createName( "Command", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      SOAPElement eCommand = eEx.addChildElement( nCom );
      Name nSta = envelope.createName( "Statement", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      SOAPElement eStatement = eCommand.addChildElement( nSta );
      eStatement.addTextNode( query );

      // <Properties>
      // <PropertyList>
      // <DataSourceInfo>Provider=MSOLAP;Data
      // Source=local</DataSourceInfo>
      // <Catalog>Foodmart 2000</Catalog>
      // <Format>Multidimensional</Format>
      // <AxisFormat>TupleFormat</AxisFormat> oder "ClusterFormat"
      // </PropertyList>
      // </Properties>
      Map paraList = new HashMap();
      paraList.put( "DataSourceInfo", dataSource ); //$NON-NLS-1$
      paraList.put( "Catalog", catalog ); //$NON-NLS-1$
      paraList.put( "Format", "Multidimensional" ); //$NON-NLS-1$ //$NON-NLS-2$
      paraList.put( "AxisFormat", "TupleFormat" ); //$NON-NLS-1$ //$NON-NLS-2$
      addParameterList( envelope, eEx, "Properties", "PropertyList", paraList ); //$NON-NLS-1$ //$NON-NLS-2$

      msg.saveChanges();

      debug( "Request for Execute" ); //$NON-NLS-1$
      logSoapMsg( msg );

      // run the call
      reply = connection.call( msg, url );

      debug( "Reply from Execute" ); //$NON-NLS-1$
      logSoapMsg( reply );

      // error check
      errorCheck( reply );

      // process the reply
      SOAPElement eRoot = findExecRoot( reply );

      // for each axis, get the positions (tuples)
      Name name = envelope.createName( "Axes", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      SOAPElement eAxes = selectSingleNode( eRoot, name );
      if ( eAxes == null ) {
        throw new XMLAException( "Excecute result has no Axes element" ); //$NON-NLS-1$
      }

      name = envelope.createName( "Axis", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator itAxis = eAxes.getChildElements( name );

    AxisLoop:
      for ( int iOrdinal = 0; itAxis.hasNext(); ) {
        SOAPElement eAxis = (SOAPElement) itAxis.next();
        name = envelope.createName( "name" ); //$NON-NLS-1$
        String axisName = eAxis.getAttributeValue( name );
        int axisOrdinal;
        if ( axisName.equals( "SlicerAxis" ) ) { //$NON-NLS-1$
          continue;
        } else {
          axisOrdinal = iOrdinal++;
        }

        name = envelope.createName( "Tuples", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
        SOAPElement eTuples = selectSingleNode( eAxis, name );
        if ( eTuples == null ) {
          continue AxisLoop; // what else?
        }

        name = envelope.createName( "Tuple", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
        Iterator itTuple = eTuples.getChildElements( name );

        // loop over tuples
        int positionOrdinal = 0;
        while ( itTuple.hasNext() ) { // TupleLoop

          SOAPElement eTuple = (SOAPElement) itTuple.next();

          if ( ( axisOrdinal == XMLABaseComponent.AXIS_COLUMNS ) && ( columnHeaders == null ) ) {
            columnCount = getChildCount( envelope, eTuples, "Tuple" ); //$NON-NLS-1$
            columnHeaders = new Object[ getChildCount( envelope, eTuple, "Member" ) ][ columnCount ]; //$NON-NLS-1$
          } else if ( ( axisOrdinal == XMLABaseComponent.AXIS_ROWS ) && ( rowHeaders == null ) ) {
            rowCount = getChildCount( envelope, eTuples, "Tuple" ); //$NON-NLS-1$
            rowHeaders = new Object[ rowCount ][ getChildCount( envelope, eTuple, "Member" ) ]; //$NON-NLS-1$
          }

          int index = 0;
          name = envelope.createName( "Member", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
          Iterator itMember = eTuple.getChildElements( name );
          while ( itMember.hasNext() ) { // MemberLoop
            SOAPElement eMem = (SOAPElement) itMember.next();
            // loop over children nodes
            String caption = null;
            Iterator it = eMem.getChildElements();
          InnerLoop:
            while ( it.hasNext() ) {
              Node n = (Node) it.next();
              if ( !( n instanceof SOAPElement ) ) {
                continue InnerLoop;
              }
              SOAPElement el = (SOAPElement) n;
              String enam = el.getElementName().getLocalName();
              if ( enam.equals( "Caption" ) ) { //$NON-NLS-1$
                caption = el.getValue();
              }
            }
            if ( axisOrdinal == XMLABaseComponent.AXIS_COLUMNS ) {
              columnHeaders[ index ][ positionOrdinal ] = caption;
            } else if ( axisOrdinal == XMLABaseComponent.AXIS_ROWS ) {
              rowHeaders[ positionOrdinal ][ index ] = caption;
            }
            ++index;
          } // MemberLoop

          ++positionOrdinal;
        } // TupleLoop
      } // AxisLoop

      data = new Object[ rowCount ][ columnCount ];
      // loop over cells in result set
      name = envelope.createName( "CellData", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
      SOAPElement eCellData = selectSingleNode( eRoot, name );
      name = envelope.createName( "Cell", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
      Iterator itSoapCell = eCellData.getChildElements( name );
      while ( itSoapCell.hasNext() ) { // CellLoop
        SOAPElement eCell = (SOAPElement) itSoapCell.next();
        name = envelope.createName( "CellOrdinal", "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String cellOrdinal = eCell.getAttributeValue( name );
        int ordinal = Integer.parseInt( cellOrdinal );
        name = envelope.createName( "Value", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
        Object value = selectSingleNode( eCell, name ).getValue();

        int rowLoc = ordinal / columnCount;
        int columnLoc = ordinal % columnCount;

        data[ rowLoc ][ columnLoc ] = value;
      } // CellLoop

      MemoryResultSet resultSet = new MemoryResultSet();
      MemoryMetaData metaData = new MemoryMetaData( columnHeaders, rowHeaders );
      resultSet.setMetaData( metaData );
      for ( Object[] element : data ) {
        resultSet.addRow( element );
      }
      rSet = resultSet;
      if ( resultSet != null ) {
        if ( getResultOutputName() != null ) {
          setOutputValue( getResultOutputName(), resultSet );
        }
        return true;
      }
      return false;

    } catch ( SOAPException se ) {
      throw new XMLAException( se );
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( SOAPException e ) {
          // log and ignore
          error( "?", e ); //$NON-NLS-1$
        }
      }

    }

  }

  private int getChildCount( final SOAPEnvelope envelope, final SOAPElement element, final String childName )
    throws SOAPException {
    Name name = envelope.createName( childName, "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$
    Iterator iter = element.getChildElements( name );
    int value = 0;
    while ( iter.hasNext() ) {
      value++;
      iter.next();
    }
    return value;
  }

  private void setProviderAndDataSource( final Map resMap ) throws XMLAException {
    if ( ( resMap == null ) || ( resMap.size() == 0 ) ) {
      error( Messages.getInstance().getString( "XMLABaseComponent.ERROR_0008_NO_RESOURCE_MAP" ) ); //$NON-NLS-1$
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0008_NO_RESOURCE_MAP" ) ); //$NON-NLS-1$
    }

    String pstr = (String) resMap.get( "ProviderName" ); //$NON-NLS-1$

    if ( pstr == null ) {
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0009_NO_PROVIDER_NAME" ) ); //$NON-NLS-1$
    }

    provider = determineProvider( "Provider=" + pstr ); //$NON-NLS-1$

    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0001_PROVIDER_ID" ) + provider ); //$NON-NLS-1$

    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0002_DATASOURCE_NAME" ) + String.valueOf(
      resMap.get( "DataSourceName" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0003_DATASOURCE_INFO" ) + String.valueOf(
      resMap.get( "DataSourceInfo" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$

    dataSource = (String) resMap.get( "DataSourceInfo" ); //$NON-NLS-1$

    if ( ( dataSource == null ) || ( dataSource.length() < 1 ) ) {
      dataSource = (String) resMap.get( "DataSourceName" ); //$NON-NLS-1$
    }

    if ( dataSource == null ) {
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0010_NO_DATASOURCE_NAME" ) ); //$NON-NLS-1$
    }

    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0004_DISCOVER_DATASOURCE_SET" )
      + dataSource ); //$NON-NLS-1$

  }

  /**
   * retrieve data source properties
   *
   * @return Map of key/value strings
   * @see DataSourceBrowser
   */
  public Map discoverDS() throws XMLAException {
    // Microsoft wants restrictions
    HashMap rHash = new HashMap();

    HashMap pHash = new HashMap();
    pHash.put( "Content", "Data" ); //$NON-NLS-1$ //$NON-NLS-2$
    final Map resultMap = new HashMap();
    Rowhandler rh = new Rowhandler() {

      public void handleRow( SOAPElement eRow, SOAPEnvelope envelope ) {

        /*
         * <row><DataSourceName>SAP_BW</DataSourceName> <DataSourceDescription>SAP BW Release 3.0A XML f. Analysis
         * Service</DataSourceDescription> <URL>http://155.56.49.46:83/SAP/BW/XML/SOAP/XMLA</URL>
         * <DataSourceInfo>default</DataSourceInfo> <ProviderName>SAP BW</ProviderName> <ProviderType>MDP</ProviderType>
         * <AuthenticationMode>Integrated</AuthenticationMode></row>
         */
        Iterator it = eRow.getChildElements();
        while ( it.hasNext() ) {
          Object o = it.next();
          if ( !( o instanceof SOAPElement ) ) {
            continue; // bypass text nodes
          }
          SOAPElement e = (SOAPElement) o;
          String name = e.getElementName().getLocalName();
          String value = e.getValue();
          resultMap.put( name, value );
        }
      }
    };

    discover( "DISCOVER_DATASOURCES", url, rHash, pHash, rh ); //$NON-NLS-1$
    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0005_DISCOVER_DATASOURCE_FOUND" ) + resultMap
      .size() ); //$NON-NLS-1$
    return resultMap;

  }

  /**
   * discover
   *
   * @param request
   * @param discoverUrl
   * @param restrictions
   * @param properties
   * @param rh
   * @throws XMLAException
   */
  private void discover( final String request, final URL discoverUrl, final Map restrictions, final Map properties,
                         final Rowhandler rh ) throws XMLAException {

    try {
      SOAPConnection connection = scf.createConnection();

      SOAPMessage msg = mf.createMessage();

      MimeHeaders mh = msg.getMimeHeaders();
      mh.setHeader( "SOAPAction", "\"urn:schemas-microsoft-com:xml-analysis:Discover\"" ); //$NON-NLS-1$ //$NON-NLS-2$

      SOAPPart soapPart = msg.getSOAPPart();
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.addNamespaceDeclaration( "xsi", "http://www.w3.org/2001/XMLSchema-instance" ); //$NON-NLS-1$//$NON-NLS-2$
      envelope.addNamespaceDeclaration( "xsd", "http://www.w3.org/2001/XMLSchema" ); //$NON-NLS-1$ //$NON-NLS-2$
      SOAPBody body = envelope.getBody();
      Name nDiscover = envelope.createName( "Discover", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$//$NON-NLS-2$

      SOAPElement eDiscover = body.addChildElement( nDiscover );
      eDiscover.setEncodingStyle( XMLABaseComponent.ENCODING_STYLE );

      Name nPara = envelope.createName( "RequestType", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$//$NON-NLS-2$
      SOAPElement eRequestType = eDiscover.addChildElement( nPara );
      eRequestType.addTextNode( request );

      // add the parameters
      if ( restrictions != null ) {
        addParameterList( envelope, eDiscover, "Restrictions", "RestrictionList",
          restrictions ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      addParameterList( envelope, eDiscover, "Properties", "PropertyList", properties ); //$NON-NLS-1$//$NON-NLS-2$

      msg.saveChanges();

      debug(
        Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0006_DISCOVER_REQUEST" ) + request ); //$NON-NLS-1$
      logSoapMsg( msg );

      // run the call
      SOAPMessage reply = connection.call( msg, discoverUrl );

      debug(
        Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0007_DISCOVER_RESPONSE" ) + request ); //$NON-NLS-1$
      logSoapMsg( reply );

      errorCheck( reply );

      SOAPElement eRoot = findDiscoverRoot( reply );

      Name nRow = envelope.createName( "row", "", XMLABaseComponent.ROWS_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator itRow = eRoot.getChildElements( nRow );
      while ( itRow.hasNext() ) { // RowLoop

        SOAPElement eRow = (SOAPElement) itRow.next();
        rh.handleRow( eRow, envelope );

      } // RowLoop

      connection.close();
    } catch ( UnsupportedOperationException e ) {
      throw new XMLAException( e );
    } catch ( SOAPException e ) {
      throw new XMLAException( e );
    }

  }

  /**
   * add a list of Restrictions/Properties ...
   */
  private void addParameterList( final SOAPEnvelope envelope, final SOAPElement eParent, final String typeName,
                                 final String listName, final Map params ) throws SOAPException {
    Name nPara = envelope.createName( typeName, "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$
    SOAPElement eType = eParent.addChildElement( nPara );
    nPara = envelope.createName( listName, "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$
    SOAPElement eList = eType.addChildElement( nPara );
    if ( params == null ) {
      return;
    }
    Iterator it = params.keySet().iterator();
    while ( it.hasNext() ) {
      String tag = (String) it.next();
      String value = (String) params.get( tag );
      nPara = envelope.createName( tag, "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$
      SOAPElement eTag = eList.addChildElement( nPara );
      eTag.addTextNode( value );
    }
  }

  /**
   * locate "root" in ExecuteResponse
   */
  private SOAPElement findExecRoot( final SOAPMessage reply ) throws SOAPException, XMLAException {
    SOAPPart sp = reply.getSOAPPart();
    SOAPEnvelope envelope = sp.getEnvelope();
    SOAPBody body = envelope.getBody();

    Name name;
    name = envelope.createName( "ExecuteResponse", "m", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$//$NON-NLS-2$
    SOAPElement eResponse = selectSingleNode( body, name );
    if ( eResponse == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0011_NO_EXECUTE_RESPONSE_ELEMENT" ) ); //$NON-NLS-1$
    }

    name = envelope.createName( "return", "m", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$//$NON-NLS-2$
    SOAPElement eReturn = selectSingleNode( eResponse, name );

    name = envelope.createName( "root", "", XMLABaseComponent.MDD_URI ); //$NON-NLS-1$//$NON-NLS-2$
    SOAPElement eRoot = selectSingleNode( eReturn, name );
    if ( eRoot == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0012_NO_RESPONSE_ROOT_ELEMENT" ) );
    } //$NON-NLS-1$
    return eRoot;
  }

  /**
   * locate "root" in DisoverResponse
   */
  private SOAPElement findDiscoverRoot( final SOAPMessage reply ) throws SOAPException, XMLAException {

    SOAPPart sp = reply.getSOAPPart();
    SOAPEnvelope envelope = sp.getEnvelope();
    SOAPBody body = envelope.getBody();
    Name childName;
    SOAPElement eResponse = null;
    if ( provider == 0 ) {
      // unknown provider - recognize by prefix of DiscoverResponse
      Iterator itBody = body.getChildElements();
      while ( itBody.hasNext() ) {
        Node n = (Node) itBody.next();
        if ( !( n instanceof SOAPElement ) ) {
          continue;
        }
        Name name = ( (SOAPElement) n ).getElementName();
        if ( name.getLocalName().equals( "DiscoverResponse" ) ) { //$NON-NLS-1$
          eResponse = (SOAPElement) n;
          provider = getProviderFromDiscoverResponse( envelope, eResponse );
          break;
        }
      }
      if ( eResponse == null ) {
        throw new XMLAException(
          Messages.getInstance().getString( "XMLABaseComponent.ERROR_0013_NO_DISCOVER_RESPONSE" ) ); //$NON-NLS-1$
      }

    } else {
      if ( ( provider == XMLABaseComponent.PROVIDER_MICROSOFT ) || ( provider
        == XMLABaseComponent.PROVIDER_ESSBASE ) ) { // Microsoft
        // or
        // Essbase
        childName =
          envelope.createName( "DiscoverResponse", "m", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      } else if ( ( provider == XMLABaseComponent.PROVIDER_SAP ) || ( provider
        == XMLABaseComponent.PROVIDER_MONDRIAN ) ) { // SAP
        // or
        // Mondrian
        childName =
          envelope.createName( "DiscoverResponse", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        throw new IllegalArgumentException( Messages.getInstance().getString(
          "XMLABaseComponent.ERROR_0014_NO_PROVIDER_SPEC" ) ); //$NON-NLS-1$
      }
      eResponse = selectSingleNode( body, childName );
      if ( eResponse == null ) {
        throw new XMLAException( Messages.getInstance().getString(
          "XMLABaseComponent.ERROR_0015_NO_DISCOVER_RESPONSE_ELEMENT" ) ); //$NON-NLS-1$
      }
    }

    SOAPElement eReturn = getDiscoverReturn( envelope, eResponse );
    if ( eReturn == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0016_NO_RESULT_RETURN_ELEMENT" ) ); //$NON-NLS-1$
    }

    SOAPElement eRoot = getDiscoverRoot( envelope, eReturn );
    if ( eRoot == null ) {
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0017_NO_RESULT_ROOT_ELEMENT" ) ); //$NON-NLS-1$
    }
    return eRoot;
  }

  /**
   * Find the Provider type in the DiscoverResponse
   *
   * @param n
   * @return
   * @throws XMLAException
   * @throws SOAPException
   */
  private int getProviderFromDiscoverResponse( final SOAPEnvelope envelope, final SOAPElement e ) throws XMLAException,
    SOAPException {
    Name name = e.getElementName();
    if ( !name.getLocalName().equals( "DiscoverResponse" ) ) { //$NON-NLS-1$
      throw new XMLAException( Messages.getInstance()
        .getString( "XMLABaseComponent.ERROR_0018_NOT_A_DISCOVER_RESPONSE" ) + name.getLocalName() ); //$NON-NLS-1$
    }

    // Look for return/root/row/ProviderName

    SOAPElement walker = getDiscoverReturn( envelope, e );

    if ( walker == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0019_NO_RESULT_DISCOVER_RESPONSE" ) ); //$NON-NLS-1$
    }

    walker = getDiscoverRoot( envelope, walker );

    if ( walker == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0020_NO_RESULT_DISCOVER_RETURN_ROOT" ) ); //$NON-NLS-1$
    }

    walker = getDiscoverRow( envelope, walker );

    if ( walker == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0021_NO_DISCOVER_RESPONSE_ROW" ) ); //$NON-NLS-1$
    }

    /*
     * Name nProviderName = envelope.createName("ProviderName", "", ROWS_URI); SOAPElement eProviderName =
     * selectSingleNode(e, nProviderName);
     * 
     * if (eProviderName == null) { throw new OlapException("Discover result has no
     * DiscoverResponse/return/root/row/ProviderName element"); } value = eProviderName.getValue();
     */
    String value = null;
    Iterator it = walker.getChildElements();
    while ( it.hasNext() ) {
      Object o = it.next();
      if ( !( o instanceof SOAPElement ) ) {
        continue; // bypass text nodes
      }
      SOAPElement e2 = (SOAPElement) o;
      String nameString = e2.getElementName().getLocalName();
      if ( nameString.equals( "ProviderName" ) ) { //$NON-NLS-1$
        value = e2.getValue();
        debug(
          Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0008_FOUND_PROVIDER" ) + value ); //$NON-NLS-1$
        break;
      }
    }

    if ( ( value == null ) || ( value.trim().length() == 0 ) ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0022_NO_PROVIDER_NAME_ELEMENT" ) ); //$NON-NLS-1$
    }

    return determineProvider( "Provider=" + value ); //$NON-NLS-1$
  }

  /**
   * Get provider id from String
   *
   * @param dataSourceString
   * @return provider id from OlapDiscoverer
   * @throws XMLAException
   */
  private int determineProvider( final String dataSourceString ) throws XMLAException {
    debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0009_DETERMINE_PROVIDER" )
      + dataSourceString ); //$NON-NLS-1$
    if ( dataSourceString == null ) {
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0023_NO_DATASOURCE_GIVEN" ) ); //$NON-NLS-1$
    }

    String upperDSString = dataSourceString.toUpperCase( Locale.US );
    if ( !upperDSString.startsWith( "PROVIDER=" ) ) { //$NON-NLS-1$
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0024_MALFORMED_DATASOURCE" ) ); //$NON-NLS-1$
    }

    if ( upperDSString.startsWith( "PROVIDER=SAP" ) ) { //$NON-NLS-1$
      debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0009_SAP_PROVIDER" ) ); //$NON-NLS-1$
      return XMLABaseComponent.PROVIDER_SAP;
    } else if ( upperDSString.startsWith( "PROVIDER=MONDRIAN" ) ) { //$NON-NLS-1$
      debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0010_MONDRIAN_PROVIDER" ) ); //$NON-NLS-1$
      return XMLABaseComponent.PROVIDER_MONDRIAN;
    } else if ( upperDSString.startsWith( "PROVIDER=MS" ) ) { //$NON-NLS-1$ //not sure if this is needed?
      debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0011_MICROSOFT_PROVIDER" ) ); //$NON-NLS-1$
      return XMLABaseComponent.PROVIDER_MICROSOFT;
    } else if ( upperDSString
      .startsWith( "PROVIDER=MICROSOFT" ) ) { //$NON-NLS-1$ // return value from MSAS: "Microsoft XML for Analysis"
      debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0011_MICROSOFT_PROVIDER" ) ); //$NON-NLS-1$
      return XMLABaseComponent.PROVIDER_MICROSOFT; //
    } else if ( upperDSString
      .startsWith( "PROVIDER=ESSBASE" ) ) { //$NON-NLS-1$ // return value from MSAS: "Microsoft XML for Analysis"
      debug( Messages.getInstance().getString( "XMLABaseComponent.DEBUG_0012_ESSBASE_PROVIDER" ) ); //$NON-NLS-1$
      return XMLABaseComponent.PROVIDER_ESSBASE; //
    } else {
      error( Messages.getInstance().getString( "XMLABaseComponent.ERROR_0023_CANNOT_DETERMINE_PROVIDER" )
        + dataSourceString ); //$NON-NLS-1$
      throw new XMLAException(
        Messages.getInstance().getString( "XMLABaseComponent.ERROR_0024_UNSUPPORTED_PROVIDER" ) ); //$NON-NLS-1$
    }

  }

  private SOAPElement getDiscoverReturn( final SOAPEnvelope envelope, final SOAPElement e ) throws XMLAException,
    SOAPException {

    Name nReturn;
    if ( ( provider == XMLABaseComponent.PROVIDER_MICROSOFT ) || ( provider == XMLABaseComponent.PROVIDER_ESSBASE ) ) {
      nReturn = envelope.createName( "return", "m", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      nReturn = envelope.createName( "return", "", XMLABaseComponent.XMLA_URI ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    SOAPElement eReturn = selectSingleNode( e, nReturn );
    if ( eReturn == null ) {
      // old Microsoft XMLA SDK 1.0 does not have "m" prefix - try
      nReturn = envelope.createName( "return", "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      eReturn = selectSingleNode( e, nReturn );
      if ( eReturn == null ) {
        throw new XMLAException( Messages.getInstance().getString(
          "XMLABaseComponent.ERROR_0025_NO_RETURN_DISCOVER_ELEMENT" ) ); //$NON-NLS-1$
      }
    }
    return eReturn;
  }

  private SOAPElement getDiscoverRoot( final SOAPEnvelope envelope, final SOAPElement e ) throws XMLAException,
    SOAPException {

    Name nRoot = envelope.createName( "root", "", XMLABaseComponent.ROWS_URI ); //$NON-NLS-1$ //$NON-NLS-2$
    SOAPElement eRoot = selectSingleNode( e, nRoot );
    if ( eRoot == null ) {
      throw new XMLAException( Messages.getInstance().getString(
        "XMLABaseComponent.ERROR_0026_NO_ROOT_DISCOVER_ELEMENT" ) );
    } //$NON-NLS-1$
    return eRoot;

  }

  private SOAPElement getDiscoverRow( final SOAPEnvelope envelope, final SOAPElement e ) throws XMLAException,
    SOAPException {

    Name nRow = envelope.createName( "row", "", XMLABaseComponent.ROWS_URI ); //$NON-NLS-1$ //$NON-NLS-2$
    SOAPElement eRow = selectSingleNode( e, nRow );
    if ( eRow == null ) {
      throw new XMLAException( Messages.getInstance()
        .getString( "XMLABaseComponent.ERROR_0027_NO_DISCOVER_ROW_ELEMENT" ) );
    } //$NON-NLS-1$
    return eRow;

  }

  // error check
  private void errorCheck( final SOAPMessage reply ) throws SOAPException, XMLAException {
    String[] strings = new String[ 4 ];
    if ( soapFault( reply, strings ) ) {
      String faultString =
        "Soap Fault code=" + strings[ 0 ] + " fault string=" + strings[ 1 ] + " fault actor="
          + strings[ 2 ]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if ( strings[ 3 ] != null ) {
        faultString += "\ndetail:" + strings[ 3 ]; //$NON-NLS-1$
      }
      throw new XMLAException( faultString );
    }
  }

  /**
   * check SOAP reply for Error, return fault Code
   *
   * @param reply   the message to check
   * @param aReturn ArrayList containing faultcode,faultstring,faultactor
   */
  private boolean soapFault( final SOAPMessage reply, final String[] faults ) throws SOAPException {
    SOAPPart sp = reply.getSOAPPart();
    SOAPEnvelope envelope = sp.getEnvelope();
    SOAPBody body = envelope.getBody();
    if ( !body.hasFault() ) {
      return false;
    }
    SOAPFault fault = body.getFault();

    faults[ 0 ] = fault.getFaultCode();
    faults[ 1 ] = fault.getFaultString();
    faults[ 2 ] = fault.getFaultActor();

    // probably not neccessary with Microsoft;
    Detail detail = fault.getDetail();
    if ( detail == null ) {
      return true;
    }
    String detailMsg = ""; //$NON-NLS-1$
    Iterator it = detail.getDetailEntries();
    for ( ; it.hasNext(); ) {
      DetailEntry det = (DetailEntry) it.next();
      Iterator ita = det.getAllAttributes();
      for ( boolean cont = false; ita.hasNext(); cont = true ) {
        Name name = (Name) ita.next();
        if ( cont ) {
          detailMsg += "; "; //$NON-NLS-1$
        }
        detailMsg += name.getLocalName();
        detailMsg += " = "; //$NON-NLS-1$
        detailMsg += det.getAttributeValue( name );
      }
    }
    faults[ 3 ] = detailMsg;

    return true;
  }

  /**
   * @param contextNode
   * @param childPath
   * @return
   */
  private SOAPElement selectSingleNode( final SOAPElement contextNode, final Name childName ) {

    Iterator it = contextNode.getChildElements( childName );
    if ( it.hasNext() ) {
      return (SOAPElement) it.next();
    }
    return null;
  }

  /**
   * log the reply message
   */
  private void logSoapMsg( final SOAPMessage msg ) {
    try {
      Writer writer = new StringWriter();
      TransformerFactory tFact = TransformerFactory.newInstance();
      Transformer transformer = tFact.newTransformer();
      Source src = msg.getSOAPPart().getContent();
      StreamResult result = new StreamResult( writer );
      transformer.transform( src, result );
      debug( writer.toString() );
    } catch ( Exception e ) {
      // no big problen - just for debugging
      error( "?", e ); //$NON-NLS-1$
    }
  }

  public void dispose() {
  }

  @Override
  public boolean init() {
    return true;
  }
}
