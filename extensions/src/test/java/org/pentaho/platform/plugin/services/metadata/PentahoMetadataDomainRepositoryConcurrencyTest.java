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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.EmptyUnifiedRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class PentahoMetadataDomainRepositoryConcurrencyTest {

  private static final String METADATA_DIR_ID = "metadataDirId";

  private DomainsStubRepository repository;
  private IAclNodeHelper aclNodeHelper;
  private PentahoMetadataDomainRepository domainRepository;

  @SuppressWarnings( "unchecked" )
  @Before
  public void setUp() throws Exception {
    repository = new DomainsStubRepository();
    repository = spy( repository );
    RepositoryFile metadataDir = new RepositoryFile.Builder( METADATA_DIR_ID, "metadataDir" ).folder( true ).build();
    doReturn( metadataDir ).when( repository ).getFile( PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath() );

    aclNodeHelper = mock( IAclNodeHelper.class );
    when( aclNodeHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );

    domainRepository = new PentahoMetadataDomainRepository( repository );
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
      metaMapStore.remove( repository );
    }

    repository = null;
    aclNodeHelper = null;
    domainRepository = null;
  }


  @Test
  public void getMetadataRepositoryFile_TenReaders() throws Exception {
    final int amountOfReaders = 10;
    final int cycles = 30;
    final int amountOfDomains = amountOfReaders - 1;

    createRepositoryFiles( amountOfDomains );

    List<FilesLookuper> readers = new ArrayList<FilesLookuper>( amountOfReaders );
    for ( int i = 0; i < amountOfDomains; i++ ) {
      readers.add( new FilesLookuper( domainRepository, generateDomainId( i ), cycles, true ) );
    }
    readers.add( new FilesLookuper( domainRepository, "non-existing domain", cycles, false ) );
    // randomizing the order of readers
    Collections.shuffle( readers );

    runTest( readers );
  }


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

    List<IdsLookuper> readers = new ArrayList<IdsLookuper>( amountOfReaders );
    for ( int i = 0; i < amountOfReaders; i++ ) {
      readers.add( new IdsLookuper( domainRepository, ids, cycles ) );
    }

    runTest( readers );
  }


  @Test
  public void addDomain_getDomain_Simultaneously() throws Exception {
    final int readersAmount = 10;
    final int cycles = 30;
    final int addersAmount = 20;

    createRepositoryFiles( readersAmount );
    doAnswer( new Answer() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        String domainId = (String) invocation.getArguments()[ 0 ];
        repository.createFile( null, createRepositoryFile( domainId ), null, null );
        repository.setFileMetadata( domainId, generateMetadataFor( domainId ) );
        return null;
      }
    } ).when( domainRepository ).createUniqueFile( anyString(), anyString(), any( SimpleRepositoryFileData.class ) );

    domainRepository.setXmiParser( mockXmiParser() );

    List<Callable<String>> actors = new ArrayList<Callable<String>>( readersAmount + addersAmount * 2 );
    for ( int i = 0; i < readersAmount; i++ ) {
      actors.add( new FilesLookuper( domainRepository, generateDomainId( i ), cycles, true ) );
    }
    for ( int i = 0; i < addersAmount; i++ ) {
      int index = i + readersAmount;
      String domainId = generateDomainId( index );
      AtomicBoolean condition = new AtomicBoolean( true );
      AtomicBoolean addedFlag = new AtomicBoolean( false );
      actors.add( new DomainAdder( domainRepository, domainId, condition, addedFlag ) );
      actors.add( new DomainLookuper( domainRepository, domainId, condition, addedFlag ) );
    }

    Collections.shuffle( actors );
    runTest( actors );
  }

  private XmiParser mockXmiParser() throws Exception {
    XmiParser parser = mock( XmiParser.class );
    when( parser.generateXmi( any( Domain.class ) ) ).thenReturn( "" );
    when( parser.parseXmi( any( InputStream.class ) ) ).thenAnswer( new Answer<Domain>() {
      @Override public Domain answer( InvocationOnMock invocation ) throws Throwable {
        return new Domain();
      }
    } );
    return parser;
  }


  private void runTest( final List<? extends Callable<String>> actors ) throws Exception {
    List<String> errors = new ArrayList<String>();
    ExecutorService executorService = Executors.newFixedThreadPool( actors.size() );
    try {
      CompletionService<String> completionService = new ExecutorCompletionService<String>( executorService );
      for ( Callable<String> reader : actors ) {
        completionService.submit( reader );
      }

      for ( int i = 0; i < actors.size(); i++ ) {
        Future<String> take = completionService.take();
        String result;
        try {
          result = take.get();
        } catch ( ExecutionException e ) {
          result = "Execution exception: " + e.getMessage();
        }
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


  private void createRepositoryFiles( int amountOfDomains ) {
    for ( int i = 0; i < amountOfDomains; i++ ) {
      RepositoryFile file = createRepositoryFile( generateDomainId( i ) );
      repository.createFile( null, file, null, null );

      Map<String, Serializable> metadata = generateMetadataFor( file.getId() );
      repository.setFileMetadata( file.getId(), metadata );
    }
  }

  private Map<String, Serializable> generateMetadataFor( Serializable id ) {
    Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    metadata.put( "file-type", "domain" );
    metadata.put( "domain-id", id );
    return metadata;
  }

  private static String generateDomainId( int index ) {
    return "domain_" + index;
  }

  private static RepositoryFile createRepositoryFile( String id ) {
    return new RepositoryFile.Builder( id, id ).build();
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


  private static class DomainLookuper implements Callable<String> {
    private final PentahoMetadataDomainRepository domainRepository;
    private final String domainId;
    private final AtomicBoolean continueCondition;
    private final AtomicBoolean addedFlag;

    public DomainLookuper( PentahoMetadataDomainRepository domainRepository, String domainId,
                           AtomicBoolean continueCondition, AtomicBoolean addedFlag ) {
      this.domainRepository = domainRepository;
      this.domainId = domainId;
      this.continueCondition = continueCondition;
      this.addedFlag = addedFlag;
    }

    @Override
    public String call() throws Exception {
      while ( continueCondition.get() ) {
        if ( addedFlag.get() ) {
          Domain domain = domainRepository.getDomain( domainId );
          if ( domain == null ) {
            return String.format( "Expected to obtain [%s], but got null", domainId );
          }
        } else {
          Domain domain = domainRepository.getDomain( domainId );
          if ( domain != null ) {
            // the reason we are doing such tricky hack is that the flag is not set inside
            // a transaction with storing domain, in other words, it is possible that domain has been already stored,
            // but the flag is not yet set
            // it is a drawback of testing approach and it is hardly can occur in real application
            Thread.sleep( 200 );
            if ( !addedFlag.get() ) {
              return String.format( "Expected not to find domain [%s], but got it", domainId );
            }
          }
        }
      }
      return null;
    }
  }

  private static class DomainAdder implements Callable<String> {
    private final PentahoMetadataDomainRepository domainRepository;
    private final String domainId;
    private final AtomicBoolean continueCondition;
    private final AtomicBoolean addedFlag;

    public DomainAdder( PentahoMetadataDomainRepository domainRepository, String domainId,
                        AtomicBoolean continueCondition, AtomicBoolean addedFlag ) {
      this.domainRepository = domainRepository;
      this.domainId = domainId;
      this.continueCondition = continueCondition;
      this.addedFlag = addedFlag;
    }

    @Override
    public String call() throws Exception {
      try {
        // sleep for a while to give lookupers a possibility to get nulls
        Thread.sleep( 2000 + new Random().nextInt( 500 ) );
        Domain domain = new Domain();
        domain.setId( domainId );
        domainRepository.storeDomain( domain, false );
        addedFlag.set( true );
      } finally {
        continueCondition.set( false );
      }
      return null;
    }
  }


  private static class DomainsStubRepository extends EmptyUnifiedRepository {
    private final List<RepositoryFile> files;
    private final Map<Serializable, Map<String, Serializable>> metadatas;

    public DomainsStubRepository() {
      this.files = new ArrayList<RepositoryFile>();
      this.metadatas = new HashMap<Serializable, Map<String, Serializable>>();
    }

    @Override
    public List<RepositoryFile> getChildren( Serializable folderId ) {
      return getChildren( folderId, null );
    }

    @Override
    public List<RepositoryFile> getChildren( Serializable folderId, String filter ) {
      return getChildren( folderId, null, null );
    }

    @Override
    public List<RepositoryFile> getChildren( Serializable folderId, String filter,
                                             Boolean showHiddenFiles ) {
      return getChildren( (RepositoryRequest) null );
    }

    @Override
    public synchronized List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
      emulateJcrDelay();
      return new ArrayList<RepositoryFile>( files );
    }


    @Override
    public RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file,
                                      IRepositoryFileData data, String versionMessage ) {
      return this.createFile( parentFolderId, file, data, null, versionMessage );
    }

    @Override
    public synchronized RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file,
                                                   IRepositoryFileData data, RepositoryFileAcl acl,
                                                   String versionMessage ) {
      emulateJcrDelay();
      files.add( file );
      return file;
    }

    @Override
    public synchronized Map<String, Serializable> getFileMetadata( Serializable fileId ) {
      return metadatas.get( fileId );
    }

    @Override
    public synchronized void setFileMetadata( Serializable fileId, Map<String, Serializable> metadataMap ) {
      metadatas.put( fileId, metadataMap );
    }


    @Override
    public synchronized <T extends IRepositoryFileData> T getDataForRead( Serializable fileId, Class<T> dataClass ) {
      return (T) new SimpleRepositoryFileData( new ByteArrayInputStream( new byte[ 0 ] ), "utf-8", null );
    }

    private void emulateJcrDelay() {
      try {
        Thread.sleep( new Random().nextInt( 10 ) );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
