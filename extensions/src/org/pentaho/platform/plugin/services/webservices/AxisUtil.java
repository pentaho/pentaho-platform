package org.pentaho.platform.plugin.services.webservices;

import com.ibm.wsdl.factory.WSDLFactoryImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class AxisUtil {

  public static String WS_EXECUTE_SERVICE_ID = "ws-run"; //$NON-NLS-1$

  public static String WSDL_SERVICE_ID = "ws-wsdl"; //$NON-NLS-1$

  public static Definition getWsdlDefinition( AxisConfiguration axisConfig, IServiceConfig webservice )
    throws Exception {

    String wsdlStr = getWsdl( axisConfig, webservice );
    InputStream in = new ByteArrayInputStream( wsdlStr.getBytes() );
    InputSource source = new InputSource( in );

    WSDLFactoryImpl factory = new WSDLFactoryImpl();
    WSDLReader reader = factory.newWSDLReader();
    Definition def = reader.readWSDL( "", source ); //$NON-NLS-1$
    return def;
  }

  public static String getWsdl( AxisConfiguration axisConfig, IServiceConfig webservice ) throws Exception {
    Class<?> serviceClass = webservice.getServiceClass();
    String name = serviceClass.getSimpleName();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Java2WSDLBuilder java2WsdlBuilder =
        new Java2WSDLBuilder( out, serviceClass.getName(), serviceClass.getClassLoader() );

    // convert the extra classes into a list of class names
    Collection<Class<?>> extraClasses = webservice.getExtraClasses();
    if ( extraClasses != null ) {
      ArrayList<String> extraClassNames = new ArrayList<String>();
      for ( Class<?> extraClass : extraClasses ) {
        extraClassNames.add( extraClass.getName() );
      }
      java2WsdlBuilder.setExtraClasses( extraClassNames );
    }
    java2WsdlBuilder.setSchemaTargetNamespace( "http://webservice.pentaho.com" ); //$NON-NLS-1$

    java2WsdlBuilder.setLocationUri( getWebServiceExecuteUrl() + name );
    java2WsdlBuilder.setTargetNamespacePrefix( "pho" ); //$NON-NLS-1$
    java2WsdlBuilder.setServiceName( name );
    java2WsdlBuilder.setAttrFormDefault( "unqualified" ); //$NON-NLS-1$
    java2WsdlBuilder.setElementFormDefault( "unqualified" ); //$NON-NLS-1$
    java2WsdlBuilder.setGenerateDocLitBare( false );
    java2WsdlBuilder.generateWSDL();

    return new String( out.toByteArray() );
  }

  /**
   * Create a web service from a web service wrapper. The concrete subclass providers the wrappers via
   * getWebServiceWrappers()
   * 
   * @param wrapper
   *          The wrapper
   * @return
   * @throws AxisFault
   */
  public static AxisService createService( IServiceConfig ws, AxisConfiguration axisConfig ) throws AxisFault {
    Class<?> serviceClass = ws.getServiceClass();
    String serviceName = ws.getId();

    if ( axisConfig.getService( serviceName ) != null ) {
      axisConfig.removeService( serviceName );
    }

    AxisService axisService = createService( serviceClass.getName(), axisConfig, serviceClass.getClassLoader() );

    axisService.setName( serviceName );
    axisService.setDocumentation( ws.getDescription() );

    return axisService;
  }

  @SuppressWarnings( "unchecked" )
  private static AxisService createService( String implClass, AxisConfiguration axisConfig, ClassLoader loader )
    throws AxisFault {

    try {
      HashMap messageReciverMap = new HashMap();
      Class inOnlyMessageReceiver = Loader.loadClass( "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver" ); //$NON-NLS-1$
      MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver.newInstance();
      messageReciverMap.put( WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver );
      Class inoutMessageReceiver = Loader.loadClass( "org.apache.axis2.rpc.receivers.RPCMessageReceiver" ); //$NON-NLS-1$
      MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver.newInstance();
      messageReciverMap.put( WSDL2Constants.MEP_URI_IN_OUT, inOutmessageReceiver );
      messageReciverMap.put( WSDL2Constants.MEP_URI_ROBUST_IN_ONLY, inOutmessageReceiver );

      return AxisService.createService( implClass, axisConfig, messageReciverMap, null, null, loader );
    } catch ( Exception e ) {
      throw AxisFault.makeFault( e );
    }
  }

  /**
   * Creates the WSDL for an Axis service
   * 
   * @param axisService
   * @param wrapper
   * @throws Exception
   */
  public static void createServiceWsdl( AxisService axisService, IServiceConfig wsDef, AxisConfiguration axisConfig )
    throws Exception {
    // specific that we are generating the WSDL
    Parameter useOriginalwsdl = new Parameter();
    useOriginalwsdl.setName( "useOriginalwsdl" ); //$NON-NLS-1$
    useOriginalwsdl.setValue( "true" ); //$NON-NLS-1$
    axisService.addParameter( useOriginalwsdl );

    // get the WSDL generation and make it a parameter
    Definition wsdlDefn = AxisUtil.getWsdlDefinition( axisConfig, wsDef );
    Parameter wsdl = new Parameter();
    wsdl.setName( WSDLConstants.WSDL_4_J_DEFINITION );
    wsdl.setValue( wsdlDefn );

    // add the WSDL parameter to the service
    axisService.addParameter( wsdl );
  }

  public static IServiceConfig getSourceDefinition( AxisService axisService,
      SystemSolutionAxisConfigurator axisConfigurator ) {
    return axisConfigurator.getWebServiceDefinition( axisService.getName() );
  }

  /**
   * Currently webservice content generators are wired up by a plugin. The following methods generate urls for executing
   * web service and wsdl generation. These methods are tightly bound to the content generator specifications in the
   * default-plugin of the system solution.
   */
  public static String getWebServiceExecuteUrl() {
    String url =
        PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + "content/" + WS_EXECUTE_SERVICE_ID + "/"; //$NON-NLS-1$ //$NON-NLS-2$
    return url;
  }

  public static String getWebServiceWsdlUrl() {
    String url =
        PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + "content/" + WSDL_SERVICE_ID + "/"; //$NON-NLS-1$ //$NON-NLS-2$
    return url;
  }

}
