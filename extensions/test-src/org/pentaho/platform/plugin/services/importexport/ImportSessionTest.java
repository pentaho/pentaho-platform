package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class ImportSessionTest extends TestCase {

	  @Test
	  public void testSettingAclProperties() {
		  ImportSession importSession = ImportSession.getSession();
		  
		  // importSession.setAclProperties(applyAclSettingsFlag, retainOwnershipFlag, overwriteAclSettingsFlag)
		  
		  importSession.setAclProperties(true, true, true);
		  Assert.assertTrue(importSession.isApplyAclSettings());
		  Assert.assertTrue(importSession.isRetainOwnership());
		  Assert.assertTrue(importSession.isOverwriteAclSettings());
		  
		  importSession.setAclProperties(false, true, true);
		  Assert.assertFalse(importSession.isApplyAclSettings());
		  Assert.assertTrue(importSession.isRetainOwnership());
		  Assert.assertTrue(importSession.isOverwriteAclSettings());
		  
		  importSession.setAclProperties(false, false, true);
		  Assert.assertFalse(importSession.isApplyAclSettings());
		  Assert.assertFalse(importSession.isRetainOwnership());
		  Assert.assertTrue(importSession.isOverwriteAclSettings());
		  
		  importSession.setAclProperties(false, false, false);
		  Assert.assertFalse(importSession.isApplyAclSettings());
		  Assert.assertFalse(importSession.isRetainOwnership());
		  Assert.assertFalse(importSession.isOverwriteAclSettings());
		  
		  importSession.setAclProperties(true, false, false);
		  Assert.assertTrue(importSession.isApplyAclSettings());
		  Assert.assertFalse(importSession.isRetainOwnership());
		  Assert.assertFalse(importSession.isOverwriteAclSettings());
		  
		  importSession.setAclProperties(true, true, false);
		  Assert.assertTrue(importSession.isApplyAclSettings());
		  Assert.assertTrue(importSession.isRetainOwnership());
		  Assert.assertFalse(importSession.isOverwriteAclSettings());
	  }	
}
