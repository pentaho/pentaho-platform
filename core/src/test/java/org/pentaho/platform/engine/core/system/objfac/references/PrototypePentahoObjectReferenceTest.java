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

package org.pentaho.platform.engine.core.system.objfac.references;

import org.junit.Test;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.util.UUID;

import static org.junit.Assert.assertNotSame;

/**
 * Created by nbaker on 4/16/14.
 */
public class PrototypePentahoObjectReferenceTest {
  @Test
  public void testReference() throws Exception {
    PrototypePentahoObjectReference<UUID> sessionRef =
      new PrototypePentahoObjectReference.Builder<UUID>( UUID.class ).creator(
        new IObjectCreator<UUID>() {
          @Override public UUID create( IPentahoSession session ) {
            return UUID.randomUUID();
          }
        }
      ).build();

    IPentahoSession s1 = new StandaloneSession( "joe" );
    IPentahoSession s2 = new StandaloneSession( "admin" );

    PentahoSessionHolder.setSession( s1 );
    UUID s1Uuid = sessionRef.getObject();

    PentahoSessionHolder.setSession( s2 );
    UUID s2Uuid = sessionRef.getObject();
    assertNotSame( s1Uuid, s2Uuid );

    PentahoSessionHolder.setSession( s1 );
    UUID s1UuidAgain = sessionRef.getObject();
    assertNotSame( s1Uuid, s1UuidAgain );

  }
}
