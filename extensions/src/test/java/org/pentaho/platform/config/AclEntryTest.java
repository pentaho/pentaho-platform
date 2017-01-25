package org.pentaho.platform.config;

import org.junit.Test;
import org.pentaho.test.BeanTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by rfellows on 10/20/15.
 */
public class AclEntryTest extends BeanTester {
  public AclEntryTest() {
    super( AclEntry.class );
  }

  @Override
  @Test
  public void testHasValidBeanToString() {
    AclEntry aclEntry = new AclEntry( "admin", "publish" );
    assertEquals( "SERVICE NAME = admin ATTRIBUTE NAME =   publish", aclEntry.toString() );
  }
  @Override
  @Test
  public void testHasValidBeanEquals() {
    AclEntry aclEntry1 = new AclEntry( "admin", "publish" );
    AclEntry aclEntry2 = new AclEntry( "admin", "publish" );
    AclEntry aclEntry3 = new AclEntry( "admin", "write" );
    assertTrue( aclEntry1.equals( aclEntry2 ) );
    assertFalse( aclEntry1.equals( aclEntry3 ) );
  }
}
