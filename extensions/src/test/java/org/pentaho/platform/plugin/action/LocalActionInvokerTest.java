/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.plugin.action.builtin.ActionSequenceAction;
import org.pentaho.platform.action.ActionInvokeStatus;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LocalActionInvokerTest {
  private LocalActionInvoker defaultActionInvoker;

  @Before
  public void setup() {
    defaultActionInvoker = new LocalActionInvoker();
  }

  @Test
  public void invokeActionLocallyTest() throws Exception {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    IAction iaction = ActionUtil.createActionBean( ActionSequenceAction.class.getName(), null );
    ActionInvokeStatus actionInvokeStatus =
      (ActionInvokeStatus) defaultActionInvoker.invokeAction( iaction, "aUser", testMap );
    Assert.assertFalse( actionInvokeStatus.requiresUpdate() );
  }

  @Test
  public void invokeActionTest() throws Exception {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    IAction iaction = ActionUtil.createActionBean( ActionSequenceAction.class.getName(), null );
    ActionInvokeStatus actionInvokeStatus =
      (ActionInvokeStatus) defaultActionInvoker.invokeAction( iaction, "aUser", testMap );
    Assert.assertFalse( actionInvokeStatus.requiresUpdate() );
  }

  @Test( expected = ActionInvocationException.class )
  public void invokeActionLocallyWithNullsThrowsExceptionTest() throws Exception {
    defaultActionInvoker.invokeAction( null, "aUser", null );
  }


  @Test
  public void getStreamProviderNullTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderNullWithInputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderWithInputAndOutputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, inputFile );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, true );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertEquals( iBackgroundExecutionStreamProvider, repositoryFileStreamProvider );
  }


  @Test
  public void getStreamProviderTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertEquals( repositoryFileStreamProvider, iBackgroundExecutionStreamProvider );
  }
}
