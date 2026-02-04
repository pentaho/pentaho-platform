/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.io.PrintWriter;
import java.util.ArrayList;

public class CommandExecGenerator extends Generator {

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
    composer.addImplementedInterface( CommandExec.class.getName() );
    composer.addImport( SolutionBrowserPanel.class.getName() );
    composer.addImport( JavaScriptObject.class.getName() );
    composer.addImport( Command.class.getName() );

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
        .println( "public native Float getParameterFloat(final String paramName,"
          + "final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter
        .println(
          "public native Double getParameterDouble(final String paramName, "
            + "final JavaScriptObject parameterMap)" );
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
        .println( "public native Short getParameterShort(final String paramName,"
          + "final JavaScriptObject parameterMap)" );
    sourceWriter.println( "/*-{" );
    sourceWriter.indent();
    sourceWriter.println( "return parameterMap[paramName];" );
    sourceWriter.outdent();
    sourceWriter.println( "}-*/;" );

    sourceWriter.println();
    sourceWriter.println( "public void execute(final String commandName, final JavaScriptObject parameterMap) { " );
    sourceWriter.indent();

    try {
      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();
      JPackage pack = typeOracle.getPackage( CommandExec.class.getPackage().getName() );
      JClassType commandSourceType = typeOracle.getType( Command.class.getName() );

      for ( JClassType type : pack.getTypes() ) {
        if ( type.isAssignableTo( commandSourceType ) ) {
          implementingTypes.add( type );
        }
      }

      sourceWriter.println( "if(false){}" ); // placeholder
      for ( JClassType implementingType : implementingTypes ) {
        if ( implementingType.isAbstract() ) {
          continue;
        }
        sourceWriter.println( "else if(commandName.equals(\"" + implementingType.getSimpleSourceName() + "\")){" );
        sourceWriter.indent();
        sourceWriter.println( implementingType.getName() + " command = new " + implementingType.getName() + "();" );
        for ( JMethod eventMethod : implementingType.getMethods() ) {
          if ( eventMethod.isPublic() && !eventMethod.isStatic() && eventMethod.isConstructor() == null
              && eventMethod.getName().startsWith( "set" ) ) {
            String propertyName = eventMethod.getName().substring( 3 );
            propertyName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
            String simpleType = implementingType.getField( propertyName ).getType().getSimpleSourceName();
            if ( "string".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterString(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "integer".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterInteger(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "float".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterFloat(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "double".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterDouble(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "long".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterLong(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "short".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterShort(\"" + propertyName
                  + "\", parameterMap));" );
            } else if ( "boolean".equalsIgnoreCase( simpleType ) ) {
              sourceWriter.println( "command." + eventMethod.getName() + "(getParameterBoolean(\"" + propertyName
                  + "\", parameterMap));" );
            }
          }
        }

        sourceWriter.println( "command.execute();" );
        sourceWriter.outdent();
        sourceWriter.println( "}" );
      }

    } catch ( Exception e ) {
      // record to logger that Map generation threw an exception
      logger.log( TreeLogger.ERROR, "Error generating BindingContext!!!", e );
    }
    sourceWriter.outdent();
    sourceWriter.println( "}" );
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
