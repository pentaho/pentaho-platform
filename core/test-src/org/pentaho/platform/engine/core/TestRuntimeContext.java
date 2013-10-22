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

package org.pentaho.platform.engine.core;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoBase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( { "all" } )
public class TestRuntimeContext extends PentahoBase implements IRuntimeContext {

  public int status;

  public Map<String, IActionParameter> outputParameters;

  @Override
  public Log getLogger() {
    // TODO Auto-generated method stub
    return null;
  }

  public void addInputParameter( String name, IActionParameter param ) {
    // TODO Auto-generated method stub

  }

  public void addTempParameter( String name, IActionParameter output ) {
    // TODO Auto-generated method stub

  }

  public String applyInputsToFormat( String format ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String applyInputsToFormat( String format, IParameterResolver resolver ) {
    // TODO Auto-generated method stub
    return null;
  }

  public void audit( String messageType, String message, String value, long duration ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( IActionParameter actionParam ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( ISelectionMapper selMap, String fieldName, Object defaultValues ) {
    // TODO Auto-generated method stub

  }

  public void
  createFeedbackParameter( ISelectionMapper selMap, String fieldName, Object defaultValues, boolean optional ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValue,
      boolean visible ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValue,
      boolean visible, boolean optional ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValues,
      List values, Map dispNames, String displayStyle ) {
    // TODO Auto-generated method stub

  }

  public void createFeedbackParameter( String fieldName, String displayName, String hint, Object defaultValues,
      List values, Map dispNames, String displayStyle, boolean optional ) {
    // TODO Auto-generated method stub

  }

  public String createNewInstance( boolean persisted ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String createNewInstance( boolean persisted, Map parameters ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String createNewInstance( boolean persisted, Map parameters, boolean forceImmediateWrite ) {
    // TODO Auto-generated method stub
    return null;
  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public void dispose( List exceptParameters ) {
    // TODO Auto-generated method stub

  }

  public void executeSequence( IActionCompleteListener listener, IExecutionListener execListener, boolean async ) {
    // TODO Auto-generated method stub
  }

  public boolean feedbackAllowed() {
    // TODO Auto-generated method stub
    return false;
  }

  public void forceSaveRuntimeData() {
    // TODO Auto-generated method stub

  }

  public IActionSequence getActionSequence() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getActionTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getCurrentComponentName() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoStreamSource getDataSource( String parameterName ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getFeedbackContentItem() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getHandle() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set getInputNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public IActionParameter getInputParameter( String name ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getInputParameterStringValue( String name ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getInputParameterValue( String name ) {
    // TODO Auto-generated method stub
    return null;
  }

  public InputStream getInputStream( String parameterName ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getInstanceId() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getOutputContentItem( String mimeType ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getOutputContentItem( String outputName, String mimeType ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getOutputItem( String outputName, String mimeType, String extension ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Set getOutputNames() {
    return outputParameters.keySet();
  }

  public IActionParameter getOutputParameter( String name ) {
    return outputParameters.get( name );
  }

  public int getOutputPreference() {
    // TODO Auto-generated method stub
    return 0;
  }

  public IParameterManager getParameterManager() {
    // TODO Auto-generated method stub
    return null;
  }

  public Map getParameterProviders() {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getResourceAsDocument( IActionSequenceResource actionParameter ) throws IOException,
    DocumentException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getResourceAsString( IActionSequenceResource actionParameter ) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoStreamSource getResourceDataSource( IActionSequenceResource actionResource )
    throws FileNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  public IActionSequenceResource getResourceDefintion( String name ) {
    // TODO Auto-generated method stub
    return null;
  }

  public InputStream getResourceInputStream( IActionSequenceResource actionResource ) throws FileNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  public Set getResourceNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoSession getSession() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionPath() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getStatus() {
    return status;
  }

  public IPentahoUrlFactory getUrlFactory() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isPromptPending() {
    // TODO Auto-generated method stub
    return false;
  }

  public void promptNeeded() {
    // TODO Auto-generated method stub

  }

  public void promptNow() {
    // TODO Auto-generated method stub

  }

  public void sendFeedbackForm() {
    // TODO Auto-generated method stub

  }

  public void setActionSequence( IActionSequence actionSequence ) {
    // TODO Auto-generated method stub

  }

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback callback ) {
    // TODO Auto-generated method stub

  }

  public void setOutputHandler( IOutputHandler outputHandler ) {
    // TODO Auto-generated method stub

  }

  public void setOutputValue( String name, Object output ) {
    // TODO Auto-generated method stub

  }

  public void setParameterTarget( String target ) {
    // TODO Auto-generated method stub

  }

  public void setParameterXsl( String xsl ) {
    // TODO Auto-generated method stub

  }

  public void setPromptStatus( int status ) {
    // TODO Auto-generated method stub

  }

  public void validateSequence( String sequenceName, IExecutionListener execListener ) {
    // TODO Auto-generated method stub
  }

  public String getActionName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProcessId() {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getContentOutputItem( String outputName, String mimeType ) {
    // TODO Auto-generated method stub
    return null;
  }

}
