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


package org.pentaho.platform.plugin.action.messages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MessagesTest {

  private Messages messages;
  private String NL;

  @Before
  public void setup() {

    messages = Messages.getInstance();
    NL = System.getProperty( "line.separator" );
  }

  @Test
  public void tesGetRunningInBackgroundLocally() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals(
      "Running action \"foo\" in background locally: Map = " + NL + "{" + NL + "    key1 = val1 java.lang"
        + ".String" + NL + "    " + "key2 = val2 java.lang.String" + NL + "} java.util.HashMap" + NL, messages
        .getRunningInBackgroundLocally( "foo", params ) );
  }

  @Test
  public void tesGetRunningInBackgroundRemotely() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "Running action \"foo\" in background remotely: Map = " + NL + "{" + NL + "    key1 = val1 "
      + "java.lang.String" + NL + "    key2 = val2 java.lang.String" + NL + "} java.util.HashMap" + NL, messages
      .getRunningInBackgroundRemotely( "foo", params ) );
  }

  @Test
  public void testGetPostingToResource() {

    final String params = "{\"key1\" : \"var1\", \"key2\" : \"var2\", \"key3\" : \"var3\"}";
    Assert.assertEquals( "POSTing to resource \"foo\": " + params, messages.getPostingToResource( "foo", params ) );
  }

  @Test
  public void testGetResourceResponded() {
    Assert.assertEquals( "Resource \"foo\" responded with \"200\"", messages.getResourceResponded( "foo", 200 ) );
  }

  @Test
  public void testGetCantInvokeActionWithNullMap() {
    Assert.assertEquals( "ActionInvoker.ERROR_0006 - Cannot invoke action when the map is null",
      messages.getCantInvokeActionWithNullMap() );
  }

  @Test
  public void testGetCantInvokeNullAction() {
    Assert
      .assertEquals( "ActionInvoker.ERROR_0005 - Action is null, cannot invoke", messages.getCantInvokeNullAction() );
  }

  @Test
  public void testGetRemoteEndpointFailure() {
    final String url = "http:///foo.com/myEndpoint";
    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "ActionInvoker.ERROR_0007 - Unable to execute the remote endpoint \"" + url+ "\": Map = " +
        NL + "{" + NL + "    key1 = val1 java.lang.String" + NL + "    key2 = val2 java.lang.String" + NL + "} java"
        + ".util.HashMap" + NL, messages.getRemoteEndpointFailure( url, params ) );
  }

  @Test
  public void testGetMapNullCantReturnSp() {
    Assert.assertEquals( "ActionInvoker.ERROR_0008 - Map is null, cannot return stream provider", messages
      .getMapNullCantReturnSp() );
  }

  @Test
  public void testGetMissingParamsCantReturnSp() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "Parameters required to create the stream provider (foo) are not "
      + "available in the map: Map = " + NL + "{" + NL + "    key1 = val1 java.lang.String" + NL + "    key2 = val2 "
      + "java.lang.String" + NL + "} java.util.HashMap" + NL, messages.getMissingParamsCantReturnSp( "foo", params ) );
  }

  @Test
  public void testGetActionFailedToExecute() {
    Assert.assertEquals( "ActionInvoker.ERROR_0004 - Action \"foo\" failed to execute", messages
      .getActionFailedToExecute( "foo" ) );
  }

  @Test
  public void testGetSkipRemovingOutputFile() {
    Assert.assertEquals( "File written by XActions must be cleaned up by external means: foo", messages
      .getSkipRemovingOutputFile( "foo" ) );
  }

  @Test
  public void testGetCannotGetRepoFile() {
    Assert.assertEquals( "ActionInvoker.ERROR_0010 - Cannot get repository file \"foo\": foe", messages
      .getCannotGetRepoFile( "foo", "foe" ) );
  }

  @Test
  public void testGetCouldNotConvertContentToMap() {
    Assert.assertEquals( "ActionInvoker.ERROR_0011 - Could not convert content to map: foo", messages
      .getCouldNotConvertContentToMap( "foo" ) );
  }

  @Test
  public void testGetCouldNotInvokeActionLocally() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );

    Assert.assertEquals( "ActionInvoker.ERROR_0012 - Could not invoke action \"foo\" locally: Map = " + NL + "{" + NL
      + "    key1 = val1 java.lang.String" + NL + "    key2 = val2 java.lang.String" + NL + "} java.util.HashMap" +
      NL, messages.getCouldNotInvokeActionLocally( "foo", params ) );
  }

  @Test
  public void testGetRunningInBgLocallySuccess() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );

    Assert.assertEquals( "Local action \"foo\" ran in background successfully: Map = " + NL + "{" + NL + "    key1 = "
        + "val1 java.lang.String" + NL + "    key2 = val2 java.lang.String" + NL + "}" + " java.util.HashMap" + NL,
      messages.getRunningInBgLocallySuccess( "foo", params ) );
  }

  @Test
  public void testGetNoEeLicense() {
    Assert.assertEquals( "ActionInvoker.ERROR_0001 - Cannot invoke actions remotely without an EE license",
      messages.getNoEeLicense() );
  }

  @Test
  public void testGetUnexpectedStatusCode() {
    Assert.assertEquals( "ActionInvoker.ERROR_0013 - Received an unexpected status code: " + 100,
      messages.getUnexpectedStatusCode( 100 ) );
  }
}
