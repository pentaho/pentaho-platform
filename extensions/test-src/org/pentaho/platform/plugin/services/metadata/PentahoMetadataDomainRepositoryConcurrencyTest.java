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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class PentahoMetadataDomainRepositoryConcurrencyTest {

  private static final String METADATA_DIR_ID = "metadataDirId";

  private IUnifiedRepository unifiedRepository;
  private IAclNodeHelper aclNodeHelper;
  private PentahoMetadataDomainRepository domainRepository;

  @Before
  public void setUp() throws Exception {
    unifiedRepository = mock( IUnifiedRepository.class );

    RepositoryFile metadataDir = new RepositoryFile.Builder( METADATA_DIR_ID, "metadataDir" ).build();
    when( unifiedRepository.getFile( PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath() ) )
      .thenReturn( metadataDir );


    aclNodeHelper = mock( IAclNodeHelper.class );

    domainRepository = new PentahoMetadataDomainRepository( unifiedRepository );
    domainRepository = spy( domainRepository );
    doReturn( aclNodeHelper ).when( domainRepository ).getAclHelper();
  }

  @SuppressWarnings( "unchecked" )
  @After
  public void cleanUp() throws Exception {
    Map<IUnifiedRepository, ?> metaMapStore =
      (Map<IUnifiedRepository, ?>) FieldUtils
        .readStaticField( PentahoMetadataDomainRepository.class, "metaMapStore", true );
    if ( metaMapStore != null ) {
      metaMapStore.remove( unifiedRepository );
    }

    unifiedRepository = null;
    aclNodeHelper = null;
    domainRepository = null;
  }


  @SuppressWarnings( "unchecked" )
  @Test
  public void getMetadataRepositoryFile_TenReaders() throws Exception {
    final int amountOfReaders = 10;
    final int cycles = 30;
    final int amountOfDomains = amountOfReaders - 1;

    List<RepositoryFile> files = createRepositoryFiles( amountOfDomains );

    when( aclNodeHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );

    List<FilesLookuper> readers = new ArrayList<FilesLookuper>( amountOfReaders );
    for ( int i = 0; i < amountOfDomains; i++ ) {
      readers.add( new FilesLookuper( domainRepository, files.get( i ).getId().toString(), cycles, true ) );
    }
    readers.add( new FilesLookuper( domainRepository, "non-existing domain", cycles, false ) );
    // randomizing the order of readers
    Collections.shuffle( readers );

    runTest( readers );
  }


  @SuppressWarnings( "unchecked" )
  @Test
  public void getDomainIds_TenReaders() throws Exception {
    final int amountOfReaders = 10;
    final int cycles = 30;

    createRepositoryFiles( amountOfReaders );
    Set<String> ids = new HashSet<String>( amountOfReaders );
    for ( int i = 0; i < amountOfReaders; i++ ) {
      ids.add( generateDomainId( i ) );
    }
    ids = Collections.unmodifiableSet( ids );

    when( aclNodeHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );

    List<IdsLookuper> readers = new ArrayList<IdsLookuper>( amountOfReaders );
    for ( int i = 0; i < amountOfReaders; i++ ) {
      readers.add( new IdsLookuper( domainRepository, ids, cycles ) );
    }

    runTest( readers );
  }


  private void runTest( final List<? extends Callable<String>> readers ) throws Exception {
    List<String> errors = new ArrayList<String>();
    ExecutorService executorService = Executors.newFixedThreadPool( readers.size() );
    try {
      CompletionService<String> completionService = new ExecutorCompletionService<String>( executorService );
      for ( Callable<String> reader : readers ) {
        completionService.submit( reader );
      }

      for ( int i = 0; i < readers.size(); i++ ) {
        String result = completionService.take().get();
        if ( result != null ) {
          errors.add( result );
        }
      }
    } finally {
      executorService.shutdown();
    }

    if ( !errors.isEmpty() ) {
      StringBuilder builder = new StringBuilder();
      builder.append( "The following errors occurred: \n" );
      for ( String error : errors ) {
        builder.append( error ).append( '\n' );
      }
      fail( builder.toString() );
    }
  }


  private List<RepositoryFile> createRepositoryFiles( int amountOfDomains ) {
    final List<RepositoryFile> files = new ArrayList<RepositoryFile>( amountOfDomains );
    for ( int i = 0; i < amountOfDomains; i++ ) {
      RepositoryFile file = createRepositoryFile( generateDomainId( i ) );
      files.add( file );

      Serializable id = file.getId();
      Map<String, Serializable> metadata = new HashMap<String, Serializable>();
      metadata.put( "file-type", "domain" );
      metadata.put( "domain-id", id );
      when( unifiedRepository.getFileMetadata( id ) ).thenReturn( metadata );
    }

    Answer<List<RepositoryFile>> answer = new Answer<List<RepositoryFile>>() {
      @Override
      public List<RepositoryFile> answer( InvocationOnMock invocation ) throws Throwable {
        // simulates delays of operations with JCR
        Thread.sleep( new Random().nextInt( 300 ) );
        return files;
      }
    };
    when( unifiedRepository.getChildren( any( RepositoryRequest.class ) ) ).thenAnswer( answer );
    when( unifiedRepository.getChildren( any( Serializable.class ), anyString() ) ).thenAnswer( answer );

    return files;
  }

  private static String generateDomainId( int index ) {
    return "domain_" + index;
  }

  private static RepositoryFile createRepositoryFile( String id ) {
    return new RepositoryFile.Builder( id, "id" ).build();
  }


  private static class FilesLookuper implements Callable<String> {
    private final PentahoMetadataDomainRepository domainRepository;
    private final String domainId;
    private final int cycles;
    private final boolean expectNotNull;

    public FilesLookuper( PentahoMetadataDomainRepository domainRepository, String domainId, int cycles,
                          boolean expectNotNull ) {
      this.domainRepository = domainRepository;
      this.domainId = domainId;
      this.cycles = cycles;
      this.expectNotNull = expectNotNull;
    }

    @Override
    public String call() throws Exception {
      for ( int i = 0; i < cycles; i++ ) {
        RepositoryFile file = domainRepository.getMetadataRepositoryFile( domainId );
        if ( expectNotNull ) {
          if ( file == null ) {
            return String.format( "Expected to obtain existing domain: [%s]", domainId );
          }
        } else {
          if ( file != null ) {
            return String.format( "Expected to obtain null for non-existing domain: [%s]", domainId );
          }
        }
      }
      return null;
    }
  }

  private static class IdsLookuper implements Callable<String> {
    private final PentahoMetadataDomainRepository domainRepository;
    private final Set<String> expectedIds;
    private final int cycles;

    public IdsLookuper( PentahoMetadataDomainRepository domainRepository, Set<String> expectedIds, int cycles ) {
      this.domainRepository = domainRepository;
      this.expectedIds = expectedIds;
      this.cycles = cycles;
    }

    @Override
    public String call() throws Exception {
      for ( int i = 0; i < cycles; i++ ) {
        Set<String> domainIds = domainRepository.getDomainIds();
        if ( domainIds.size() != expectedIds.size() ) {
          return error( domainIds );
        } else {
          Set<String> tmp = new HashSet<String>( expectedIds );
          tmp.removeAll( domainIds );
          if ( !tmp.isEmpty() ) {
            return error( domainIds );
          }
        }
      }
      return null;
    }

    private String error( Set<String> domainIds ) {
      return String.format( "Expected to obtain [%s], but got [%s]", expectedIds, domainIds );
    }
  }
}
