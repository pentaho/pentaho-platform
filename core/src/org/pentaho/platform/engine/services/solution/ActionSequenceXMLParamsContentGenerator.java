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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ActionSequenceXMLParamsContentGenerator extends
		ActionSequenceContentGenerator implements ICreateFeedbackParameterCallback {

  private static final long serialVersionUID = -4884652274570628034L;

  @Override
	protected void setupListeners( ISolutionEngine solutionEngine ) {
	    // setup any listeners
	    ICreateFeedbackParameterCallback feedbackParameterCallback = (ICreateFeedbackParameterCallback) getCallback( ICreateFeedbackParameterCallback.class );
	    if( feedbackParameterCallback != null ) {
		    solutionEngine.setCreateFeedbackParameterCallback(feedbackParameterCallback);
	    }
	}

	public void createFeedbackParameter( IRuntimeContext context, String fieldName, String displayName, String hint, Object defaultValues, List values, Map dispNames,
            String displayStyle, boolean optional, boolean visible) {
          if( PentahoSystem.debug ) System.out.println("createFeedbackParameterCallback::fieldName = " + fieldName); //$NON-NLS-1$
          if( PentahoSystem.debug ) System.out.println("createFeedbackParameterCallback::displayStyle = " + displayStyle); //$NON-NLS-1$
          if (values != null) {
            for (Object value : values) {
              if( PentahoSystem.debug ) System.out.println("createFeedbackParameterCallback::values[i] = " + value); //$NON-NLS-1$
            }
          }
          if (defaultValues != null) {
            if (defaultValues instanceof List) {
              for (Object value : (List) defaultValues) {
                if( PentahoSystem.debug ) System.out.println("createFeedbackParameterCallback::defaultValues[i] = " + value); //$NON-NLS-1$
              }
            } else {
              if( PentahoSystem.debug ) System.out.println("createFeedbackParameterCallback::defaultValue = " + defaultValues); //$NON-NLS-1$
            }
          }
	}
	
	public void feedbackParametersDone() {
		
	}
	
}
