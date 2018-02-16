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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.rebind;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.apache.commons.lang.StringUtils;
import org.pentaho.mantle.client.events.EventBusUtil;

import java.io.PrintWriter;
import java.util.ArrayList;

public class EventBusUtilGenerator extends Generator {

  private String packageName;
  private String className;
  private TypeOracle typeOracle;
  private TreeLogger logger;

  @Override
  public String generate( TreeLogger logger, GeneratorContext context, String typeName )
    throws UnableToCompleteException {
    this.logger = logger;
    typeOracle = context.getTypeOracle();

    try {
      // get classType and save instance variables
      JClassType classType = typeOracle.getType( typeName );
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      // Generate class source code
      generateClass( logger, context );

    } catch ( Exception e ) {
      // record to logger that Map generation threw an exception
      logger.log( TreeLogger.ERROR, "PropertyMap ERROR!!!", e );
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  private void generateClass( TreeLogger logger, GeneratorContext context ) {

    // get print writer that receives the source code
    PrintWriter printWriter = null;
    printWriter = context.tryCreate( logger, packageName, className );
    // print writer if null, source code has ALREADY been generated, return
    if ( printWriter == null ) {
      return;
    }

    // init composer, set class properties, create source writer
    ClassSourceFileComposerFactory composer = null;
    composer = new ClassSourceFileComposerFactory( packageName, className );
    composer.addImplementedInterface( EventBusUtil.class.getName() );
    composer.addImport( GwtEvent.class.getName() );
    composer.addImport( JavaScriptObject.class.getName() );
    composer.addImport( EventBus.class.getName() );
    composer.addImport( HandlerRegistration.class.getName() );

    SourceWriter sourceWriter = null;
    sourceWriter = composer.createSourceWriter( context, printWriter );

    sourceWriter.indent();

    // generator constructor source code
    generateConstructor( sourceWriter );
    generateMethods( sourceWriter );

    // close generated class
    sourceWriter.outdent();
    sourceWriter.println( "}" );

    // commit generated class
    context.commit( logger, printWriter );
  }

  private void generateMethods( SourceWriter sourceWriter ) {

    sourceWriter
        .println( "public native void invokeEventBusJSO(final JavaScriptObject jso, final String parameterJSON)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    // unfortunately, we can obtain pairs like {"a":"undefined"} or even {"a":undefined}
    // the latter will crash the procedure, so let's try to clean up the string
    // by removing pairs with 'undefined'
    sourceWriter.println( "var tmp = parameterJSON"
      // replace ("a":undefined) and ("a":"undefined") with empty space
      + ".replace(/\\\"[\\w]+\\\"\\:[ ]*(\\\")?undefined(\\\")?(,)?/g, '')"
      // remove doubled commas
      + ".replace(/,[ ]*,/, ',')"
      // remove leading and trailing commas
      + ".replace(/\\{[ ]*,/, '{')"
      + ".replace(/,[ ]*\\}/, '}');" );
    sourceWriter.println( "var p = JSON.parse(tmp);" );
    sourceWriter.println( "jso.call(this, p)" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println(
          "public native String getParameterString(final String paramName, final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println(
          "public native Integer getParameterInteger(final String paramName, final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println(
          "public native Boolean getParameterBoolean(final String paramName, final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println( "public native Float getParameterFloat(final String paramName, "
          + "final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println(
          "public native Double getParameterDouble(final String paramName, final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println( "public native Long getParameterLong(final String paramName, final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println( "public native Short getParameterShort(final String paramName, "
          + "final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    // *********************
    // FIRE EVENT
    // *********************
    sourceWriter.println();
    sourceWriter.println( "public void fireEvent(final String eventType, final JavaScriptObject parameterMap) { " );
    sourceWriter.indent();
    try {
      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();
      JPackage pack = typeOracle.getPackage( EventBusUtil.class.getPackage().getName() );
      JClassType eventSourceType = typeOracle.getType( GwtEvent.class.getName() );

      for ( JClassType type : pack.getTypes() ) {
        if ( type.isAssignableTo( eventSourceType ) ) {
          implementingTypes.add( type );
        }
      }

      sourceWriter.println( "if(false){}" ); // placeholder
      for ( JClassType implementingType : implementingTypes ) {
        sourceWriter.println( "else if(eventType.equals(\"" + implementingType.getSimpleSourceName() + "\")){" );
        sourceWriter.indent();
        sourceWriter.println( implementingType.getName() + " event = new " + implementingType.getName() + "();" );
        for ( JMethod eventMethod : implementingType.getMethods() ) {
          if ( eventMethod.isPublic() && !eventMethod.isStatic() && eventMethod.isConstructor() == null
              && eventMethod.getName().startsWith( "set" ) ) {
            String propertyName = eventMethod.getName().substring( 3 );
            propertyName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
            String simpleType = implementingType.getField( propertyName ).getType().getSimpleSourceName();
            if ( "string".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterString(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "integer".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterInteger(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "float".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterFloat(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "double".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterDouble(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "long".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterLong(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "short".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterShort(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "boolean".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "event." + eventMethod.getName() + "(getParameterBoolean(\"" + propertyName
                  + "\", parameterMap));" );
            }
          }
        }

        sourceWriter.println( "EVENT_BUS.fireEvent(event);" );
        sourceWriter.outdent();
        sourceWriter.println( "}" );
      }

    } catch ( Exception e ) {
      // record to logger that Map generation threw an exception
      logger.log( TreeLogger.ERROR, "Error generating BindingContext!!!", e );
    }
    sourceWriter.outdent();
    sourceWriter.println( "}" );

    // *********************
    // ADD HANDLER
    // *********************
    sourceWriter.println();
    sourceWriter.println( "public HandlerRegistration addHandler(final String eventType, final JavaScriptObject handler) { " );
    sourceWriter.indent();
    try {
      // find Command implementors
      JPackage pack = typeOracle.getPackage( EventBusUtil.class.getPackage().getName() );
      JClassType eventSourceType = typeOracle.getType( GwtEvent.class.getName() );

      sourceWriter.println( "if(false){return null;}" ); // placeholder
      for ( JClassType type : pack.getTypes() ) {
        if ( type.isAssignableTo( eventSourceType ) ) {
          addHandlerElseCondition( sourceWriter, type );
        }
      }
    } catch ( Exception e ) {
      // record to logger that Map generation threw an exception
      logger.log( TreeLogger.ERROR, "Error generating BindingContext!!!", e );
    }

    sourceWriter.indent();
    sourceWriter.println( "return null;" );

    sourceWriter.outdent();
    sourceWriter.println( "}" );
  }

  private void addHandlerElseCondition( SourceWriter writer, JClassType type ) throws NotFoundException {
    writer.println( "else if(eventType.equals(\"" + type.getSimpleSourceName() + "\")){" );
    writer.indent();

    JClassType handlerType =
      typeOracle.getType( EventBusUtil.class.getPackage().getName() + "." + type.getName()
        + "Handler" );
    writer.println( "HandlerRegistration handlerRegistration = EVENT_BUS.addHandler(" + type.getName() + ".TYPE, new "
      + type.getName() + "Handler() {" );
    writer.indent();
    for ( JMethod handlerMethod : handlerType.getMethods() ) {

      String parameterJSON = addHandlerParamJson( type );

      writer.println( "public void " + handlerMethod.getName() + "(" + type.getName()
        + " event) {" );
      writer.indent();
      writer.println( "invokeEventBusJSO(handler," + parameterJSON + ");" );
      writer.outdent();
      writer.println( "}" );
    }
    writer.outdent();
    writer.println( "});" );

    writer.indent();
    writer.println( "return handlerRegistration;" );

    writer.outdent();
    writer.println( "}" );
  }

  // visible for testing purposes
  String addHandlerParamJson( JClassType type ) {
    StringBuilder json = new StringBuilder( 128 );
    json.append( '"' ).append( '{' );
    JMethod[] methods = type.getMethods();
    for ( JMethod eventMethod : methods ) {

      if ( eventMethod.isPublic()
        && !eventMethod.isStatic()
        && ( eventMethod.isConstructor() == null )
        && !"void".equalsIgnoreCase( eventMethod.getReturnType().getSimpleSourceName() )
        && !eventMethod.getName().equals( "getAssociatedType" ) ) {
        // let's add the property to JSON object
        String propertyName = StringUtils.uncapitalize( eventMethod.getName().substring( 3 ) );
        String simpleType = type.getField( propertyName ).getType().getSimpleSourceName();
        if ( "string".equalsIgnoreCase( simpleType ) ) {
          json.append( "\\\"" + propertyName + "\\\":\\\"\" + event." + eventMethod.getName() + "() + \"\\\"" );
        } else {
          json.append( "\\\"" + propertyName + "\\\":\" + event." + eventMethod.getName() + "() + \"" );
        }
        json.append( ',' );
      }
    }

    int lastIndex = json.lastIndexOf( "," );
    if ( lastIndex == -1 ) {
      json.append( '}' );
    } else {
      json.setCharAt( lastIndex, '}' );
    }
    return json.append( '"' ).toString();
  }

  private void generateConstructor( SourceWriter sourceWriter ) {
    // start constructor source generation
    sourceWriter.println( "public " + className + "() { " );
    sourceWriter.indent();
    sourceWriter.println( "super();" );
    sourceWriter.outdent();
    sourceWriter.println( "}" );
  }

  @SuppressWarnings( "unused" )
  private String boxPrimative( JType type ) {
    if ( type.isPrimitive() != null ) {
      JPrimitiveType primative = type.isPrimitive();
      return primative.getQualifiedBoxedSourceName();
    } else {
      return type.getQualifiedSourceName();
    }
  }
}
