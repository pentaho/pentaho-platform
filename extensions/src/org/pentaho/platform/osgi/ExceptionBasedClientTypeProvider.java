package org.pentaho.platform.osgi;

import org.pentaho.di.core.KettleEnvironment;

/**
 * An implementation which determines the client type by looking of the call stack to the class which called
 * KettleEnvironment.init()
 * <p/>
 * This will return Spoon/Pan/Kitchen/Carte in the case of a pdi-client execution. Others will return "default"
 * <p/>
 * Created by nbaker on 3/24/16.
 */
public class ExceptionBasedClientTypeProvider implements IClientTypeProvider {
  protected Class targetClass = KettleEnvironment.class;

  public void setTargetClass( Class targetClass ) {
    this.targetClass = targetClass;
  }

  @Override public String getClientType() {
    try {
      throw new Exception( "bogusError" );
    } catch ( Exception e ) {
      StackTraceElement[] stackTrace = e.getStackTrace();
      boolean environmentInitFound = false;
      for ( StackTraceElement stackTraceElement : stackTrace ) {
        String className = stackTraceElement.getClassName();
        if ( environmentInitFound && !className.equals( targetClass.getName() ) ) {
          // We're the guy who called KettleEnvironment.init()
          className = className.substring( className.lastIndexOf( "." ) + 1 ).toLowerCase(); //Spoon, Kitchen...
          if ( className.contains( "$" ) ) {
            className = className.substring( 0, className.indexOf( "$" ) );
          }
          return className;
        }
        if ( className.equals( targetClass.getName() ) ) {
          environmentInitFound = true;
        }
      }

    }
    return "default";
  }
}
