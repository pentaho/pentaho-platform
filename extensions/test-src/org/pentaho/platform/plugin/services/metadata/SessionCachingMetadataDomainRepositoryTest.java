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

package org.pentaho.platform.plugin.services.metadata;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;

public class SessionCachingMetadataDomainRepositoryTest {

  @Test
  public void testStoreAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";
    String annotationsXml = "<annotations/>";

    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 0 ) ).storeAnnotationsXml( domainId, annotationsXml );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 1 ) ).storeAnnotationsXml( domainId, annotationsXml );
  }

  @Test
  public void testLoadAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";

    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 0 ) ).loadAnnotationsXml( domainId );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 1 ) ).loadAnnotationsXml( domainId );
  }
}
