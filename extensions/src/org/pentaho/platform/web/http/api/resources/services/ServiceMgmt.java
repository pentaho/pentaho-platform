package org.pentaho.platform.web.http.api.resources.services;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import org.pentaho.platform.web.http.api.resources.services.messages.Messages;

public class ServiceMgmt {

	public enum ServiceStatus { VOID, OK, ERRORS, PROCESSING };
	
	public static final String DATE_TIME_FORMAT = "MM.dd.yyyy hh:mm:ss:SSS a z"; //$NON-NLS-1$

  private static final long serialVersionUID = -6461343946361097451L;
  
  private static final Logger logger = Logger.getLogger(ServiceMgmt.class);

  List<JMXService> simpleServices = new ArrayList<JMXService>();
  List<MgmtServicesDynamicBean> dynamicServices = new ArrayList<MgmtServicesDynamicBean>();
//  Map<String, JMXService> jmxServiceMap = new HashMap<String, JMXService>();
  Map<String,Node> attributesMap = new HashMap<String,Node>();
  
  private static Map<String,List<AttributeChangeListener>> attributeListeners = new HashMap<String,List<AttributeChangeListener>>();
  private static Map<String,List<BeanChangeListener>> beanListeners = new HashMap<String,List<BeanChangeListener>>();
  
  private Document servicesDoc = null;
  
  private ServiceStatus serviceStatus[];
  
  private static ServiceMgmt serviceMgmt = null;
  
  public static String getServiceStatus( int idx ) {
	  return getInstance().getServiceStatus()[ idx ].toString();
  }
  
  public static String getAttributeValue( String id ) {
	  String value = ""; //$NON-NLS-1$
	  try {
		  value = ServiceMgmt.getInstance().getAttribute( id );
	  } catch (Exception e) {
		  logger.error( Messages.getString( "mgmt.ui.get_failed", id ) ); //$NON-NLS-1$
	  }
	  return value;
	  
  }

  public static String getFooterSolutionPath(  ) {
	  String path = getAttributeValue( "JavaSystemPropertiesTester.pentaho.solutionpath" ); //$NON-NLS-1$
	  if( path == null ) {
		  return ""; //$NON-NLS-1$
	  }
	  path = path.replace('\\', '/');
	  if( path.endsWith( "/" ) ) { //$NON-NLS-1$
		  path = path.substring( 0, path.length()-1 );
	  }
	  int pos = path.lastIndexOf( '/' );
	  if( pos == -1 ) {
		  return path;
	  }
	  String dir = path.substring( pos );
	  path = path.substring( 0, pos );
	  if( path.length() + dir.length() > 90 ) {
		  path = path.substring( 0, 90-3-dir.length() ) + "..."; //$NON-NLS-1$
	  }
	  return path+dir;
  }

  public static String getPlatformVersion(  ) {
	  String version = ""; //$NON-NLS-1$
	  try {
		  version = ServiceMgmt.getInstance().getAttribute( "PentahoVersionInfoTester.PentahoProSubscriptionVersion" ); //$NON-NLS-1$
		  if( version == null ) {
			  return ""; //$NON-NLS-1$
		  }
		  int pos = version.indexOf( "," ); //$NON-NLS-1$
		  if( pos != -1 ) {
			  version = version.substring(0, pos);
			  pos = version.indexOf( " 1" ); //$NON-NLS-1$
			  pos += 1;
			  version = version.substring( pos );
		  } else {
			  return version;
		  }

		  /*
		  int pos = version.indexOf( ',' );
		  if( pos != -1 ) {
			  version = version.substring(0, pos);
			  pos = version.lastIndexOf( ' ' );
			  pos += 1;
			  version = version.substring( pos );
		  } else {
			  return version;
		  }
*/
	  } catch (Exception e) {
		  logger.error( Messages.getString( "mgmt.ui.get_failed", "PentahoVersionInfoTester.PentahoBISuiteVersion" ) ); //$NON-NLS-1$ //$NON-NLS-2$
	  }
	  return version;
	  
  }
  
  public static String getIsProcessing() {
	  ServiceStatus[] status = ServiceMgmt.getInstance().getServiceStatus();
	  boolean processing = false;
	  for( int idx=0; idx<status.length; idx++ ) {
		  if( status[idx] == ServiceStatus.PROCESSING ) {
			  processing = true;
			  break;
		  }
	  }
	  JMXService service = ServiceMgmt.getInstance().getJmxService( "repositoryIO" ); //$NON-NLS-1$
	  String result = service.invoke( "transferDone" , new Object[0], new String[0]); //$NON-NLS-1$
	  if( result.equals( "false" ) ) { //$NON-NLS-1$
		  processing = true;
	  }
	  return new Boolean( processing ).toString();
  }
  
  public static int getServiceCount(  ) {
	  return getInstance().getServiceStatus().length;
  }
  
  public static String getServiceId( int idx ) {
	  return getInstance().getJmxServiceId( idx );
  }

  public static String getNeedChartRefresh() {

	  try {
		  String dateStr = getAttributeValue( "metrics.NewDataForMetricsAvailableDateTime" ); //$NON-NLS-1$
		  SimpleDateFormat fmt = new SimpleDateFormat(DATE_TIME_FORMAT);
		  Date date = fmt.parse( dateStr );
		  Date now = new Date();
		  boolean needed = now.after( date );
		  return Boolean.toString( needed );
	  } catch (Exception e) {
		  
	  }
	  return "false"; //$NON-NLS-1$
  }
  
  public String getJmxServiceId( int idx ) {
	  if( idx < simpleServices.size() ) {
		  return simpleServices.get( idx ).getId();
	  } else {
		  return dynamicServices.get( idx - simpleServices.size() ).getId();
	  }
  }

  public ServiceMgmt(List<JMXService> simpServices, List<MgmtServicesDynamicBean> dynaServices ) {
	    for (JMXService service : simpServices) {
	        addService(service);
	      }
	    for (MgmtServicesDynamicBean service : dynaServices) {
	        addService(service);
	      }
	    serviceStatus = new ServiceStatus[simpleServices.size() + dynamicServices.size()];
	    for( int i=0; i < serviceStatus.length; i++ ) {
	    	serviceStatus[i] = ServiceStatus.VOID;
	    }
    serviceMgmt = this;
  }
  
  public void setServiceStatus( int idx, ServiceStatus status ) {
	  serviceStatus[idx] = status;
  }
  
  public void setServiceStatus( String id, ServiceStatus status ) {
	  int idx = 0;
	  for (JMXService service : simpleServices) {
	    if( id.equals( service.getId() ) ) {
	    	setServiceStatus( idx, status );
	    }
	    idx++;
	  }
	   for (MgmtServicesDynamicBean service : dynamicServices) {
		    if( id.equals( service.getId() ) ) {
		    	setServiceStatus( idx, status );
		    }
		    idx++;
	    }
  }
  
  public ServiceStatus[] getServiceStatus(  ) {
	  return serviceStatus;
  }
  
  public static ServiceMgmt getInstance() {
	  return serviceMgmt;
  }
  
  public static void registerListener( String id, AttributeChangeListener listener ) {
	  List<AttributeChangeListener> list = attributeListeners.get( id );
	  if( list == null ) {
		  list = new ArrayList<AttributeChangeListener>();
		  attributeListeners.put( id, list );
	  }
	  list.add( listener );
  }
  
  public static void registerListener( String id, BeanChangeListener listener ) {
	  List<BeanChangeListener> list = beanListeners.get( id );
	  if( list == null ) {
		  list = new ArrayList<BeanChangeListener>();
		  beanListeners.put( id, list );
	  }
	  list.add( listener );
  }
  
  public List<AttributeChangeListener> getAttributeListeners( String id ) {
	  return attributeListeners.get( id );
  }
  
  public List<BeanChangeListener> getBeanListeners( String id ) {
	  return beanListeners.get( id );
  }
  
  public void getAttributes( String jmxName ) {

	  try {
		  	ObjectName objectName = new ObjectName(jmxName);
		      MBeanInfo info = JMXService.mbeanServer.getMBeanInfo(objectName);
		      String docName = info.getClassName().replace('.', '/');
		      docName += ".xml"; //$NON-NLS-1$
		      InputStream in = this.getClass().getClassLoader().getResourceAsStream( docName );
		      if( in != null ) {
		        // begin danger zone
		        // mlowery Do not modify this block. It fixes PPP-657.
		        String docString = new String(IOUtils.toCharArray(in, "utf-8")); //$NON-NLS-1$
		        Document doc = XmlDom4JHelper.getDocFromString(docString, JarEntityResolver.getInstance()); //$NON-NLS-1$
		        // end danger zone
		    	  List<?> nodes = doc.selectNodes( "/service-attributes/ext" ); //$NON-NLS-1$
		    	  for( Object node : nodes ) {
		    		  Node attr = ((Element)node).selectSingleNode( "@id" ); //$NON-NLS-1$
		    		  String attrId = attr.getText();
		    		  attributesMap.put( attrId, (Element) node );
		    	  }
		      }
	  } catch (Exception e) {
		  logger.error( Messages.getErrorString("ServiceMgmt.ERROR_0001_COULD_NOT_GET_ATTRIBUTE") ); //$NON-NLS-1$
	  }

  }
  
  public void addService(JMXService service) {
	  simpleServices.add(service);
	  getAttributes( service.jmxName );
  }

  public void addService(MgmtServicesDynamicBean service) {
	  dynamicServices.add(service);
	  getAttributes( service.getJmxName() );
  }

  public JMXService getJmxService( String id ) {
	    for (JMXService service : simpleServices) {
	        if( service.getId().equals( id ) ) {
	        	return service;
	        }
	      }
	   return null;
}

  public MgmtServicesDynamicBean getDynamicService( String id ) {
	    for (MgmtServicesDynamicBean service : dynamicServices) {
	        if( service.getId().equals( id ) ) {
	        	return service;
	        }
	      }
	   return null;
}

  public Map<String,Node> getAttributesMap() {
	  return attributesMap;
  }
  
  public StringBuffer getServicesXml( List<String> serviceIds ) {
	    List<JMXService> jmxServices = new ArrayList<JMXService>();
	    List<MgmtServicesDynamicBean> dynaServices = new ArrayList<MgmtServicesDynamicBean>();
	    for (JMXService service : simpleServices) {
	    	if( serviceIds.contains( service.getId() ) ) {
	    		jmxServices.add( service );
	    	}
	    }
	    for (MgmtServicesDynamicBean service : dynamicServices) {
	    	if( serviceIds.contains( service.getId() ) ) {
	    		dynaServices.add( service );
	    	}
	    }
	    return getServicesXml( jmxServices, dynaServices );
  }
  
  public StringBuffer getServicesXml( List<JMXService> jmxServices, List<MgmtServicesDynamicBean> dynaServices ) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("<services>"); //$NON-NLS-1$
	    if( jmxServices != null ) {
		    for (JMXService service : jmxServices) {
		        sb.append("<service>"); //$NON-NLS-1$
		        sb.append("<display>").append(service.getDisplayName()).append("</display>"); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append( service.getJmxXml() );
		        sb.append("</service>"); //$NON-NLS-1$
		    }
	    }
	    if( dynaServices != null ) {
		    for (MgmtServicesDynamicBean service : dynaServices) {
		        sb.append("<service>"); //$NON-NLS-1$
		        sb.append("<display>").append(service.getDisplayName()).append("</display>"); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append( JMXService.getJmxXml( service.getJmxName(), service.getId(), service.getDisplayName(), attributesMap ) );
		        sb.append("</service>"); //$NON-NLS-1$
		    }
	    }
	    sb.append("</services>"); //$NON-NLS-1$
	    
	    return sb;
  }
  
  public void processAll( List<String> serviceIds, boolean sync ) {

	  int idx = 0;
	  
	  if( serviceIds == null || serviceIds.size() == 0 ) {
		  reset();
	  }

	  for( JMXService simpleService : simpleServices ) {
		  if( serviceIds == null || serviceIds.contains( simpleService.getId() ) ) {
			  setServiceStatus( idx , ServiceStatus.VOID );
		  }
		  idx++;
	  }

	  for( MgmtServicesDynamicBean dynamicService : dynamicServices ) {
		  if( serviceIds == null || serviceIds.contains( dynamicService.getId() ) ) {
			  dynamicService.reset();
			  setServiceStatus( idx , ServiceStatus.VOID );
		  }
		  idx++;
	  }
	  
	  if( servicesDoc == null ) {
		  servicesDoc = DocumentHelper.createDocument();
		  Element root = DocumentHelper.createElement( "services" ); //$NON-NLS-1$
		  servicesDoc.add( root );
	  }

	  ServiceProcessor processor = new ServiceProcessor( this, simpleServices, dynamicServices, serviceIds );

	  if( sync ) {
		  processor.process();
	  } else {
		  // Create the thread supplying it with the runnable object
		  Thread thread = new Thread(processor);
		    
		  // Start the thread
		  thread.start();
	  }

  }
  
  public void reset() {
	  servicesDoc = null;
  }
  
  public void refreshService( String serviceId ) {
	  if( servicesDoc == null ) {
		  return;
	  }
	  Node node = servicesDoc.selectSingleNode( "/services/service/jmxxml[@id='"+serviceId+"']" ); //$NON-NLS-1$ //$NON-NLS-2$
	  if (node != null) {
  	  servicesDoc.remove( node );
  	  Element parent = (Element) node.getParent();
  	  node.detach();
  	  String strXml = null;
      for (JMXService service : simpleServices) {
      	if( service.getId().equals( serviceId ) ) {
      		strXml = service.getJmxXml();
      	}
      }
      for (MgmtServicesDynamicBean service : dynamicServices) {
      	if( service.getId().equals( serviceId ) ) {
      		strXml = JMXService.getJmxXml( service.getJmxName(), service.getId(), service.getDisplayName(), attributesMap );
      	}
      }
      if( strXml != null ) {
  		  try {
  			  Document doc = XmlDom4JHelper.getDocFromString(strXml, JarEntityResolver.getInstance());
  			  Node root = (Node) doc.getRootElement();
  			  root.detach();
  			  parent.add( root );
  		  } catch (Exception e) {
  			  logger.error( Messages.getErrorString("ServiceMgmt.ERROR_0002_COULD_NOT_UPDATE_DOCUMENT", serviceId ), e ); //$NON-NLS-1$
  		  }
      }
	  }
  }
  
  public Document getServicesDoc() {
	  if( servicesDoc == null ) {
		  StringBuffer sb = getServicesXml();
		  try {
			  String strXml = sb.toString();
			  servicesDoc = XmlDom4JHelper.getDocFromString(strXml, JarEntityResolver.getInstance());
		  } catch (Exception e) {
			  logger.error( Messages.getErrorString("ServiceMgmt.ERROR_0003_COULD_NOT_GET_DOCUMENT"), e ); //$NON-NLS-1$
			  e.printStackTrace();
		  }
	  }
	  return servicesDoc;
  }
  
  public StringBuffer getServiceXml( String displayName, String jmxXml ) {
	    StringBuffer sb = new StringBuffer();
        sb.append("<service>"); //$NON-NLS-1$
        sb.append("<display>").append(displayName).append("</display>"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( jmxXml );
        sb.append("</service>"); //$NON-NLS-1$
	  return sb;
  }
  
  
  private StringBuffer getServicesXml() {
    StringBuffer sb = new StringBuffer();
    sb.append("<services>"); //$NON-NLS-1$
    int idx = 0;
    for (JMXService service : simpleServices) {
    	setServiceStatus( idx , ServiceStatus.PROCESSING );
    	sb.append( getServiceXml( service.getDisplayName(), service.getJmxXml() ) );
    	setServiceStatus( idx , ServiceStatus.OK );
    	idx++;
      }
    for (MgmtServicesDynamicBean service : dynamicServices) {
    	setServiceStatus( idx , ServiceStatus.PROCESSING );
    	sb.append( getServiceXml( service.getDisplayName(), JMXService.getJmxXml( service.getJmxName(), service.getId(), service.getDisplayName(), attributesMap ) ) );
    	setServiceStatus( idx , ServiceStatus.OK );
    	idx++;
      }
    sb.append("</services>"); //$NON-NLS-1$
    
    return sb;
  }

  public void updateServicesNode( String id, String attributeId, String value ) {
	  if( servicesDoc == null ) {
		  return;
	  }
	  String xPath = "services/service/jmxxml/attribute[@id='"+id+"."+attributeId+"']/value"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  Node node = servicesDoc.selectSingleNode( xPath );
	  if( node != null ) {
		  node.setText( value );
	  }
  }
  
  protected void updateServicesBlock( String id, String xml ) {
	  if( servicesDoc == null ) {
		  return;
	  }
	  String xPath = "services/service/jmxxml[@id='"+id+"']"; //$NON-NLS-1$ //$NON-NLS-2$
	  Node node = servicesDoc.selectSingleNode( xPath );
	  try {
		  Document doc = DocumentHelper.parseText( xml );
		  if( node != null ) {
			  Element serviceNode = node.getParent();
			  serviceNode.remove( node );
			  Element jmxNode = (Element) doc.selectSingleNode( "service/jmxxml[@id='"+id+"']" ); //$NON-NLS-1$ //$NON-NLS-2$
			  jmxNode.detach();
			  serviceNode.add( jmxNode );
		  } else {
			  Element serviceNode = (Element) doc.selectSingleNode( "service" ); //$NON-NLS-1$
			  serviceNode.detach();
			  Element servicesNode = (Element) servicesDoc.selectSingleNode( "services" ); //$NON-NLS-1$
			  servicesNode.add( serviceNode );
		  }
	  } catch (Exception e) {
		  
	  }
  }
  
  public void setAttribute( String id, String value ) throws Exception {
	  int pos = id.indexOf('.');
	  String serviceId = id.substring( 0, pos );
	  String attributeId = id.substring(pos+1);
	  boolean handled = false;
	    for (JMXService service : simpleServices) {
	    	if( service.getId().equals( serviceId ) ) {
	    		service.setAttribute( attributeId , value);
	    		updateServicesNode( serviceId, attributeId, value );
	    		handled = true;
	    		break;
	    	}
	    }
	    if( !handled ) {
		    for (MgmtServicesDynamicBean service : dynamicServices) {
		    	if( service.getId().equals( serviceId ) ) {
		    	      try {
		    	    	  JMXService.setAttribute( attributeId , value, service.getJmxName() );
			    		updateServicesNode( serviceId, attributeId, value );
			    		handled = true;
		    	      } catch (Exception e) {
		    	    	  logger.error( Messages.getErrorString("ServiceMgmt.ERROR_0004_COULD_NOT_SET_ATTRIBUTE", id ), e ); //$NON-NLS-1$
		    	      }
		    	      break;
		    	}
		    }
	    }
	    if( handled ) {
	    	List<AttributeChangeListener> attrListeners = getAttributeListeners( id );
	    	if( attrListeners != null ) {
		    	for( AttributeChangeListener listener : attrListeners ) {
		    		listener.attributeChanged( id );
		    	}
	    	}
	    	List<BeanChangeListener> beanListeners = getBeanListeners( serviceId );
	    	if( beanListeners != null ) {
		    	for( BeanChangeListener listener : beanListeners ) {
		    		listener.beanChanged( serviceId );
		    	}
	    	}
	    }
  }
  
  public String getAttribute( String id ) throws Exception {
	  int pos = id.indexOf('.');
	  String serviceId = id.substring( 0, pos );
	  String attributeId = id.substring(pos+1);
	  boolean handled = false;
	  Object value = ""; //$NON-NLS-1$
	  for (JMXService service : simpleServices) {
	    	if( service.getId().equals( serviceId ) ) {
	    		value = service.getAttribute(attributeId );
  	    		if( value != null ) {
  		    		updateServicesNode( serviceId, attributeId, value.toString() );
  	    		}
	    		handled = true;
	    		break;
	    	}
	    }
	    if( !handled ) {
		    for (MgmtServicesDynamicBean service : dynamicServices) {
		    	if( service.getId().equals( serviceId ) ) {
		    	      try {
		  	    		value = service.getAttribute( attributeId );
		  	    		if( value != null ) {
				    		updateServicesNode( serviceId, attributeId, value.toString() );
		  	    		}
			    		handled = true;
		    	      } catch (Exception e) {
		    	    	  logger.error( Messages.getErrorString("ServiceMgmt.ERROR_0005_COULD_NOT_GET_ATTRIBUTE", id), e ); //$NON-NLS-1$
		    	      }
		    	      break;
		    	}
		    }
	    }
	    if( handled ) {
	    	List<AttributeChangeListener> attrListeners = getAttributeListeners( id );
	    	if( attrListeners != null ) {
		    	for( AttributeChangeListener listener : attrListeners ) {
		    		listener.attributeChanged( id );
		    	}
	    	}
	    	List<BeanChangeListener> beanListeners = getBeanListeners( serviceId );
	    	if( beanListeners != null ) {
		    	for( BeanChangeListener listener : beanListeners ) {
		    		listener.beanChanged( serviceId );
		    	}
	    	}
	    }
	    if( value != null ) {
		    return value.toString();
	    } else {
	    	return ""; //$NON-NLS-1$
	    }
  }
  
  public String invoke( String id, Object params[], String types[] ) {
	  int pos = id.indexOf('.');
	  if( pos == -1 ) {
		  return Messages.getString("ServiceMgmt.invalid_operation_id", id );  //$NON-NLS-1$
	  }
	  String serviceId = id.substring( 0, pos );
	  String attributeId = id.substring(pos+1);
	  String result = Messages.getString("ServiceMgmt.operation_not_found", id ); //$NON-NLS-1$
	    for (JMXService service : simpleServices) {
	    	if( service.getId().equals( serviceId ) ) {
	    		result = service.invoke( attributeId , params, types );
	    		refreshService( service.getId() );
	    	}
	    }
	    
	    for (MgmtServicesDynamicBean service : dynamicServices) {
	    	if( service.getId().equals( serviceId ) ) {
	    		try {
	    			Object resultObject = service.invoke( attributeId , params, types );
	    			if( resultObject != null ) {
			    		result = resultObject.toString();
	    			} else {
	    				result = null;
	    			}
		    		refreshService( service.getId() );
	    		} catch (Exception e) {
	    			result = Messages.getString("ServiceMgmt.operation_failed"); //$NON-NLS-1$
	    		}
	    	}
	    }
	    return result;
  }
  
  private class ServiceProcessor implements Runnable {

	  ServiceMgmt mgmt;
	  List<JMXService> simpleServices;
	  List<MgmtServicesDynamicBean> dynamicServices;
	  List<String> serviceIds;
	  
	  public ServiceProcessor( ServiceMgmt mgmt, List<JMXService> simpleServices, List<MgmtServicesDynamicBean> dynamicServices, List<String> serviceIds ) {
		  this.mgmt = mgmt;
		  this.simpleServices = simpleServices;
		  this.dynamicServices = dynamicServices;
		  this.serviceIds = serviceIds;
	  }
	  
	  public void process() {
    	  int idx = 0;
  	    for (JMXService service : simpleServices) {
  	    	if( serviceIds == null || serviceIds.contains( service.getId() ) ) {
  	    		System.out.println( "Refreshing "+service.getId() );  //$NON-NLS-1$
  	    		mgmt.setServiceStatus( idx , ServiceStatus.PROCESSING );
      	    	String xml = mgmt.getServiceXml( service.getDisplayName(), service.getJmxXml() ).toString();
      	    	mgmt.updateServicesBlock( service.getId() , xml);
      	    	mgmt.setServiceStatus( idx , ServiceStatus.OK );
  	    	} else {
  	    		System.out.println( "Skipping "+service.getId() );  //$NON-NLS-1$
  	    	}
  	    	idx++;
  	      }
  	    for (MgmtServicesDynamicBean service : dynamicServices) {
  	    	if( serviceIds == null || serviceIds.contains( service.getId() ) ) {
      	    	mgmt.setServiceStatus( idx , ServiceStatus.PROCESSING );
      	    	String xml = getServiceXml( service.getDisplayName(), JMXService.getJmxXml( service.getJmxName(), service.getId(), service.getDisplayName(), attributesMap ) ).toString();
      	    	mgmt.updateServicesBlock( service.getId() , xml);
      	    	mgmt.setServiceStatus( idx , ServiceStatus.OK );
  	    		System.out.println( "Refreshing "+service.getId() );  //$NON-NLS-1$
  	    	} else {
  	    		System.out.println( "Skipping "+service.getId() );  //$NON-NLS-1$
  	    	}
  	    	idx++;
  	    }
  		System.out.println( "Refresh complete" );  //$NON-NLS-1$
	  }
	  
      public void run() {
    	  process();
      }
  }
  
}
