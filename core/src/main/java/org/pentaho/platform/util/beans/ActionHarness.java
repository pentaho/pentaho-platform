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


package org.pentaho.platform.util.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.util.messages.Messages;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility for consistent interaction with {@link IAction}s. Based on {@link BeanUtil}, but with logic specific
 * to {@link IAction} beans. One such behavior is the setting of pass-through properties to an
 * {@link IVarArgsAction}. Note that all the setValue and setValues methods will properly handle
 * {@link IVarArgsAction}s if the bean is of this type.
 * 
 * @author aphillips
 */
public class ActionHarness extends BeanUtil {

  private VarArgsWrapperCallback varArgsCallback;

  private static final Log logger = LogFactory.getLog( ActionHarness.class );

  public ActionHarness( IAction actionBean ) {
    super( actionBean );

    setDefaultCallback( new DefaultActionCallback() );

    if ( actionBean instanceof IVarArgsAction ) {
      Map<String, Object> varArgsMap = new HashMap<String, Object>();
      ( (IVarArgsAction) actionBean ).setVarArgs( varArgsMap );
      varArgsCallback = new VarArgsWrapperCallback( varArgsMap );
    }
  }

  /**
   * Performs same function as the super, with the added feature of granting undeclared properties to the action
   * bean if it is a {@link IVarArgsAction}.
   * <p>
   * {@inheritDoc}
   */
  public void setValue( String propertyName, Object value, ValueSetErrorCallback callback,
      PropertyNameFormatter... formatters ) throws Exception {

    if ( varArgsCallback != null ) {
      varArgsCallback.setWrappedCallback( callback );
      varArgsCallback.setValue( value );
      callback = varArgsCallback;
    }

    super.setValue( propertyName, value, callback, formatters );
  }

  public static class VarArgsWrapperCallback implements ValueSetErrorCallback {
    private Map<String, Object> varArgsMap;

    private ValueSetErrorCallback wrappedCallback;

    private Object curValue;

    public VarArgsWrapperCallback( Map<String, Object> varArgsMap ) {
      this.varArgsMap = varArgsMap;
    }

    public void setWrappedCallback( ValueSetErrorCallback wrappedCallback ) {
      this.wrappedCallback = wrappedCallback;
    }

    public void setValue( Object value ) throws Exception {
      curValue = value;
    }

    public void failedToSetValue( Object targetBean, String propertyName, Object value, String beanPropertyType,
        Throwable cause ) throws Exception {
      wrappedCallback.failedToSetValue( targetBean, propertyName, value, beanPropertyType, cause );
    }

    public void propertyNotWritable( Object targetBean, String propertyName ) throws Exception {
      if ( varArgsMap != null ) {
        varArgsMap.put( propertyName, curValue );
      }
      // do not call super here since the action bean does accept these properties and thus this
      // is not an error state.
    }
  }

  public static class DefaultActionCallback extends EagerFailingCallback {

    @Override
    public void propertyNotWritable( Object bean, String propertyName ) throws Exception {
      String beanType = ( bean != null ) ? bean.getClass().getName() : "[ClassNameNotAvailable]"; //$NON-NLS-1$
      if ( logger.isDebugEnabled() ) {
        logger.debug( Messages.getInstance().getString( "ActionHarness.WARN_NO_METHOD_FOR_PROPERTY", //$NON-NLS-1$
            propertyName, beanType ) );
      }
    }
  }
}
