package org.pentaho.platform.util;

import junit.framework.Assert;

import org.junit.Test;

public class RepositoryPathEncoderTest {
  @Test
  public void testEncode() {
    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%3A%3AWithColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File::WithColon" ) );

    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%3A%3AWith%255CColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File::With%5CColon" ) );
  }

  @Test
  public void testEncodeRepositoryPath() {
    Assert.assertEquals( ":public:Steel Wheels:File::WithColon", RepositoryPathEncoder
        .encodeRepositoryPath( "/public/Steel Wheels/File:WithColon" ) );
  }
  
  @Test
  public void testDecodeRepositoryPath() {
    Assert.assertEquals( "/public/Steel Wheels/File:WithColon", RepositoryPathEncoder
        .decodeRepositoryPath( ":public:Steel Wheels:File::WithColon" ) );
  }
}
