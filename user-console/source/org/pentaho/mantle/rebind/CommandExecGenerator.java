package org.pentaho.mantle.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class CommandExecGenerator extends Generator {

  private String packageName;

  private String className;

  private TypeOracle typeOracle;

  private TreeLogger logger;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    this.logger = logger;
    typeOracle = context.getTypeOracle();

    try {
      // get classType and save instance variables
      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      // Generate class source code
      generateClass(logger, context);

    } catch (Exception e) {

      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "PropertyMap ERROR!!!", e);

    }

    // return the fully qualifed name of the class generated
    return packageName + "." + className;
  }

  private void generateClass(TreeLogger logger, GeneratorContext context) {

    // get print writer that receives the source code
    PrintWriter printWriter = null;
    printWriter = context.tryCreate(logger, packageName, className);
    // print writer if null, source code has ALREADY been generated, return
    if (printWriter == null)
      return;

    // init composer, set class properties, create source writer
    ClassSourceFileComposerFactory composer = null;
    composer = new ClassSourceFileComposerFactory(packageName, className);
    composer.addImplementedInterface(CommandExec.class.getName());
    composer.addImport(SolutionBrowserPerspective.class.getName());
    composer.addImport(Command.class.getName());

    SourceWriter sourceWriter = null;
    sourceWriter = composer.createSourceWriter(context, printWriter);

    sourceWriter.indent();

    // generator constructor source code
    generateConstructor(sourceWriter);
    generateMethods(sourceWriter);

    // close generated class
    sourceWriter.outdent();
    sourceWriter.println("}");

    // commit generated class
    context.commit(logger, printWriter);

  }

  private void generateMethods(SourceWriter sourceWriter) {

    sourceWriter.println("public void execute(String commandName) { ");
    sourceWriter.indent();

    try {

      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();

      JPackage pack = typeOracle.getPackage(AbstractCommand.class.getPackage().getName());

      JClassType eventSourceType = typeOracle.getType(Command.class.getName());

      for (JClassType type : pack.getTypes()) {
        if (type.isAssignableTo(eventSourceType)) {
          implementingTypes.add(type);
        }
      }

      sourceWriter.println("if(false){}"); // placeholder
      for (JClassType implementingType : implementingTypes) {
        sourceWriter.println("else if(commandName.equals(\"" + implementingType.getSimpleSourceName() + "\")){");

        if (implementingType.isDefaultInstantiable()) {
          sourceWriter.println("new " + implementingType.getSimpleSourceName() + "().execute();");
        } else {
          logger.log(TreeLogger.WARN, "Cannot generate auto-scripts for Command type (" + implementingType.getSimpleSourceName()
              + "), needs at least a no-arg constructor");
        }
        sourceWriter.println("}");
      }

    } catch (Exception e) {
      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "Error generating BindingContext!!!", e);

    }

    sourceWriter.outdent();
    sourceWriter.println("}");



    sourceWriter.println("public Command lookupCommand(String commandName){ ");
    sourceWriter.indent();

    try {

      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();

      JPackage pack = typeOracle.getPackage(AbstractCommand.class.getPackage().getName());

      JClassType eventSourceType = typeOracle.getType(Command.class.getName());

      for (JClassType type : pack.getTypes()) {
        if (type.isAssignableTo(eventSourceType)) {
          implementingTypes.add(type);
        }
      }

      sourceWriter.println("if(false){}"); // placeholder
      for (JClassType implementingType : implementingTypes) {
        sourceWriter.println("else if(commandName.equals(\"" + implementingType.getSimpleSourceName() + "\")){");

        if (implementingType.isDefaultInstantiable()) {
          sourceWriter.println("return new " + implementingType.getSimpleSourceName() + "();");
        } else {
          logger.log(TreeLogger.WARN, "Cannot generate auto-scripts for Command type (" + implementingType.getSimpleSourceName()
              + "), needs at least a no-arg constructor");
        }
        sourceWriter.println("}");
      }

    } catch (Exception e) {
      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "Error generating BindingContext!!!", e);

    }
    sourceWriter.println("return null;");
    sourceWriter.outdent();
    sourceWriter.println("}");
  }

  private void generateConstructor(SourceWriter sourceWriter) {
    // start constructor source generation
    sourceWriter.println("public " + className + "() { ");
    sourceWriter.indent();
    sourceWriter.println("super();");

    sourceWriter.outdent();
    sourceWriter.println("}");
  }

  private String boxPrimative(JType type) {
    if (type.isPrimitive() != null) {
      JPrimitiveType primative = type.isPrimitive();
      return primative.getQualifiedBoxedSourceName();
    } else {
      return type.getQualifiedSourceName();
    }
  }
}
