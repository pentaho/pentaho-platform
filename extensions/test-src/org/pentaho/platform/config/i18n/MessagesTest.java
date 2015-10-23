package org.pentaho.platform.config.i18n;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class MessagesTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetMessages() throws Exception {
    assertEquals( "Wrong message returned", "Normal", Messages.getString( "XmlSerializer.stateNormal" ) );

    assertEquals( "Wrong message returned", "Unknown Host: localhost",
      Messages.getString( "StopJettyServer.ERROR_0003_UNKNOWN_HOST", "localhost" ) );

    assertEquals( "Wrong message returned", "Client request for URI \"A\" failed. Reason for failure: B.",
      Messages.getString( "ThreadSafeHttpClient.ERROR_0001_CLIENT_REQUEST_FAILED", "A", "B" ) );

  }

  @Test
  public void testErrorMessages() {

    assertEquals(
      "Wrong message returned", "StopJettyServer.ERROR_0003 - Unknown Host: localhost",
      Messages.getErrorString( "StopJettyServer.ERROR_0003_UNKNOWN_HOST", "localhost" ) );

    assertEquals( "Wrong message returned", "PacService.ERROR_0048 - Failed to rollback transaction.",
      Messages.getErrorString( "PacService.ERROR_0048_ROLLBACK_FAILED" ) );
  }

  @Test
  public void testBadKey() {

    assertEquals( "Wrong message returned", "!bogus key!", Messages.getString( "bogus key" ) );

    assertEquals(
      "Wrong message returned", "test.ERROR_0001 - !test.ERROR_0001_BOGUS!",
      Messages.getErrorString( "test.ERROR_0001_BOGUS" ) );

  }

  @Test
  public void testBadEncoding() {
    //the messages.properties file has a bad encoding for the test.encode1 property, this causes a
    // MissingResourceException which
    //manifests as a returned string of !<key>! for all getString calls including the good strings
    assertEquals( "!test.bad_encode1!", Messages.getString( "test.bad_encode1" ) );
    //it seems that the successful retrieval of a good message inside a bundle that has a bad encoding is not
    // consistent.
    //Therefore, the following check is not very useful.
    //    assertEquals("!test.MESSAGE1!", Messages.getString("test.MESSAGE1")); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
