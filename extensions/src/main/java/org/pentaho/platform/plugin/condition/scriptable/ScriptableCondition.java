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


package org.pentaho.platform.plugin.condition.scriptable;

import org.apache.commons.logging.Log;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.plugin.action.messages.Messages;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScriptableCondition implements IConditionalExecution {

  private String script;
  private String scriptLanguage = "JavaScript";
  private boolean defaultResult = true;
  private boolean ignoreInputNamesWithMinus = false; // Backward compatibility with old ConditionalExecution
  private boolean listAvailableEngines = false;

  public ScriptableCondition() {
    super();
  }

  public String getScript() {
    return script;
  }

  public void setScript( final String script ) {
    this.script = script;
  }

  public void setScriptLanguage( final String value ) {
    this.scriptLanguage = value;
  }

  public void setDefaultResult( final boolean value ) {
    this.defaultResult = value;
  }

  public boolean getDefaultResult() {
    return this.defaultResult;
  }

  public String getScriptLanguage() {
    return this.scriptLanguage;
  }

  public void setIgnoreInputNamesWithMinus( final boolean value ) {
    this.ignoreInputNamesWithMinus = value;
  }

  public boolean getIgnoreInputNamesWithMinus() {
    return this.ignoreInputNamesWithMinus;
  }

  public void setListAvailableEngines( final boolean value ) {
    this.listAvailableEngines = value;
    if ( value ) {
      System.out.println( "*** DEBUG - Display Script Engine List ***" );
      ScriptEngineManager manager = new ScriptEngineManager();
      List<ScriptEngineFactory> factories = manager.getEngineFactories();
      for ( ScriptEngineFactory factory : factories ) {
        System.out.println( String.format( "Engine %s, Version %s, Language %s, Registered Names: %s", factory
            .getEngineName(), factory.getEngineVersion(), factory.getLanguageName(), factory.getNames().toString() ) );
      }
    }
  }

  public boolean shouldExecute( final Map currentInputs, final Log logger ) throws Exception {
    boolean shouldExecute = this.getDefaultResult();
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName( this.getScriptLanguage() );
    if ( engine == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "ScriptableCondition.ERROR_0001_ENGINE_NOT_AVAILABLE", this.getScriptLanguage() ) ); //$NON-NLS-1$
    }
    Object inputValue;
    IActionParameter inputParameter;
    String inputName = null;
    Iterator inputs = currentInputs.entrySet().iterator();
    Map.Entry mapEntry;
    while ( inputs.hasNext() ) {
      mapEntry = (Map.Entry) inputs.next();
      inputName = (String) mapEntry.getKey();
      if ( this.getIgnoreInputNamesWithMinus() && inputName.indexOf( '-' ) >= 0 ) {
        logger.info( Messages.getInstance().getString( "ScriptableCondition.INFO_IGNORED_INPUT", inputName ) ); //$NON-NLS-1$
        continue;
      }
      inputParameter = (IActionParameter) mapEntry.getValue();
      inputValue = inputParameter.getValue();
      engine.put( inputName, inputValue ); // What happens to resultset objects I wonder...
    }
    engine.put( "out", System.out );
    engine.put( "rule", this );
    Object resultObject = engine.eval( this.getScript() );
    if ( resultObject instanceof Boolean ) {
      return ( (Boolean) resultObject ).booleanValue();
    } else if ( resultObject instanceof String ) {
      return ( "true".equalsIgnoreCase( resultObject.toString() ) ) || ( "yes".equalsIgnoreCase(
        resultObject.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( resultObject instanceof Number ) {
      return ( (Number) resultObject ).intValue() > 0;
    } else if ( resultObject instanceof IPentahoResultSet ) {
      return ( (IPentahoResultSet) resultObject ).getRowCount() > 0;
    }
    logger.info( Messages.getInstance().getString( "ScriptableCondition.INFO_DEFAULT_RESULT_RETURNED" ) ); //$NON-NLS-1$
    return shouldExecute;
  }

}
