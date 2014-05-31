package org.pentaho.platform.util;

import junit.framework.Assert;

import org.junit.Test;

public class RepositoryPathEncoderTest {
  @Test
  public void testEncode() {
    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%09WithColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File\tWithColon" ) );

    Assert.assertEquals( "%3Apublic%3ASteel%20Wheels%3AFile%09With%255CColon", RepositoryPathEncoder
        .encode( ":public:Steel Wheels:File\tWith%5CColon" ) );
  }

  @Test
  public void testEncodeRepositoryPath() {
    Assert.assertEquals( ":public:Steel Wheels:File\tWithColon", RepositoryPathEncoder
        .encodeRepositoryPath( "/public/Steel Wheels/File:WithColon" ) );
  }
  
  @Test
  public void testDecodeRepositoryPath() {
    Assert.assertEquals( "/public/Steel Wheels/File:WithColon", RepositoryPathEncoder
        .decodeRepositoryPath( ":public:Steel Wheels:File\tWithColon" ) );
  }
}
