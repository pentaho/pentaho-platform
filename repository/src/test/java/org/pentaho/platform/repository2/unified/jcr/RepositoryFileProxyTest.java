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

package org.pentaho.platform.repository2.unified.jcr;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.springframework.extensions.jcr.JcrTemplate;

public class RepositoryFileProxyTest {

  private RepositoryFileProxy repoFileProxy;
  private RepositoryFileProxy repoFileProxySpy;
  private Node nodeMock = mock( Node.class );
  private Session sessionMock = mock( Session.class );
  private JcrTemplate templateMock = mock( JcrTemplate.class );
  private IPentahoLocale pentahoLocale = new PentahoLocale();
  private Map<String, Serializable> metadata = new HashMap<String, Serializable>();

  @Before
  public void setUp() throws RepositoryException {
    when( nodeMock.getSession() ).thenReturn( sessionMock );
    // Live session
    when( sessionMock.isLive() ).thenReturn( true );
    repoFileProxy = new RepositoryFileProxy( nodeMock, templateMock, pentahoLocale );
    repoFileProxySpy = spy( repoFileProxy );
    // test metadata
    doReturn( metadata ).when( repoFileProxySpy ).getMetadata();

  }

  // If no "is schedulable" metadata property --> it is schedulable by default
  // Files added added into repository in v7.0 covered with this case
  // Please see PDI-16326
  @Test
  public void testDefaultLogic_IfNoIsSchedulableInMetadata() {
    assertTrue( repoFileProxySpy.isSchedulable() );
  }

  // Metadata contains "is schedulable" property: "_PERM_SCHEDULABLE"=true
  // Files added added into repository starting from v7.1 covered with this case
  @Test
  public void testSchedulable_IfIsSchedulableTrueInMetadata() {
    metadata.put( RepositoryFile.SCHEDULABLE_KEY, "true" );
    assertTrue( repoFileProxySpy.isSchedulable() );
  }

  // Metadata contains "is schedulable" property: "_PERM_SCHEDULABLE"=false
  // Files with unchecked "schedulable" checkbox covered with this case
  @Test
  public void testNotSchedulable_IfIsSchedulableFalseInMetadata() {
    metadata.put( RepositoryFile.SCHEDULABLE_KEY, "false" );
    assertFalse( repoFileProxySpy.isSchedulable() );
  }

}
