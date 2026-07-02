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


package org.pentaho.platform.osgi;

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation which determines the client type by looking of the call stack to the class which called
 * KettleEnvironment.init() / KettleClientEnvironment.init()
 * <p/>
 * This will return Spoon/Pan/Kitchen/Carte in the case of a pdi-client execution. Others will return "default"
 * <p/>
 * Created by nbaker on 3/24/16.
 */
public class ExceptionBasedClientTypeProvider implements IClientTypeProvider {
  protected List<String> targetClassNames =
    Arrays.asList( KettleEnvironment.class.getName(), KettleClientEnvironment.class.getName() );

  // if we run into these classNames, we'll reset environmentInitFound and carry on
  // @see http://jira.pentaho.com/browse/BACKLOG-7784 for removal of KettleDataFactoryModuleInitializer class hard dependency
  protected List<String> disregardClassNames =
    Arrays.asList( "org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactoryModuleInitializer" ); // PRD

  // Useful for unit testing
  public void setTargetClass( Class targetClass ) {
    this.targetClassNames = targetClass != null ?  Arrays.asList( targetClass.getName() ) : new ArrayList<String>();
  }

  // Useful for unit testing
  public void setDisregardClass( Class disregardClass ) {
    this.disregardClassNames = disregardClass != null ? Arrays.asList( disregardClass.getName() ) : new ArrayList<String>();
  }

  @Override public String getClientType() {
    try {
      throw new Exception( "bogusError" );
    } catch ( Exception e ) {
      StackTraceElement[] stackTrace = e.getStackTrace();
      boolean environmentInitFound = false;
      for ( StackTraceElement stackTraceElement : stackTrace ) {
        String className = stackTraceElement.getClassName();
        if ( environmentInitFound && disregardClassNames.contains( className ) ) {
          environmentInitFound = false; // reset and carry on
        }
        if ( environmentInitFound && !targetClassNames.contains( className ) ) {
          // We're the guy who called KettleEnvironment.init() / KettleClientEnvironment.init()
          className = className.substring( className.lastIndexOf( "." ) + 1 ).toLowerCase(); //Spoon, Kitchen...
          if ( className.contains( "$" ) ) {
            className = className.substring( 0, className.indexOf( "$" ) );
          }
          return className;
        }
        if ( targetClassNames.contains( className ) ) {
          environmentInitFound = true;
        }
      }

    }
    return "default";
  }
}
