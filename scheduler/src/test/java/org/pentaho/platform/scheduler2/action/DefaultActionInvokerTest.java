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
 * Copyright 2017 Hitachi Vantara.  All rights reserved.
 */
package org.pentaho.platform.scheduler2.action;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@RunWith( PowerMockRunner.class )
public class DefaultActionInvokerTest
{
  @Test
  public void testGetStreamProvider() throws Exception {
    final DefaultActionInvoker ai = new DefaultActionInvoker();
    final Map<String, Serializable> params = new HashMap<>();

    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( "foo", "bar" );
    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, null );
    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, 1 );
    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, true );
    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "streamProviderFoo" );
    Assert.assertNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );

    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, Mockito.mock( IBackgroundExecutionStreamProvider.class ) );
    Assert.assertNotNull( Whitebox.invokeMethod( ai,"getStreamProvider", params ) );
  }
}
