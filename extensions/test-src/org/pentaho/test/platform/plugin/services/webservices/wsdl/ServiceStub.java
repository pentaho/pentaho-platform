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

/**
 * StubServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

package org.pentaho.test.platform.plugin.services.webservices.wsdl;

/*
 *  StubServiceStub java implementation
 */

@SuppressWarnings ( { "all" } )
public class ServiceStub extends org.apache.axis2.client.Stub {
  protected org.apache.axis2.description.AxisOperation[] _operations;

  // hashmaps to keep the fault mapping
  private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
  private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
  private java.util.HashMap faultMessageMap = new java.util.HashMap();

  private static int counter = 0;

  private static synchronized String getUniqueSuffix() {
    // reset the counter if it is greater than 99999
    if ( counter > 99999 ) {
      counter = 0;
    }
    counter = counter + 1;
    return Long.toString( System.currentTimeMillis() ) + "_" + counter;
  }

  private void populateAxisService() throws org.apache.axis2.AxisFault {

    // creating the Service with a unique name
    _service = new org.apache.axis2.description.AxisService( "StubService" + getUniqueSuffix() );
    addAnonymousOperations();

    // creating the operations
    org.apache.axis2.description.AxisOperation __operation;

    _operations = new org.apache.axis2.description.AxisOperation[ 5 ];

    __operation = new org.apache.axis2.description.OutInAxisOperation();

    __operation.setName( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "throwsError2" ) );
    _service.addOperation( __operation );

    _operations[ 0 ] = __operation;

    __operation = new org.apache.axis2.description.OutInAxisOperation();

    __operation.setName( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "getString" ) );
    _service.addOperation( __operation );

    _operations[ 1 ] = __operation;

    __operation = new org.apache.axis2.description.OutInAxisOperation();

    __operation.setName( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "getDetails" ) );
    _service.addOperation( __operation );

    _operations[ 2 ] = __operation;

    __operation = new org.apache.axis2.description.OutOnlyAxisOperation();

    __operation.setName( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "setString" ) );
    _service.addOperation( __operation );

    _operations[ 3 ] = __operation;

    __operation = new org.apache.axis2.description.OutOnlyAxisOperation();

    __operation.setName( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "throwsError1" ) );
    _service.addOperation( __operation );

    _operations[ 4 ] = __operation;

  }

  // populates the faults
  private void populateFaults() {

  }

  /**
   * Constructor that takes in a configContext
   */

  public ServiceStub( org.apache.axis2.context.ConfigurationContext configurationContext,
                      java.lang.String targetEndpoint ) throws org.apache.axis2.AxisFault {
    this( configurationContext, targetEndpoint, false );
  }

  /**
   * Constructor that takes in a configContext and useseperate listner
   */
  public ServiceStub( org.apache.axis2.context.ConfigurationContext configurationContext,
                      java.lang.String targetEndpoint, boolean useSeparateListener ) throws org.apache.axis2.AxisFault {
    // To populate AxisService
    populateAxisService();
    populateFaults();

    _serviceClient = new org.apache.axis2.client.ServiceClient( configurationContext, _service );

    configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

    _serviceClient.getOptions().setTo( new org.apache.axis2.addressing.EndpointReference( targetEndpoint ) );
    _serviceClient.getOptions().setUseSeparateListener( useSeparateListener );

    // Set the soap version
    _serviceClient.getOptions().setSoapVersionURI( org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI );

  }

  /**
   * Default Constructor
   */
  public ServiceStub( org.apache.axis2.context.ConfigurationContext configurationContext )
    throws org.apache.axis2.AxisFault {

    this( configurationContext, "http://testhost:8080/testcontext/content/ws-run/StubService" );

  }

  /**
   * Default Constructor
   */
  public ServiceStub() throws org.apache.axis2.AxisFault {

    this( "http://testhost:8080/testcontext/content/ws-run/StubService" );

  }

  /**
   * Constructor taking the target endpoint
   */
  public ServiceStub( java.lang.String targetEndpoint ) throws org.apache.axis2.AxisFault {
    this( null, targetEndpoint );
  }

  /**
   * Auto generated method signature
   *
   * @see org.pentaho.test.platform.plugin.services.webservices.StubService#throwsError2
   */

  public org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response throwsError2(

  )

    throws java.rmi.RemoteException {
    org.apache.axis2.context.MessageContext _messageContext = null;
    try {
      org.apache.axis2.client.OperationClient _operationClient =
        _serviceClient.createClient( _operations[ 0 ].getName() );
      _operationClient.getOptions().setAction( "urn:throwsError2" );
      _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault( true );

      addPropertyToOperationClient( _operationClient,
        org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&" );

      // create a message context
      _messageContext = new org.apache.axis2.context.MessageContext();

      // create SOAP envelope with that payload
      org.apache.axiom.soap.SOAPEnvelope env = null;

      // Style is taken to be "document". No input parameters
      // according to the WS-Basic profile in this case we have to send an empty soap message
      org.apache.axiom.soap.SOAPFactory factory = getFactory( _operationClient.getOptions().getSoapVersionURI() );
      env = factory.getDefaultEnvelope();

      // adding SOAP soap_headers
      _serviceClient.addHeadersToEnvelope( env );
      // set the message context with that soap envelope
      _messageContext.setEnvelope( env );

      // add the message contxt to the operation client
      _operationClient.addMessageContext( _messageContext );

      // execute the operation client
      _operationClient.execute( true );

      org.apache.axis2.context.MessageContext _returnMessageContext =
        _operationClient.getMessageContext( org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE );
      org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

      java.lang.Object object =
        fromOM( _returnEnv.getBody().getFirstElement(),
          org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response.class,
          getEnvelopeNamespaces( _returnEnv ) );

      return (org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response) object;

    } catch ( org.apache.axis2.AxisFault f ) {

      org.apache.axiom.om.OMElement faultElt = f.getDetail();
      if ( faultElt != null ) {
        if ( faultExceptionNameMap.containsKey( faultElt.getQName() ) ) {
          // make the fault by reflection
          try {
            java.lang.String exceptionClassName =
              (java.lang.String) faultExceptionClassNameMap.get( faultElt.getQName() );
            java.lang.Class exceptionClass = java.lang.Class.forName( exceptionClassName );
            java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
            // message class
            java.lang.String messageClassName = (java.lang.String) faultMessageMap.get( faultElt.getQName() );
            java.lang.Class messageClass = java.lang.Class.forName( messageClassName );
            java.lang.Object messageObject = fromOM( faultElt, messageClass, null );
            java.lang.reflect.Method m =
              exceptionClass.getMethod( "setFaultMessage", new java.lang.Class[] { messageClass } );
            m.invoke( ex, new java.lang.Object[] { messageObject } );

            throw new java.rmi.RemoteException( ex.getMessage(), ex );
          } catch ( java.lang.ClassCastException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.ClassNotFoundException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.NoSuchMethodException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.reflect.InvocationTargetException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.IllegalAccessException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.InstantiationException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          }
        } else {
          throw f;
        }
      } else {
        throw f;
      }
    } finally {
      _messageContext.getTransportOut().getSender().cleanup( _messageContext );
    }
  }

  /**
   * Auto generated method signature
   *
   * @see org.pentaho.test.platform.plugin.services.webservices.StubService#getString
   */

  public org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse getString(

  )

    throws java.rmi.RemoteException {
    org.apache.axis2.context.MessageContext _messageContext = null;
    try {
      org.apache.axis2.client.OperationClient _operationClient =
        _serviceClient.createClient( _operations[ 1 ].getName() );
      _operationClient.getOptions().setAction( "urn:getString" );
      _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault( true );

      addPropertyToOperationClient( _operationClient,
        org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&" );

      // create a message context
      _messageContext = new org.apache.axis2.context.MessageContext();

      // create SOAP envelope with that payload
      org.apache.axiom.soap.SOAPEnvelope env = null;

      // Style is taken to be "document". No input parameters
      // according to the WS-Basic profile in this case we have to send an empty soap message
      org.apache.axiom.soap.SOAPFactory factory = getFactory( _operationClient.getOptions().getSoapVersionURI() );
      env = factory.getDefaultEnvelope();

      // adding SOAP soap_headers
      _serviceClient.addHeadersToEnvelope( env );
      // set the message context with that soap envelope
      _messageContext.setEnvelope( env );

      // add the message contxt to the operation client
      _operationClient.addMessageContext( _messageContext );

      // execute the operation client
      _operationClient.execute( true );

      org.apache.axis2.context.MessageContext _returnMessageContext =
        _operationClient.getMessageContext( org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE );
      org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

      java.lang.Object object =
        fromOM( _returnEnv.getBody().getFirstElement(),
          org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse.class,
          getEnvelopeNamespaces( _returnEnv ) );

      return (org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse) object;

    } catch ( org.apache.axis2.AxisFault f ) {

      org.apache.axiom.om.OMElement faultElt = f.getDetail();
      if ( faultElt != null ) {
        if ( faultExceptionNameMap.containsKey( faultElt.getQName() ) ) {
          // make the fault by reflection
          try {
            java.lang.String exceptionClassName =
              (java.lang.String) faultExceptionClassNameMap.get( faultElt.getQName() );
            java.lang.Class exceptionClass = java.lang.Class.forName( exceptionClassName );
            java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
            // message class
            java.lang.String messageClassName = (java.lang.String) faultMessageMap.get( faultElt.getQName() );
            java.lang.Class messageClass = java.lang.Class.forName( messageClassName );
            java.lang.Object messageObject = fromOM( faultElt, messageClass, null );
            java.lang.reflect.Method m =
              exceptionClass.getMethod( "setFaultMessage", new java.lang.Class[] { messageClass } );
            m.invoke( ex, new java.lang.Object[] { messageObject } );

            throw new java.rmi.RemoteException( ex.getMessage(), ex );
          } catch ( java.lang.ClassCastException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.ClassNotFoundException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.NoSuchMethodException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.reflect.InvocationTargetException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.IllegalAccessException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.InstantiationException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          }
        } else {
          throw f;
        }
      } else {
        throw f;
      }
    } finally {
      _messageContext.getTransportOut().getSender().cleanup( _messageContext );
    }
  }

  /**
   * Auto generated method signature
   *
   * @param getDetails
   * @see org.pentaho.test.platform.plugin.services.webservices.StubService#getDetails
   */

  public org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse getDetails(

    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails getDetails )

    throws java.rmi.RemoteException {
    org.apache.axis2.context.MessageContext _messageContext = null;
    try {
      org.apache.axis2.client.OperationClient _operationClient =
        _serviceClient.createClient( _operations[ 2 ].getName() );
      _operationClient.getOptions().setAction( "urn:getDetails" );
      _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault( true );

      addPropertyToOperationClient( _operationClient,
        org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&" );

      // create a message context
      _messageContext = new org.apache.axis2.context.MessageContext();

      // create SOAP envelope with that payload
      org.apache.axiom.soap.SOAPEnvelope env = null;

      env =
        toEnvelope( getFactory( _operationClient.getOptions().getSoapVersionURI() ), getDetails,
          optimizeContent( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "getDetails" ) ) );

      // adding SOAP soap_headers
      _serviceClient.addHeadersToEnvelope( env );
      // set the message context with that soap envelope
      _messageContext.setEnvelope( env );

      // add the message contxt to the operation client
      _operationClient.addMessageContext( _messageContext );

      // execute the operation client
      _operationClient.execute( true );

      org.apache.axis2.context.MessageContext _returnMessageContext =
        _operationClient.getMessageContext( org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE );
      org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

      java.lang.Object object =
        fromOM( _returnEnv.getBody().getFirstElement(),
          org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse.class,
          getEnvelopeNamespaces( _returnEnv ) );

      return (org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse) object;

    } catch ( org.apache.axis2.AxisFault f ) {

      org.apache.axiom.om.OMElement faultElt = f.getDetail();
      if ( faultElt != null ) {
        if ( faultExceptionNameMap.containsKey( faultElt.getQName() ) ) {
          // make the fault by reflection
          try {
            java.lang.String exceptionClassName =
              (java.lang.String) faultExceptionClassNameMap.get( faultElt.getQName() );
            java.lang.Class exceptionClass = java.lang.Class.forName( exceptionClassName );
            java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
            // message class
            java.lang.String messageClassName = (java.lang.String) faultMessageMap.get( faultElt.getQName() );
            java.lang.Class messageClass = java.lang.Class.forName( messageClassName );
            java.lang.Object messageObject = fromOM( faultElt, messageClass, null );
            java.lang.reflect.Method m =
              exceptionClass.getMethod( "setFaultMessage", new java.lang.Class[] { messageClass } );
            m.invoke( ex, new java.lang.Object[] { messageObject } );

            throw new java.rmi.RemoteException( ex.getMessage(), ex );
          } catch ( java.lang.ClassCastException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.ClassNotFoundException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.NoSuchMethodException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.reflect.InvocationTargetException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.IllegalAccessException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          } catch ( java.lang.InstantiationException e ) {
            // we cannot intantiate the class - throw the original Axis fault
            throw f;
          }
        } else {
          throw f;
        }
      } else {
        throw f;
      }
    } finally {
      _messageContext.getTransportOut().getSender().cleanup( _messageContext );
    }
  }

  /**
   * Auto generated method signature
   */
  public void setString( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString setString

  ) throws java.rmi.RemoteException {
    org.apache.axis2.context.MessageContext _messageContext = null;

    org.apache.axis2.client.OperationClient _operationClient =
      _serviceClient.createClient( _operations[ 3 ].getName() );
    _operationClient.getOptions().setAction( "urn:setString" );
    _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault( true );

    addPropertyToOperationClient( _operationClient,
      org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&" );

    org.apache.axiom.soap.SOAPEnvelope env = null;
    _messageContext = new org.apache.axis2.context.MessageContext();

    // Style is Doc.

    env =
      toEnvelope( getFactory( _operationClient.getOptions().getSoapVersionURI() ), setString,
        optimizeContent( new javax.xml.namespace.QName( "http://test.webservices.pentaho.org", "setString" ) ) );

    // adding SOAP soap_headers
    _serviceClient.addHeadersToEnvelope( env );
    // create message context with that soap envelope

    _messageContext.setEnvelope( env );

    // add the message contxt to the operation client
    _operationClient.addMessageContext( _messageContext );

    _operationClient.execute( true );

    _messageContext.getTransportOut().getSender().cleanup( _messageContext );

    return;
  }

  /**
   * Auto generated method signature
   */
  public void throwsError1(

  ) throws java.rmi.RemoteException {
    org.apache.axis2.context.MessageContext _messageContext = null;

    org.apache.axis2.client.OperationClient _operationClient =
      _serviceClient.createClient( _operations[ 4 ].getName() );
    _operationClient.getOptions().setAction( "urn:throwsError1" );
    _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault( true );

    addPropertyToOperationClient( _operationClient,
      org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&" );

    org.apache.axiom.soap.SOAPEnvelope env = null;
    _messageContext = new org.apache.axis2.context.MessageContext();

    // Style is taken to be "document". No input parameters
    // according to the WS-Basic profile in this case we have to send an empty soap message
    org.apache.axiom.soap.SOAPFactory factory = getFactory( _operationClient.getOptions().getSoapVersionURI() );
    env = factory.getDefaultEnvelope();

    // adding SOAP soap_headers
    _serviceClient.addHeadersToEnvelope( env );
    // create message context with that soap envelope

    _messageContext.setEnvelope( env );

    // add the message contxt to the operation client
    _operationClient.addMessageContext( _messageContext );

    _operationClient.execute( true );

    _messageContext.getTransportOut().getSender().cleanup( _messageContext );

    return;
  }

  /**
   * A utility method that copies the namepaces from the SOAPEnvelope
   */
  private java.util.Map getEnvelopeNamespaces( org.apache.axiom.soap.SOAPEnvelope env ) {
    java.util.Map returnMap = new java.util.HashMap();
    java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
    while ( namespaceIterator.hasNext() ) {
      org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
      returnMap.put( ns.getPrefix(), ns.getNamespaceURI() );
    }
    return returnMap;
  }

  private javax.xml.namespace.QName[] opNameArray = null;

  private boolean optimizeContent( javax.xml.namespace.QName opName ) {

    if ( opNameArray == null ) {
      return false;
    }
    for ( int i = 0; i < opNameArray.length; i++ ) {
      if ( opName.equals( opNameArray[ i ] ) ) {
        return true;
      }
    }
    return false;
  }

  // http://testhost:8080/testcontext/content/ws-run/StubService
  public static class ComplexType implements org.apache.axis2.databinding.ADBBean {
    /*
     * This type was generated from the piece of schema that had name = ComplexType Namespace URI =
     * http://test.webservices.pentaho.org/xsd Namespace Prefix = ns1
     */

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://test.webservices.pentaho.org/xsd" ) ) {
        return "ns1";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for Address
     */

    protected java.lang.String localAddress;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean localAddressTracker = false;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getAddress() {
      return localAddress;
    }

    /**
     * Auto generated setter method
     *
     * @param param Address
     */
    public void setAddress( java.lang.String param ) {

      if ( param != null ) {
        // update the setting tracker
        localAddressTracker = true;
      } else {
        localAddressTracker = true;

      }

      this.localAddress = param;

    }

    /**
     * field for Age
     */

    protected int localAge;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean localAgeTracker = false;

    /**
     * Auto generated getter method
     *
     * @return int
     */
    public int getAge() {
      return localAge;
    }

    /**
     * Auto generated setter method
     *
     * @param param Age
     */
    public void setAge( int param ) {

      // setting primitive attribute tracker to true

      if ( param == java.lang.Integer.MIN_VALUE ) {
        localAgeTracker = false;

      } else {
        localAgeTracker = true;
      }

      this.localAge = param;

    }

    /**
     * field for Name
     */

    protected java.lang.String localName;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean localNameTracker = false;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getName() {
      return localName;
    }

    /**
     * Auto generated setter method
     *
     * @param param Name
     */
    public void setName( java.lang.String param ) {

      if ( param != null ) {
        // update the setting tracker
        localNameTracker = true;
      } else {
        localNameTracker = true;

      }

      this.localName = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource =
        new org.apache.axis2.databinding.ADBDataSource( this, parentQName ) {

          public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
            throws javax.xml.stream.XMLStreamException {
            ComplexType.this.serialize( parentQName, factory, xmlWriter );
          }
        };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( parentQName, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://test.webservices.pentaho.org/xsd" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":ComplexType",
            xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "ComplexType", xmlWriter );
        }

      }
      if ( localAddressTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "address", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "address" );
          }

        } else {
          xmlWriter.writeStartElement( "address" );
        }

        if ( localAddress == null ) {
          // write the nil attribute

          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );

        } else {

          xmlWriter.writeCharacters( localAddress );

        }

        xmlWriter.writeEndElement();
      }
      if ( localAgeTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "age", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "age" );
          }

        } else {
          xmlWriter.writeStartElement( "age" );
        }

        if ( localAge == java.lang.Integer.MIN_VALUE ) {

          throw new org.apache.axis2.databinding.ADBException( "age cannot be null!!" );

        } else {
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( localAge ) );
        }

        xmlWriter.writeEndElement();
      }
      if ( localNameTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "name", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "name" );
          }

        } else {
          xmlWriter.writeStartElement( "name" );
        }

        if ( localName == null ) {
          // write the nil attribute

          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );

        } else {

          xmlWriter.writeCharacters( localName );

        }

        xmlWriter.writeEndElement();
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( localAddressTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "address" ) );

        elementList.add( localAddress == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
          .convertToString( localAddress ) );
      }
      if ( localAgeTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "age" ) );

        elementList.add( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( localAge ) );
      }
      if ( localNameTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "name" ) );

        elementList.add( localName == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
          .convertToString( localName ) );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static ComplexType parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        ComplexType object = new ComplexType();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"ComplexType".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (ComplexType) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "address" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( !"true".equals( nillableValue ) && !"1".equals( nillableValue ) ) {

              java.lang.String content = reader.getElementText();

              object.setAddress( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( content ) );

            } else {

              reader.getElementText(); // throw away text nodes if any.
            }

            reader.next();

          } // End of if for expected property start element

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "age" ).equals( reader.getName() ) ) {

            java.lang.String content = reader.getElementText();

            object.setAge( org.apache.axis2.databinding.utils.ConverterUtil.convertToInt( content ) );

            reader.next();

          } else {

            object.setAge( java.lang.Integer.MIN_VALUE );

          }

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "name" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( !"true".equals( nillableValue ) && !"1".equals( nillableValue ) ) {

              java.lang.String content = reader.getElementText();

              object.setName( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( content ) );

            } else {

              reader.getElementText(); // throw away text nodes if any.
            }

            reader.next();

          }

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class

  }

  public static class ExtensionMapper {

    public static java.lang.Object getTypeObject( java.lang.String namespaceURI, java.lang.String typeName,
                                                  javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {

      if ( "http://test.webservices.pentaho.org/xsd".equals( namespaceURI ) && "ComplexType".equals( typeName ) ) {

        return ComplexType.Factory.parse( reader );

      }

      throw new org.apache.axis2.databinding.ADBException( "Unsupported type " + namespaceURI + " " + typeName );
    }

  }

  public static class GetDetailsResponse implements org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
      "http://webservice.pentaho.com", "getDetailsResponse", "ns2" );

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://webservice.pentaho.com" ) ) {
        return "ns2";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for _return
     */

    protected ComplexType local_return;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean local_returnTracker = false;

    /**
     * Auto generated getter method
     *
     * @return ComplexType
     */
    public ComplexType get_return() {
      return local_return;
    }

    /**
     * Auto generated setter method
     *
     * @param param _return
     */
    public void set_return( ComplexType param ) {

      if ( param != null ) {
        // update the setting tracker
        local_returnTracker = true;
      } else {
        local_returnTracker = true;

      }

      this.local_return = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource( this, MY_QNAME ) {

        public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
          throws javax.xml.stream.XMLStreamException {
          GetDetailsResponse.this.serialize( MY_QNAME, factory, xmlWriter );
        }
      };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( MY_QNAME, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://webservice.pentaho.com" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
            + ":getDetailsResponse", xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "getDetailsResponse", xmlWriter );
        }

      }
      if ( local_returnTracker ) {
        if ( local_return == null ) {

          java.lang.String namespace2 = "";

          if ( !namespace2.equals( "" ) ) {
            java.lang.String prefix2 = xmlWriter.getPrefix( namespace2 );

            if ( prefix2 == null ) {
              prefix2 = generatePrefix( namespace2 );

              xmlWriter.writeStartElement( prefix2, "return", namespace2 );
              xmlWriter.writeNamespace( prefix2, namespace2 );
              xmlWriter.setPrefix( prefix2, namespace2 );

            } else {
              xmlWriter.writeStartElement( namespace2, "return" );
            }

          } else {
            xmlWriter.writeStartElement( "return" );
          }

          // write the nil attribute
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );
          xmlWriter.writeEndElement();
        } else {
          local_return.serialize( new javax.xml.namespace.QName( "", "return" ), factory, xmlWriter );
        }
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( local_returnTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "return" ) );

        elementList.add( local_return == null ? null : local_return );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static GetDetailsResponse parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        GetDetailsResponse object = new GetDetailsResponse();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"getDetailsResponse".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (GetDetailsResponse) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "return" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( "true".equals( nillableValue ) || "1".equals( nillableValue ) ) {
              object.set_return( null );
              reader.next();

              reader.next();

            } else {

              object.set_return( ComplexType.Factory.parse( reader ) );

              reader.next();
            }
          }

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class

  }

  public static class GetStringResponse implements org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
      "http://webservice.pentaho.com", "getStringResponse", "ns2" );

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://webservice.pentaho.com" ) ) {
        return "ns2";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for _return
     */

    protected java.lang.String local_return;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean local_returnTracker = false;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String get_return() {
      return local_return;
    }

    /**
     * Auto generated setter method
     *
     * @param param _return
     */
    public void set_return( java.lang.String param ) {

      if ( param != null ) {
        // update the setting tracker
        local_returnTracker = true;
      } else {
        local_returnTracker = true;

      }

      this.local_return = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource( this, MY_QNAME ) {

        public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
          throws javax.xml.stream.XMLStreamException {
          GetStringResponse.this.serialize( MY_QNAME, factory, xmlWriter );
        }
      };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( MY_QNAME, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://webservice.pentaho.com" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
            + ":getStringResponse", xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "getStringResponse", xmlWriter );
        }

      }
      if ( local_returnTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "return", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "return" );
          }

        } else {
          xmlWriter.writeStartElement( "return" );
        }

        if ( local_return == null ) {
          // write the nil attribute

          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );

        } else {

          xmlWriter.writeCharacters( local_return );

        }

        xmlWriter.writeEndElement();
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( local_returnTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "return" ) );

        elementList.add( local_return == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
          .convertToString( local_return ) );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static GetStringResponse parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        GetStringResponse object = new GetStringResponse();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"getStringResponse".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (GetStringResponse) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "return" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( !"true".equals( nillableValue ) && !"1".equals( nillableValue ) ) {

              java.lang.String content = reader.getElementText();

              object.set_return( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( content ) );

            } else {

              reader.getElementText(); // throw away text nodes if any.
            }

            reader.next();

          }

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class

  }

  public static class SetString implements org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
      "http://webservice.pentaho.com", "setString", "ns2" );

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://webservice.pentaho.com" ) ) {
        return "ns2";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for Str
     */

    protected java.lang.String localStr;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean localStrTracker = false;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getStr() {
      return localStr;
    }

    /**
     * Auto generated setter method
     *
     * @param param Str
     */
    public void setStr( java.lang.String param ) {

      if ( param != null ) {
        // update the setting tracker
        localStrTracker = true;
      } else {
        localStrTracker = true;

      }

      this.localStr = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource( this, MY_QNAME ) {

        public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
          throws javax.xml.stream.XMLStreamException {
          SetString.this.serialize( MY_QNAME, factory, xmlWriter );
        }
      };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( MY_QNAME, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://webservice.pentaho.com" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":setString",
            xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "setString", xmlWriter );
        }

      }
      if ( localStrTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "str", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "str" );
          }

        } else {
          xmlWriter.writeStartElement( "str" );
        }

        if ( localStr == null ) {
          // write the nil attribute

          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );

        } else {

          xmlWriter.writeCharacters( localStr );

        }

        xmlWriter.writeEndElement();
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( localStrTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "str" ) );

        elementList.add( localStr == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
          .convertToString( localStr ) );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static SetString parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        SetString object = new SetString();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"setString".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (SetString) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "str" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( !"true".equals( nillableValue ) && !"1".equals( nillableValue ) ) {

              java.lang.String content = reader.getElementText();

              object.setStr( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( content ) );

            } else {

              reader.getElementText(); // throw away text nodes if any.
            }

            reader.next();

          }
          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class

  }

  public static class ThrowsError2Response implements org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
      "http://webservice.pentaho.com", "throwsError2Response", "ns2" );

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://webservice.pentaho.com" ) ) {
        return "ns2";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for _return
     */

    protected java.lang.String local_return;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean local_returnTracker = false;

    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String get_return() {
      return local_return;
    }

    /**
     * Auto generated setter method
     *
     * @param param _return
     */
    public void set_return( java.lang.String param ) {

      if ( param != null ) {
        // update the setting tracker
        local_returnTracker = true;
      } else {
        local_returnTracker = true;

      }

      this.local_return = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource( this, MY_QNAME ) {

        public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
          throws javax.xml.stream.XMLStreamException {
          ThrowsError2Response.this.serialize( MY_QNAME, factory, xmlWriter );
        }
      };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( MY_QNAME, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://webservice.pentaho.com" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
            + ":throwsError2Response", xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "throwsError2Response",
            xmlWriter );
        }

      }
      if ( local_returnTracker ) {
        namespace = "";
        if ( !namespace.equals( "" ) ) {
          prefix = xmlWriter.getPrefix( namespace );

          if ( prefix == null ) {
            prefix = generatePrefix( namespace );

            xmlWriter.writeStartElement( prefix, "return", namespace );
            xmlWriter.writeNamespace( prefix, namespace );
            xmlWriter.setPrefix( prefix, namespace );

          } else {
            xmlWriter.writeStartElement( namespace, "return" );
          }

        } else {
          xmlWriter.writeStartElement( "return" );
        }

        if ( local_return == null ) {
          // write the nil attribute

          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );

        } else {

          xmlWriter.writeCharacters( local_return );

        }

        xmlWriter.writeEndElement();
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( local_returnTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "return" ) );

        elementList.add( local_return == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
          .convertToString( local_return ) );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static ThrowsError2Response parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        ThrowsError2Response object = new ThrowsError2Response();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"throwsError2Response".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (ThrowsError2Response) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "return" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( !"true".equals( nillableValue ) && !"1".equals( nillableValue ) ) {

              java.lang.String content = reader.getElementText();

              object.set_return( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( content ) );

            } else {

              reader.getElementText(); // throw away text nodes if any.
            }

            reader.next();

          }
          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class

  }

  public static class GetDetails implements org.apache.axis2.databinding.ADBBean {

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
      "http://webservice.pentaho.com", "getDetails", "ns2" );

    private static java.lang.String generatePrefix( java.lang.String namespace ) {
      if ( namespace.equals( "http://webservice.pentaho.com" ) ) {
        return "ns2";
      }
      return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * field for Object
     */

    protected ComplexType localObject;

    /*
     * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will be
     * used to determine whether to include this field in the serialized XML
     */
    protected boolean localObjectTracker = false;

    /**
     * Auto generated getter method
     *
     * @return ComplexType
     */
    public ComplexType getObject() {
      return localObject;
    }

    /**
     * Auto generated setter method
     *
     * @param param Object
     */
    public void setObject( ComplexType param ) {

      if ( param != null ) {
        // update the setting tracker
        localObjectTracker = true;
      } else {
        localObjectTracker = true;

      }

      this.localObject = param;

    }

    /**
     * isReaderMTOMAware
     *
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware( javax.xml.stream.XMLStreamReader reader ) {
      boolean isReaderMTOMAware = false;

      try {
        isReaderMTOMAware =
          java.lang.Boolean.TRUE
            .equals( reader.getProperty( org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE ) );
      } catch ( java.lang.IllegalArgumentException e ) {
        isReaderMTOMAware = false;
      }
      return isReaderMTOMAware;
    }

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement( final javax.xml.namespace.QName parentQName,
                                                       final org.apache.axiom.om.OMFactory factory )
      throws org.apache.axis2.databinding.ADBException {

      org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource( this, MY_QNAME ) {

        public void serialize( org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
          throws javax.xml.stream.XMLStreamException {
          GetDetails.this.serialize( MY_QNAME, factory, xmlWriter );
        }
      };
      return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl( MY_QNAME, factory, dataSource );

    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
      serialize( parentQName, factory, xmlWriter, false );
    }

    public void serialize( final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                           org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                           boolean serializeType )
      throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

      java.lang.String prefix = null;
      java.lang.String namespace = null;

      prefix = parentQName.getPrefix();
      namespace = parentQName.getNamespaceURI();

      if ( ( namespace != null ) && ( namespace.trim().length() > 0 ) ) {
        java.lang.String writerPrefix = xmlWriter.getPrefix( namespace );
        if ( writerPrefix != null ) {
          xmlWriter.writeStartElement( namespace, parentQName.getLocalPart() );
        } else {
          if ( prefix == null ) {
            prefix = generatePrefix( namespace );
          }

          xmlWriter.writeStartElement( prefix, parentQName.getLocalPart(), namespace );
          xmlWriter.writeNamespace( prefix, namespace );
          xmlWriter.setPrefix( prefix, namespace );
        }
      } else {
        xmlWriter.writeStartElement( parentQName.getLocalPart() );
      }

      if ( serializeType ) {

        java.lang.String namespacePrefix = registerPrefix( xmlWriter, "http://webservice.pentaho.com" );
        if ( ( namespacePrefix != null ) && ( namespacePrefix.trim().length() > 0 ) ) {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix + ":getDetails",
            xmlWriter );
        } else {
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "getDetails", xmlWriter );
        }

      }
      if ( localObjectTracker ) {
        if ( localObject == null ) {

          java.lang.String namespace2 = "";

          if ( !namespace2.equals( "" ) ) {
            java.lang.String prefix2 = xmlWriter.getPrefix( namespace2 );

            if ( prefix2 == null ) {
              prefix2 = generatePrefix( namespace2 );

              xmlWriter.writeStartElement( prefix2, "object", namespace2 );
              xmlWriter.writeNamespace( prefix2, namespace2 );
              xmlWriter.setPrefix( prefix2, namespace2 );

            } else {
              xmlWriter.writeStartElement( namespace2, "object" );
            }

          } else {
            xmlWriter.writeStartElement( "object" );
          }

          // write the nil attribute
          writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter );
          xmlWriter.writeEndElement();
        } else {
          localObject.serialize( new javax.xml.namespace.QName( "", "object" ), factory, xmlWriter );
        }
      }
      xmlWriter.writeEndElement();

    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute( java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                 java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( xmlWriter.getPrefix( namespace ) == null ) {
        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );

      }

      xmlWriter.writeAttribute( namespace, attName, attValue );

    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute( java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                                 javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attValue );
      }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute( java.lang.String namespace, java.lang.String attName,
                                      javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      java.lang.String attributeNamespace = qname.getNamespaceURI();
      java.lang.String attributePrefix = xmlWriter.getPrefix( attributeNamespace );
      if ( attributePrefix == null ) {
        attributePrefix = registerPrefix( xmlWriter, attributeNamespace );
      }
      java.lang.String attributeValue;
      if ( attributePrefix.trim().length() > 0 ) {
        attributeValue = attributePrefix + ":" + qname.getLocalPart();
      } else {
        attributeValue = qname.getLocalPart();
      }

      if ( namespace.equals( "" ) ) {
        xmlWriter.writeAttribute( attName, attributeValue );
      } else {
        registerPrefix( xmlWriter, namespace );
        xmlWriter.writeAttribute( namespace, attName, attributeValue );
      }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName( javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String namespaceURI = qname.getNamespaceURI();
      if ( namespaceURI != null ) {
        java.lang.String prefix = xmlWriter.getPrefix( namespaceURI );
        if ( prefix == null ) {
          prefix = generatePrefix( namespaceURI );
          xmlWriter.writeNamespace( prefix, namespaceURI );
          xmlWriter.setPrefix( prefix, namespaceURI );
        }

        if ( prefix.trim().length() > 0 ) {
          xmlWriter.writeCharacters( prefix + ":"
            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        } else {
          // i.e this is the default namespace
          xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
        }

      } else {
        xmlWriter.writeCharacters( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qname ) );
      }
    }

    private void writeQNames( javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter )
      throws javax.xml.stream.XMLStreamException {

      if ( qnames != null ) {
        // we have to store this data until last moment since it is not possible to write any
        // namespace data after writing the charactor data
        java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
        java.lang.String namespaceURI = null;
        java.lang.String prefix = null;

        for ( int i = 0; i < qnames.length; i++ ) {
          if ( i > 0 ) {
            stringToWrite.append( " " );
          }
          namespaceURI = qnames[ i ].getNamespaceURI();
          if ( namespaceURI != null ) {
            prefix = xmlWriter.getPrefix( namespaceURI );
            if ( ( prefix == null ) || ( prefix.length() == 0 ) ) {
              prefix = generatePrefix( namespaceURI );
              xmlWriter.writeNamespace( prefix, namespaceURI );
              xmlWriter.setPrefix( prefix, namespaceURI );
            }

            if ( prefix.trim().length() > 0 ) {
              stringToWrite.append( prefix ).append( ":" ).append(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            } else {
              stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
            }
          } else {
            stringToWrite.append( org.apache.axis2.databinding.utils.ConverterUtil.convertToString( qnames[ i ] ) );
          }
        }
        xmlWriter.writeCharacters( stringToWrite.toString() );
      }

    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix( javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace )
      throws javax.xml.stream.XMLStreamException {
      java.lang.String prefix = xmlWriter.getPrefix( namespace );

      if ( prefix == null ) {
        prefix = generatePrefix( namespace );

        while ( xmlWriter.getNamespaceContext().getNamespaceURI( prefix ) != null ) {
          prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        xmlWriter.writeNamespace( prefix, namespace );
        xmlWriter.setPrefix( prefix, namespace );
      }

      return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser( javax.xml.namespace.QName qName )
      throws org.apache.axis2.databinding.ADBException {

      java.util.ArrayList elementList = new java.util.ArrayList();
      java.util.ArrayList attribList = new java.util.ArrayList();

      if ( localObjectTracker ) {
        elementList.add( new javax.xml.namespace.QName( "", "object" ) );

        elementList.add( localObject == null ? null : localObject );
      }

      return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl( qName, elementList.toArray(),
        attribList.toArray() );

    }

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {

      /**
       * static method to create the object Precondition: If this object is an element, the current or next start
       * element starts this object and any intervening reader events are ignorable If this object is not an element, it
       * is a complex type and the reader is at the event just after the outer start element Postcondition: If this
       * object is an element, the reader is positioned at its end element If this object is a complex type, the reader
       * is positioned at the end element of its outer element
       */
      public static GetDetails parse( javax.xml.stream.XMLStreamReader reader ) throws java.lang.Exception {
        GetDetails object = new GetDetails();

        int event;
        java.lang.String nillableValue = null;
        java.lang.String prefix = "";
        java.lang.String namespaceuri = "";
        try {

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" ) != null ) {
            java.lang.String fullTypeName =
              reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "type" );
            if ( fullTypeName != null ) {
              java.lang.String nsPrefix = null;
              if ( fullTypeName.indexOf( ":" ) > -1 ) {
                nsPrefix = fullTypeName.substring( 0, fullTypeName.indexOf( ":" ) );
              }
              nsPrefix = nsPrefix == null ? "" : nsPrefix;

              java.lang.String type = fullTypeName.substring( fullTypeName.indexOf( ":" ) + 1 );

              if ( !"getDetails".equals( type ) ) {
                // find namespace for the prefix
                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI( nsPrefix );
                return (GetDetails) ExtensionMapper.getTypeObject( nsUri, type, reader );
              }

            }

          }

          // Note all attributes that were handled. Used to differ normal attributes
          // from anyAttributes.
          java.util.Vector handledAttributes = new java.util.Vector();

          reader.next();

          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() && new javax.xml.namespace.QName( "", "object" ).equals( reader.getName() ) ) {

            nillableValue = reader.getAttributeValue( "http://www.w3.org/2001/XMLSchema-instance", "nil" );
            if ( "true".equals( nillableValue ) || "1".equals( nillableValue ) ) {
              object.setObject( null );
              reader.next();

              reader.next();

            } else {

              object.setObject( ComplexType.Factory.parse( reader ) );

              reader.next();
            }
          }
          while ( !reader.isStartElement() && !reader.isEndElement() ) {
            reader.next();
          }

          if ( reader.isStartElement() )
          // A start element we are not expecting indicates a trailing invalid property
          {
            throw new org.apache.axis2.databinding.ADBException( "Unexpected subelement " + reader.getLocalName() );
          }

        } catch ( javax.xml.stream.XMLStreamException e ) {
          throw new java.lang.Exception( e );
        }

        return object;
      }

    } // end of factory class
  }

  private org.apache.axiom.om.OMElement toOM(
    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response param,
    boolean optimizeContent ) throws org.apache.axis2.AxisFault {

    try {
      return param.getOMElement(
        org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response.MY_QNAME,
        org.apache.axiom.om.OMAbstractFactory.getOMFactory() );
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  private org.apache.axiom.om.OMElement toOM(
    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse param,
    boolean optimizeContent ) throws org.apache.axis2.AxisFault {

    try {
      return param.getOMElement(
        org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse.MY_QNAME,
        org.apache.axiom.om.OMAbstractFactory.getOMFactory() );
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  private org.apache.axiom.om.OMElement toOM(
    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails param, boolean optimizeContent )
    throws org.apache.axis2.AxisFault {

    try {
      return param.getOMElement(
        org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails.MY_QNAME,
        org.apache.axiom.om.OMAbstractFactory.getOMFactory() );
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  private org.apache.axiom.om.OMElement toOM(
    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse param,
    boolean optimizeContent ) throws org.apache.axis2.AxisFault {

    try {
      return param.getOMElement(
        org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse.MY_QNAME,
        org.apache.axiom.om.OMAbstractFactory.getOMFactory() );
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  private org.apache.axiom.om.OMElement toOM(
    org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString param, boolean optimizeContent )
    throws org.apache.axis2.AxisFault {

    try {
      return param.getOMElement(
        org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString.MY_QNAME,
        org.apache.axiom.om.OMAbstractFactory.getOMFactory() );
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  private org.apache.axiom.soap.SOAPEnvelope toEnvelope( org.apache.axiom.soap.SOAPFactory factory,
                                                         org.pentaho.test.platform.plugin.services.webservices.wsdl
                                                           .ServiceStub.GetDetails param,
                                                         boolean optimizeContent )
    throws org.apache.axis2.AxisFault {

    try {

      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
      emptyEnvelope.getBody().addChild(
        param.getOMElement(
          org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails.MY_QNAME, factory ) );
      return emptyEnvelope;
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  /* methods to provide back word compatibility */

  private org.apache.axiom.soap.SOAPEnvelope toEnvelope( org.apache.axiom.soap.SOAPFactory factory,
                                                         org.pentaho.test.platform.plugin.services.webservices.wsdl
                                                           .ServiceStub.SetString param,
                                                         boolean optimizeContent )
    throws org.apache.axis2.AxisFault {

    try {

      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
      emptyEnvelope.getBody().addChild(
        param.getOMElement(
          org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString.MY_QNAME, factory ) );
      return emptyEnvelope;
    } catch ( org.apache.axis2.databinding.ADBException e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }

  }

  /* methods to provide back word compatibility */

  /**
   * get the default envelope
   */
  private org.apache.axiom.soap.SOAPEnvelope toEnvelope( org.apache.axiom.soap.SOAPFactory factory ) {
    return factory.getDefaultEnvelope();
  }

  private java.lang.Object fromOM( org.apache.axiom.om.OMElement param, java.lang.Class type,
                                   java.util.Map extraNamespaces ) throws org.apache.axis2.AxisFault {

    try {

      if ( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response.class
        .equals( type ) ) {

        return org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.ThrowsError2Response.Factory
          .parse( param.getXMLStreamReaderWithoutCaching() );

      }

      if ( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse.class
        .equals( type ) ) {

        return org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetStringResponse.Factory
          .parse( param.getXMLStreamReaderWithoutCaching() );

      }

      if ( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails.class.equals( type ) ) {

        return org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetails.Factory.parse( param
          .getXMLStreamReaderWithoutCaching() );

      }

      if ( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse.class
        .equals( type ) ) {

        return org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.GetDetailsResponse.Factory
          .parse( param.getXMLStreamReaderWithoutCaching() );

      }

      if ( org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString.class.equals( type ) ) {

        return org.pentaho.test.platform.plugin.services.webservices.wsdl.ServiceStub.SetString.Factory.parse( param
          .getXMLStreamReaderWithoutCaching() );

      }

    } catch ( java.lang.Exception e ) {
      throw org.apache.axis2.AxisFault.makeFault( e );
    }
    return null;
  }

}
