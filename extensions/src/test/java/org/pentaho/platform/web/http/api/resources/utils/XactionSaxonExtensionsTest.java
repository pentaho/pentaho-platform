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

package org.pentaho.platform.web.http.api.resources.utils;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class XactionSaxonExtensionsTest {
  private static final String CURRENT_NAMESPACE = "org.pentaho.platform.web.xsl.messages.Messages";
  private static final String LEGACY_NAMESPACE = "org.pentaho.platform.plugin.action.messages.Messages";

  @Test
  public void testRegistersMessageFunctionsForCurrentAndLegacyNamespaces() {
    RecordingConfiguration configuration = new RecordingConfiguration();

    XactionSaxonExtensions.registerAll( configuration );

    assertEquals( Set.of(
      function( CURRENT_NAMESPACE, "getInstance" ),
      function( CURRENT_NAMESPACE, "getXslString" ),
      function( CURRENT_NAMESPACE, "getString" ),
      function( LEGACY_NAMESPACE, "getInstance" ),
      function( LEGACY_NAMESPACE, "getXslString" ),
      function( LEGACY_NAMESPACE, "getString" ) ), configuration.messageFunctions );
  }

  private static String function( String namespace, String localPart ) {
    return namespace + ':' + localPart;
  }

  private static class RecordingConfiguration extends Configuration {
    private final Set<String> messageFunctions = new HashSet<>();

    @Override
    public void registerExtensionFunction( ExtensionFunctionDefinition function ) {
      StructuredQName name = function.getFunctionQName();
      if ( CURRENT_NAMESPACE.equals( name.getURI() ) || LEGACY_NAMESPACE.equals( name.getURI() ) ) {
        messageFunctions.add( function( name.getURI(), name.getLocalPart() ) );
      }
    }
  }
}