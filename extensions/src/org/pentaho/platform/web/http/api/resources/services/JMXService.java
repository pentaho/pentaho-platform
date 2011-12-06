package org.pentaho.platform.web.http.api.resources.services;

import java.lang.reflect.Method;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.pentaho.platform.web.http.api.resources.services.messages.Messages;



public class JMXService {

  private static final Logger logger = Logger.getLogger(JMXService.class);
  
  /**
   * this should contain the jmx provider class, if null, use the JVMs provider
   */
  public static MBeanServer mbeanServer = MBeanRegister.getMBeanServer();
  
  /**
   * this should contain the name of the jmx managed bean to lookup
   */
  String jmxName = null;
  
  /**
   * the name by which this jmx service goes by
   */
  String displayName = null;
  
  /**
   * the id of the jmx service
   */
  String id;
  
  public JMXService(String id, String displayName, String jmxName) {
	  this.id = id;
    this.displayName = displayName;
    this.jmxName = jmxName;
  }

  public String getDisplayName() {
    return displayName;
  }
  
  public Object getAttribute(String attribName) {
    try {
      mbeanServer.getMBeanInfo(new ObjectName(jmxName));
      return mbeanServer.getAttribute(new ObjectName(jmxName), attribName);
    } catch (Exception e) {
      logger.error( Messages.getErrorString( "JMXService.ERROR_0001_ERROR_READING_ATTRIBUTE", attribName ), e ); //$NON-NLS-1$
    }
    return Messages.getErrorString( "JMXService.ERROR_0001_ERROR_READING_ATTRIBUTE", attribName ); //$NON-NLS-1$
  }
  
  public String invoke( String operation, Object params[], String types[] ) {

	  try {
		  Object result = JMXService.mbeanServer.invoke( new ObjectName(jmxName), operation, params, types);
		  if( result == null ) {
			  return null;
		  } else {
			  return result.toString();
		  }
	  } catch (Exception e) {
		  logger.error( Messages.getErrorString( "JMXService.ERROR_0002_COULD_NOT_INVOKE", operation ), e );  //$NON-NLS-1$
	  }
	  return Messages.getErrorString( "JMXService.ERROR_0002_COULD_NOT_INVOKE", operation ); //$NON-NLS-1$
  }

  public boolean setAttribute(String attribName, String str) throws Exception {
    mbeanServer.getMBeanInfo(new ObjectName(jmxName));
	  return setAttribute( attribName, str, jmxName );
  }
  
  @SuppressWarnings("unchecked")
  public static boolean setAttribute(String attribName, String str, String jmxName ) throws Exception {

        // check the type to see if we need to convert
        MBeanAttributeInfo attrs[] = mbeanServer.getMBeanInfo(new ObjectName(jmxName) ).getAttributes();

        Object value = null;
        for( int idx=0; idx < attrs.length; idx++ ) {
        	if( attrs[idx].getName().equals( attribName  )) {
        		if( attrs[idx].getType().equals( "java.lang.String" ) ) { //$NON-NLS-1$
        			value = str;
        		}
        		else if( attrs[idx].getType().equals( "java.lang.Long" ) ) { //$NON-NLS-1$
        			value = Long.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "long" ) ) { //$NON-NLS-1$
        			value = Long.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "java.lang.Integer" ) ) { //$NON-NLS-1$
        			value = Integer.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "int" ) ) { //$NON-NLS-1$
        			value = Integer.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "java.lang.Double" ) ) { //$NON-NLS-1$
        			value = Double.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "double" ) ) { //$NON-NLS-1$
        			value = Double.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "java.lang.Boolean" ) ) { //$NON-NLS-1$
        			value = Boolean.valueOf( str );
        		}
        		else if( attrs[idx].getType().equals( "boolean" ) ) { //$NON-NLS-1$
        			value = Boolean.valueOf( str );
        		} else {
        		  try {
                Class theClass = Class.forName(attrs[idx].getType());
                if (theClass.isEnum()) {
                  value = Enum.valueOf(theClass, str);
                }
        		  } catch (Throwable t) {
        		    // Do nothing.
        		  }
        		}
        		// TODO add numerics and dates
        		break;
        	}
        }
        
    	Attribute attrib = new Attribute(attribName, value);
	      mbeanServer.setAttribute(new ObjectName(jmxName), attrib );
	      return true;
	      
  }
  
  public Object executeOperation(String operationName) {
    try {
    	return mbeanServer.invoke(new ObjectName(jmxName), operationName, null, null);
    } catch (Exception e) {
    	logger.error( Messages.getErrorString( "JMXService.ERROR_0002_COULD_NOT_INVOKE", operationName ), e );  //$NON-NLS-1$
    }
    return 	Messages.getErrorString( "JMXService.ERROR_0002_COULD_NOT_INVOKE", operationName );  //$NON-NLS-1$
  }
  
  private static String getExtXml( String beanId, String attributeId, ObjectName objectName, Map<String, Node> attributesMap ) {
      StringBuffer sb = new StringBuffer();
      String compoundId = beanId+'.'+attributeId;
	  if( attributesMap.containsKey( compoundId )) {
      	Node attrNode = attributesMap.get( compoundId );
      	sb.append( attrNode.asXML() );
      } else {
      	// try to get extended attributes from the class
	        Method method1 = null;
	        Method method2 = null;
	        Class<?> beanClass = null;
	        try {
		        String className = mbeanServer.getObjectInstance( objectName ).getClassName();
		        beanClass = Class.forName(className);
	        } catch (Exception e) {
	        }
	        try {
	        	method1 = beanClass.getMethod( "getInstance", String.class ); //$NON-NLS-1$
	        } catch (Exception e) {
	        }
	        try {
	        	method2 = beanClass.getMethod( "getInstance", (Class []) null ); //$NON-NLS-1$
	        } catch (Exception e) {
	        }
	        if( (method1 != null || method2 != null ) && beanClass != null ) {
	            try {
	            	Object obj = null;
	            	if( method1 != null ) {
		            	obj = method1.invoke( beanClass, beanId );
	            	} else if( method2 != null ) {
		            	obj = method2.invoke( beanClass, new Object[0] );
	            	}  
	            	if( obj != null && obj instanceof MgmtServicesBean ) {
	                	MgmtServicesBean bean = (MgmtServicesBean) obj;
	                	ExtendedAttributes ext = bean.getExtendedAttributes( attributeId );
	                	if( ext != null ) {
	                		ext.setBeanId( beanId );
	                		sb.append( ext.asXml() );
	                	}
	            	}
	            } catch (Exception e) {
	            	logger.error( Messages.getErrorString( "JMXService.ERROR_0003_EXTENDED_ATTRIBUTE_ERROR" ) , e ); //$NON-NLS-1$
	            }
	        }

      }
	  return sb.toString();
  }
  
  public static String getJmxXml( String jmxName, String id, String displayName, Map<String, Node> attributesMap ) {
	    StringBuffer xml = new StringBuffer();
	    xml.append("<jmxxml jmxName=\"" + jmxName + "\"");  //$NON-NLS-1$ //$NON-NLS-2$
	    xml.append( " id=\"" ).append( id ).append( "\">\n" ); //$NON-NLS-1$ //$NON-NLS-2$
	    xml.append( "<name>" ).append( displayName ).append( "</name>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
	    try {
	    	ObjectName objectName = new ObjectName(jmxName);
	      MBeanInfo info = mbeanServer.getMBeanInfo(objectName);
	      for (int i = 0; i < info.getAttributes().length; i++) {
	    	  MBeanAttributeInfo attribute = info.getAttributes()[i];
	    	  String attributeId = id +"."+attribute.getName(); //$NON-NLS-1$
	    	  try {
	  	  	    StringBuffer sb = new StringBuffer();
		        sb.append("   <attribute"); //$NON-NLS-1$
		        sb.append( " id=\"" ).append( attributeId ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append(" type=\"" ).append( attribute.getType() ).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append(" isReadable=\"" ).append( attribute.isReadable() ).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append(" isWritable=\"" ).append( attribute.isWritable() ).append("\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append("<name>" ).append( attribute.getName() ).append("</name>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		        sb.append("<description>" ).append( attribute.getDescription() ).append("</description>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		        if( attribute.isReadable() ) {
			        sb.append("<value>").append( mbeanServer.getAttribute(objectName, attribute.getName()) ).append("</value>\n");  //$NON-NLS-1$//$NON-NLS-2$
		        }
		        
		        sb.append( getExtXml( id, attribute.getName(), objectName, attributesMap ) );
		        sb.append("</attribute>\n"); //$NON-NLS-1$
		        xml.append( sb );
	    	  } catch (Exception e) {
	    	      logger.error( Messages.getErrorString( "JMXService.ERROR_0004_ERROR_CREATING_ATTRIBUTE_XML", attributeId ), e); //$NON-NLS-1$
	    	      xml.append("<error msg=\"" + e.getMessage() + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
	    	  }
	      }
	      
	    } catch (Exception e) {
  	      logger.error( Messages.getErrorString( "JMXService.ERROR_0006_ERROR_CREATING_SERVICE_XML" ), e); //$NON-NLS-1$
	    }
	    xml.append("</jmxxml>\n"); //$NON-NLS-1$
	    return xml.toString();
  }
  
  public String getJmxXml() {
	  return getJmxXml( jmxName, id, displayName, ServiceMgmt.getInstance().getAttributesMap() );
  }

public String getId() {
	return id;
}
}
