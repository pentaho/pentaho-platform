/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system;


import org.junit.Test;
import org.junit.Assert;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

import java.util.ArrayList;
import java.util.List;


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

public class PentahoSystemShutdownOrderTest {
  @Test
  public void shutdownSystemListenerOrderTest() {
    TestShutdownOrderListener listener1 = new TestShutdownOrderListener();
    TestShutdownOrderListener listener2 = new TestShutdownOrderListener();
    TestShutdownOrderListener listener3 = new TestShutdownOrderListener();
    List<IPentahoSystemListener> listenerList = new ArrayList<>(  );
    listenerList.add( listener1 );
    listenerList.add( listener2 );
    listenerList.add( listener3 );
    PentahoSystem.setSystemListeners( listenerList );
    PentahoSystem.shutdown();

    Assert.assertEquals( listener1.getOrder(), 2  );
    Assert.assertEquals( listener2.getOrder(), 1 );
    Assert.assertEquals( listener3.getOrder(), 0 );

  }
}
