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

package org.pentaho.platform.engine.services.solution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class interfaces with a plain old Java object and makes it available as a component within the Pentaho
 * platform.
 * 
 * Resources and Input Parameters are set on a Pojo via setters. Any public setter is available to both, without
 * bias. The setters are called individually for Resources and Input Parameters and as such may be called for each
 * one should a parameter exist in both forms. Resources are processed first, followed by Input Parameters giving
 * Input Parameters the power to override.
 * 
 * All public getters are exposed through the PojoComponent for consumption as Output Parameters within an Action
 * Sequence.
 * 
 * There exist special methods which may be defined on a Pojo (No interface needed) in order to better facilitate
 * integration to the platform. They are as follows: configure validate execute done getOutputs setResources
 * setInputs setLogger setSession setOutputStream / getMimeType
 * 
 * @author jamesdixon
 * @deprecated Pojo components are deprecated, use {@link IAction}
 * 
 */
public class PojoComponent extends ComponentBase {

  private static final long serialVersionUID = 7064470160805918218L;

  protected Object pojo;

  Map<String, Method> getMethods = new HashMap<String, Method>();
  Map<String, List<Method>> setMethods = new HashMap<String, List<Method>>();
  Method executeMethod = null;
  Method validateMethod = null;
  Method doneMethod = null;
  Method resourcesMethod = null;
  Method runtimeInputsMethod = null;
  Method runtimeOutputsMethod = null;
  Method loggerMethod = null;
  Method sessionMethod = null;
  Method configureMethod = null;

  public Log getLogger() {
    return LogFactory.getLog( PojoComponent.class );
  }

  @Override
  public void done() {
    if ( doneMethod != null && pojo != null ) {
      try {
        doneMethod.invoke( pojo, (Object[]) null );
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  protected void callMethod( Method method, Object value ) throws Throwable {
    List<Method> methods = Arrays.asList( new Method[] { method } );
    callMethods( methods, value );
  }

  protected void callMethods( List<Method> methods, Object value ) throws Throwable {
    if ( value instanceof String ) {
      callMethodWithString( methods, value.toString() );
      return;
    }

    boolean done = false;

    for ( Method method : methods ) {
      Class<?>[] paramClasses = method.getParameterTypes();
      if ( paramClasses.length != 1 ) {
        // we don't know how to handle this
        throw new GenericSignatureFormatError();
      }
      Class<?> paramclass = paramClasses[0];
      // do some type safety. this would be the point to do automatic type conversions
      if ( value instanceof IPentahoResultSet && paramclass.equals( IPentahoResultSet.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { (IPentahoResultSet) value } );
        break;
      } else if ( value instanceof java.lang.Boolean
          && ( paramclass.equals( Boolean.class ) || paramclass.equals( boolean.class ) ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof java.lang.Integer
          && ( paramclass.equals( Integer.class ) || paramclass.equals( int.class ) ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof java.lang.Long
          && ( paramclass.equals( Long.class ) || paramclass.equals( long.class ) ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof java.lang.Double
          && ( paramclass.equals( Double.class ) || paramclass.equals( double.class ) ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof java.lang.Float
          && ( paramclass.equals( Float.class ) || paramclass.equals( float.class ) ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof IPentahoStreamSource && paramclass.equals( IPentahoStreamSource.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof Date && paramclass.equals( Date.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof BigDecimal && paramclass.equals( BigDecimal.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof IContentItem && paramclass.equals( IContentItem.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      } else if ( value instanceof IContentItem && paramclass.equals( String.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value.toString() } );
        break;
      } else if ( paramclass.equals( IPentahoSession.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { (IPentahoSession) value } );
        break;
      } else if ( paramclass.equals( Log.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { (Log) value } );
        break;
      }
    }

    if ( !done ) {
      // Try invoking the first instance with what we have
      try {
        methods.get( 0 ).invoke( pojo, new Object[] { value } );
      } catch ( Exception ex ) {
        throw new IllegalArgumentException(
            "No implementation of method \"" + Method.class.getName() + "\" takes a " + value.getClass() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  protected void callMethodWithString( List<Method> methodList, String value ) throws Throwable {
    boolean done = false;

    value = applyInputsToFormat( value );

    // Search ALL instances of a given method for an implementation
    // that takes a single string
    for ( Method method : methodList ) {
      Class<?>[] paramClasses = method.getParameterTypes();
      if ( paramClasses.length != 1 ) {
        // we don't know how to handle this
        throw new GenericSignatureFormatError();
      }

      Class<?> paramclass = paramClasses[0];
      if ( paramclass.equals( String.class ) ) {
        done = true;
        method.invoke( pojo, new Object[] { value } );
        break;
      }
    }

    if ( !done ) {
      for ( Method method : methodList ) {
        Class<?>[] paramClasses = method.getParameterTypes();
        if ( paramClasses.length != 1 ) {
          // we don't know how to handle this
          throw new GenericSignatureFormatError();
        }

        Class<?> paramclass = paramClasses[0];
        if ( paramclass.equals( Boolean.class ) || paramclass.equals( boolean.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new Boolean( value ) } );
          break;
        } else if ( paramclass.equals( Integer.class ) || paramclass.equals( int.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new Integer( value ) } );
          break;
        } else if ( paramclass.equals( Long.class ) || paramclass.equals( long.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new Long( value ) } );
          break;
        } else if ( paramclass.equals( Double.class ) || paramclass.equals( double.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new Double( value ) } );
          break;
        } else if ( paramclass.equals( Float.class ) || paramclass.equals( float.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new Float( value ) } );
          break;
        } else if ( paramclass.equals( BigDecimal.class ) ) {
          done = true;
          method.invoke( pojo, new Object[] { new BigDecimal( value ) } );
          break;
        }
      }
    }
    if ( !done ) {
      throw new GenericSignatureFormatError();
    }
  }

  @SuppressWarnings( { "unchecked" } )
  @Override
  protected boolean executeAction() throws Throwable {

    Set<?> inputNames = getInputNames();
    Element defnNode = (Element) getComponentDefinition();

    // first do the system settings so that component settings and inputs can override them if necessary
    // if( pojo instanceof IConfiguredPojo ) {
    if ( getMethods.containsKey( "CONFIGSETTINGSPATHS" ) && configureMethod != null ) { //$NON-NLS-1$

      Method method = getMethods.get( "CONFIGSETTINGSPATHS" ); //$NON-NLS-1$
      Set<String> settingsPaths = (Set<String>) method.invoke( pojo, new Object[] {} );
      Iterator<String> keys = settingsPaths.iterator();
      Map<String, String> settings = new HashMap<String, String>();
      SystemSettingsParameterProvider params = new SystemSettingsParameterProvider();
      while ( keys.hasNext() ) {
        String path = keys.next();
        String value = params.getStringParameter( path, null );
        if ( value != null ) {
          settings.put( path, value );
        }
      }
      configureMethod.invoke( pojo, new Object[] { settings } );
    }

    // set the PentahoSession
    if ( sessionMethod != null ) {
      callMethods( Arrays.asList( new Method[] { sessionMethod } ), getSession() );
    }

    // set the logger
    if ( loggerMethod != null ) {
      callMethods( Arrays.asList( new Method[] { loggerMethod } ), getLogger() );
    }

    Map<String, Object> inputMap = new HashMap<String, Object>();
    // look at the component settings
    List<?> nodes = defnNode.selectNodes( "*" ); //$NON-NLS-1$
    for ( int idx = 0; idx < nodes.size(); idx++ ) {
      Element node = (Element) nodes.get( idx );
      // inputs may typically contain a dash in them, such as
      // something like "report-definition" and we should expect
      // a setter as setReportDefinition, so we will remove the
      // dashes and everything should proceed as expected
      String name = node.getName().replace( "-", "" ).toUpperCase(); //$NON-NLS-1$ //$NON-NLS-2$
      if ( !name.equals( "CLASS" ) && !name.equals( "OUTPUTSTREAM" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        String value = node.getText();

        List<Method> method = setMethods.get( name );
        if ( method != null ) {
          callMethodWithString( method, value );
        } else if ( runtimeInputsMethod != null ) {
          inputMap.put( name, value );
        } else {
          // Supress error (For string/value replacement)
          getLogger().warn( Messages.getInstance().getString( "PojoComponent.UNUSED_INPUT", name ) ); //$NON-NLS-1$
        }
      }
    }

    Iterator<?> it = null;

    // now process all of the resources and see if we can call them as setters
    Set<?> resourceNames = getResourceNames();
    Map<String, IActionSequenceResource> resourceMap = new HashMap<String, IActionSequenceResource>();
    if ( resourceNames != null && resourceNames.size() > 0 ) {
      it = resourceNames.iterator();
      while ( it.hasNext() ) {
        String name = (String) it.next();
        IActionSequenceResource resource = getResource( name );
        name = name.replace( "-", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        resourceMap.put( name, resource );
        List<Method> methods = setMethods.get( name.toUpperCase() );

        if ( methods != null ) {
          for ( Method method : methods ) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if ( paramTypes.length == 1 ) {
              Object value = null;

              if ( paramTypes[0] == InputStream.class ) {
                value = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
              } else if ( paramTypes[0] == IActionSequenceResource.class ) {
                value = resource;
              } else if ( paramTypes[0] == String.class ) {
                value = getRuntimeContext().getResourceAsString( resource );
              } else if ( paramTypes[0] == Document.class ) {
                value = getRuntimeContext().getResourceAsDocument( resource );
              }

              callMethod( method, value );
            }
          }
          //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
        } else {
          // BISERVER-2715 we should ignore this as the resource might be meant for another component
        }
      }
    }

    // now process all of the inputs, overriding the component settings
    it = inputNames.iterator();
    while ( it.hasNext() ) {
      String name = (String) it.next();
      Object value = getInputValue( name );
      // now that we have the value, we can fix the name
      name = name.replace( "-", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      List<Method> methods = setMethods.get( name.toUpperCase() );
      if ( methods != null ) {
        callMethods( methods, value );
      } else if ( runtimeInputsMethod != null ) {
        inputMap.put( name, value );
      } else {
        // Supress error (For string/value replacement)
        getLogger().warn( Messages.getInstance().getString( "PojoComponent.UNUSED_INPUT", name ) ); //$NON-NLS-1$
      }
    }

    if ( resourceMap.size() > 0 && resourcesMethod != null ) {
      // call the resources setter
      resourcesMethod.invoke( pojo, new Object[] { resourceMap } );
    }

    if ( inputMap.size() > 0 && runtimeInputsMethod != null ) {
      // call the generic input setter
      runtimeInputsMethod.invoke( pojo, new Object[] { inputMap } );
    }

    if ( getOutputNames().contains( "outputstream" ) && setMethods.containsKey( "OUTPUTSTREAM" ) //$NON-NLS-1$ //$NON-NLS-2$
        && getMethods.containsKey( "MIMETYPE" ) ) { //$NON-NLS-1$ 
      // get the mime-type
      // Get the first method to match
      Method method = getMethods.get( "MIMETYPE" ); //$NON-NLS-1$
      String mimeType = (String) method.invoke( pojo, new Object[] {} );
      String mappedOutputName = "outputstream"; //$NON-NLS-1$
      if ( ( getActionDefinition() != null ) && ( getActionDefinition().getOutput( "outputstream" ) != null ) ) { //$NON-NLS-1$
        mappedOutputName = getActionDefinition().getOutput( "outputstream" ).getPublicName(); //$NON-NLS-1$
      }

      // this marks the HttpOutputHandler as contentDone=true, causing the MessageFormatter to not print an error
      IContentItem contentItem = getOutputContentItem( mappedOutputName, mimeType );
      if ( !( contentItem instanceof SimpleContentItem ) ) {
        // SimpleContentItem can't handle being added to outputs because it
        // doesn't have a getInputStream(), and the path used to return
        // null.
        setOutputValue( "outputstream", contentItem ); //$NON-NLS-1$
      }
      // set the output stream
      OutputStream out = contentItem.getOutputStream( getActionName() );
      method = setMethods.get( "OUTPUTSTREAM" ).get( 0 ); //$NON-NLS-1$
      method.invoke( pojo, new Object[] { out } );
    }

    if ( validateMethod != null ) {
      Object obj = validateMethod.invoke( pojo, (Object[]) null );
      if ( obj instanceof Boolean ) {
        Boolean ok = (Boolean) obj;
        if ( !ok ) {
          return false;
        }
      }
    }

    // now execute the pojo
    Boolean result = Boolean.FALSE;
    if ( executeMethod != null ) {
      result = (Boolean) executeMethod.invoke( pojo, new Object[] {} );
    } else {
      // we can only assume we are ok so far
      result = Boolean.TRUE;
    }

    // now handle outputs
    Set<?> outputNames = getOutputNames();
    // first get the runtime outputs
    Map<String, Object> outputMap = new HashMap<String, Object>();
    if ( runtimeOutputsMethod != null ) {
      outputMap = (Map<String, Object>) runtimeOutputsMethod.invoke( pojo, new Object[] {} );
    }
    it = outputNames.iterator();
    while ( it.hasNext() ) {
      String name = (String) it.next();
      //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
      if ( name.equals( "outputstream" ) ) { //$NON-NLS-1$
        // we should be done
      } else {
        IActionParameter param = getOutputItem( name );
        Method method = getMethods.get( name.toUpperCase() );
        if ( method != null ) {
          Object value = method.invoke( pojo, new Object[] {} );
          param.setValue( value );
        } else {
          Object value = outputMap.get( name );
          if ( value != null ) {
            param.setValue( value );
          } else {
            throw new NoSuchMethodException( name );
          }
        }
      }
    }

    return result.booleanValue();
  }

  @Override
  public boolean init() {
    // nothing to do here
    return true;
  }

  @Override
  protected boolean validateAction() {

    boolean ok = false;
    if ( pojo == null && isDefinedInput( "class" ) ) { //$NON-NLS-1$
      String className = getInputStringValue( "class" ); //$NON-NLS-1$

      // try to load the class from a plugin
      IPluginManager pluginMgr = PentahoSystem.get( IPluginManager.class, getSession() );
      if ( pluginMgr != null && pluginMgr.isBeanRegistered( className ) ) {
        try {
          pojo = pluginMgr.getBean( className ); // "className" is actually the plugin bean id in this case
        } catch ( PluginBeanException e ) {
          error( "Could not load bean class from plugin", e ); //$NON-NLS-1$
          return false;
        }
      }

      // the bean class was not found in a plugin, so try the default classloader
      if ( pojo == null ) {
        try {
          // TODO support loading classes from the solution repository
          Class<?> aClass = getClass().getClassLoader().loadClass( className );
          pojo = aClass.newInstance();
        } catch ( Exception ex ) {
          error( "Could not load bean class", ex ); //$NON-NLS-1$
          return false;
        }
      }
    }
    if ( pojo != null ) {
      // By the time we get here, we've got our class
      try {
        Method[] methods = pojo.getClass().getMethods();
        // create a method map
        for ( Method method : methods ) {
          String name = method.getName();
          Class<?>[] paramTypes = method.getParameterTypes();
          if ( name.equals( "getOutputs" ) ) { //$NON-NLS-1$
            runtimeOutputsMethod = method;
          } else if ( name.equals( "setInputs" ) ) { //$NON-NLS-1$
            runtimeInputsMethod = method;
          } else if ( name.equals( "setResources" ) ) { //$NON-NLS-1$
            resourcesMethod = method;
          } else if ( name.equals( "setLogger" ) ) { //$NON-NLS-1$
            if ( paramTypes.length == 1 && paramTypes[0] == Log.class ) {
              loggerMethod = method;
            }
          } else if ( name.equals( "setSession" ) ) { //$NON-NLS-1$
            if ( paramTypes.length == 1 && paramTypes[0] == IPentahoSession.class ) {
              sessionMethod = method;
            }
          } else if ( name.equalsIgnoreCase( "configure" ) ) { //$NON-NLS-1$
            configureMethod = method;
          } else if ( name.startsWith( "set" ) ) { //$NON-NLS-1$
            name = name.substring( 3 ).toUpperCase();
            if ( name.equals( "CLASS" ) ) { //$NON-NLS-1$
              warn( Messages.getInstance().getString( "PojoComponent.CANNOT_USE_SETCLASS" ) ); //$NON-NLS-1$
            } else {
              if ( !setMethods.containsKey( name ) ) {
                setMethods.put( name, new ArrayList<Method>() );
              }

              setMethods.get( name ).add( method );
            }
          } else if ( name.startsWith( "get" ) ) { //$NON-NLS-1$
            name = name.substring( 3 ).toUpperCase();

            getMethods.put( name, method );
          } else if ( name.equalsIgnoreCase( "execute" ) ) { //$NON-NLS-1$
            executeMethod = method;
          } else if ( name.equalsIgnoreCase( "validate" ) ) { //$NON-NLS-1$
            validateMethod = method;
          } else if ( name.equalsIgnoreCase( "done" ) ) { //$NON-NLS-1$
            doneMethod = method;
          }
        }

        ok = true;
      } catch ( Throwable ex ) {
        error( "Could not load object class", ex ); //$NON-NLS-1$
      }
    }

    return ok;
  }

  @Override
  protected boolean validateSystemSettings() {
    // nothing to do here, the pojo must do this during its init
    return true;
  }

  public void setPojo( Object pojo ) {
    this.pojo = pojo;
  }

}
