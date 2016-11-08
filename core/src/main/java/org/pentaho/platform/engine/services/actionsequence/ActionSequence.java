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

package org.pentaho.platform.engine.services.actionsequence;

import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.api.engine.ISequenceDefinition;

import java.util.List;
import java.util.Map;

public class ActionSequence implements IActionSequence {
  private ISequenceDefinition sequenceDefinition;

  private String loopParameter;

  private List actionDefinitions;

  private IConditionalExecution conditionalExecution;

  private boolean loopUsingPeek;

  protected ActionSequence( final String loopParameter, final ISequenceDefinition sequenceDefinition,
      final List actionDefinitions, boolean loopUsingPeek ) {
    this.loopParameter = loopParameter;
    this.sequenceDefinition = sequenceDefinition;
    this.actionDefinitions = actionDefinitions;
    this.loopUsingPeek = loopUsingPeek;
  }

  public List getActionDefinitions() {
    return actionDefinitions;
  }

  public String getLoopParameter() {
    return loopParameter;
  }

  public IConditionalExecution getConditionalExecution() {
    return conditionalExecution;
  }

  public void setConditionalExecution( final IConditionalExecution value ) {
    this.conditionalExecution = value;
  }

  public boolean hasLoop() {
    return ( loopParameter != null );
  }

  public String getResultType() {
    return sequenceDefinition.getResultType();
  }

  public String getSequenceName() {
    return ( sequenceDefinition.getSequenceName() );
  }

  public String getAuthor() {
    return ( sequenceDefinition.getAuthor() );
  }

  public String getDescription() {
    return ( sequenceDefinition.getDescription() );
  }

  public String getHelp() {
    return ( sequenceDefinition.getHelp() );
  }

  public Map getInputDefinitions() {
    return ( sequenceDefinition.getInputDefinitions() );
  }

  public Map getInputDefinitionsForParameterProvider( final String parameterProviderName ) {
    return ( sequenceDefinition.getInputDefinitionsForParameterProvider( parameterProviderName ) );
  }

  public Map getOutputDefinitions() {
    return ( sequenceDefinition.getOutputDefinitions() );
  }

  public Map getResourceDefinitions() {
    return ( sequenceDefinition.getResourceDefinitions() );
  }

  public String getSolutionName() {
    return ( sequenceDefinition.getSolutionName() );
  }

  public String getSolutionPath() {
    return ( sequenceDefinition.getSolutionPath() );
  }

  public int getLoggingLevel() {
    return ( sequenceDefinition.getLoggingLevel() );
  }

  public String getTitle() {
    return sequenceDefinition.getTitle();
  }

  public String getIcon() {
    return sequenceDefinition.getIcon();
  }

  public List getActionDefinitionsAndSequences() {
    return getActionDefinitions();
  }

  public boolean getLoopUsingPeek() {
    return loopUsingPeek;
  }

}
