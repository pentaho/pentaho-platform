package org.pentaho.test.platform.web.http.api;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import com.sun.jersey.api.client.ClientResponse;

import junit.framework.AssertionFailedError;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("nls")
public class JerseyTestUtil {

  protected static void assertResponse(ClientResponse response, ClientResponse.Status expectedStatus) {
    assertResponse(response, expectedStatus, null);
  }

  public static void assertResponse(ClientResponse response, ClientResponse.Status expectedStatus,
      String expectedMediaType) {
    try {
      assertEquals(expectedStatus, response.getClientResponseStatus());
    } catch (AssertionFailedError e) {
      throw new AssertionFailedError("Response status incorrect: " + e.getMessage());
    }

    if (expectedMediaType != null) {
      try {
        assertEquals(expectedMediaType, response.getType().toString());
      } catch (AssertionFailedError e) {
        throw new AssertionFailedError("Response media type incorrect: " + e.getMessage());
      }
    }
  }
  
  public static void assertResponseIsZip(ClientResponse response) {
    ZipInputStream zis = new ZipInputStream(response.getEntityInputStream());
    byte[] singleByte = new byte[1];
    try {
      zis.read(singleByte);
    } catch (IOException e) {
      throw new AssertionFailedError("Response entity is not a zip archive: "+e.getMessage());
    }
  }
}
