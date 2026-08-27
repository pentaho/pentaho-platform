/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.plugin.action.xml.xquery;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.XQueryAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XQueryBaseComponentTest {

  @Test
  public void testPrepareQueryUsesFileUriForXmlResource() {
    XQueryAction action = mock( XQueryAction.class );
    IActionInput sourceXml = mock( IActionInput.class );
    IActionResource xmlResource = mock( IActionResource.class );
    IActionSequenceResource resource = mock( IActionSequenceResource.class );
    IActionOutput preparedStatement = mock( IActionOutput.class );

    when( action.getSourceXml() ).thenReturn( sourceXml );
    when( sourceXml.getStringValue() ).thenReturn( null );
    when( action.getXmlDocument() ).thenReturn( xmlResource );
    when( xmlResource.getName() ).thenReturn( "test.xml" );
    when( action.getOutputPreparedStatement() ).thenReturn( preparedStatement );
    when( resource.getSourceType() ).thenReturn( IActionSequenceResource.SOLUTION_FILE_RESOURCE );
    when( resource.getInputStream( RepositoryFilePermission.READ ) ).thenAnswer( invocation -> xml() );

    TestXQueryBaseComponent component = new TestXQueryBaseComponent( resource );
    component.setActionDefinition( action );

    assertTrue( component.runQuery( mock( IPentahoConnection.class ), "/result-set" ) );
    assertTrue( component.preparedQuery.startsWith( "doc(\"file:" ) );
  }

  private InputStream xml() {
    return new ByteArrayInputStream( "<result-set/>".getBytes( StandardCharsets.UTF_8 ) );
  }

  private static class TestXQueryBaseComponent extends XQueryBaseComponent {
    private final IActionSequenceResource resource;

    private TestXQueryBaseComponent( IActionSequenceResource resource ) {
      this.resource = resource;
    }

    @Override
    public boolean validateSystemSettings() {
      return true;
    }

    @Override
    public Log getLogger() {
      return mock( Log.class );
    }

    @Override
    protected IActionSequenceResource getResource( String resourceName ) {
      return resource;
    }

    @Override
    protected boolean retrieveColumnTypes() {
      return false;
    }

    @Override
    protected boolean prepareFinalQuery( String rawQuery, String[] columnTypes ) {
      preparedQuery = rawQuery;
      return true;
    }
  }
}