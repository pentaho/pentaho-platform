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


package org.pentaho.platform.scheduler2.quartz;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Collections;

public class QuartzSchedulerTest {


  private static IUnifiedRepository repo;
  private static IUnifiedRepository oldRepo;


  @BeforeClass
  public static void setUp() throws Exception {

    oldRepo = PentahoSystem.get( IUnifiedRepository.class );
    repo = Mockito.mock( IUnifiedRepository.class );
    Mockito.when( repo.getFile( Mockito.anyString() ) ).then( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        final RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
        final String param = (String) invocationOnMock.getArguments()[ 0 ];
        if ( "/home/admin/notexist.ktr".equals( param ) ) {
          return null;
        }
        if ( "/home/admin".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( true );
        }
        if ( "/home/admin/notallowed.ktr".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( false );
          Mockito.when( repositoryFile.isSchedulable() ).thenReturn( false );
        }
        if ( "/home/admin/allowed.ktr".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( false );
          Mockito.when( repositoryFile.isSchedulable() ).thenReturn( true );
        }
        return repositoryFile;
      }
    } );
    PentahoSystem.registerObject( repo, IUnifiedRepository.class );
  }

  @AfterClass
  public static void tearDown() throws Exception {
    repo = null;
    if ( oldRepo != null ) {
      PentahoSystem.registerObject( oldRepo, IUnifiedRepository.class );
    }
  }

  @Test
  public void testValidateParamsNoStreamProviderParam() throws SchedulerException {
    new QuartzScheduler().validateJobParams( Collections.emptyMap() );
  }

  @Test
  public void testValidateParamsNoStringConf() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, 1L ) );
  }

  @Test
  public void testValidateParamsNoInputFile() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "someinputfile" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsFileNotFound() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/notexist.ktr : output = /home/admin/notexist" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsFileIsFolder() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin : output = /home/admin/notexist" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsSchedulingNotAllowed() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/notallowed.ktr : output = /home/admin/notallowed" ) );
  }

  @Test
  public void testValidateParamsSchedulingAllowed() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/allowed.ktr : output = /home/admin/allowed." ) );
  }


}