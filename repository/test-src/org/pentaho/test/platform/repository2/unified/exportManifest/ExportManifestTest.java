package org.pentaho.test.platform.repository2.unified.exportManifest;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestFormatException;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;

public class ExportManifestTest extends TestCase {
	ExportManifest exportManifest;
	RepositoryFile repoDir2;
	RepositoryFile repoFile3;
	RepositoryFileAcl repoDir1Acl;
	RepositoryFileAcl repoDir2Acl;
	RepositoryFileAcl repoFile3Acl;
	ExportManifestEntity entity1;
	ExportManifestEntity entity2;
	ExportManifestEntity entity3;

	public ExportManifestTest() {
	  String rootFolder = "/dir1/";
	  exportManifest = new ExportManifest();
	  ExportManifestDto.ExportManifestInformation exportManifestInformation = exportManifest
	      .getManifestInformation();
	  exportManifestInformation.setExportBy("MickeyMouse");
	  exportManifestInformation.setExportDate("2013-01-01");
	  exportManifestInformation.setRootFolder(rootFolder);
	  
		List<RepositoryFileAce> aces1 = new ArrayList<RepositoryFileAce>();
		aces1.add(createMockAce("admin-/pentaho/tenant0", "USER",
				RepositoryFilePermission.READ, RepositoryFilePermission.WRITE));
		aces1.add(createMockAce("TenantAdmin-/pentaho/tenant0", "ROLE",
				RepositoryFilePermission.READ));
		repoDir2 = createMockRepositoryFile("/dir1/dir2", true);
		repoDir2Acl = createMockRepositoryAcl("acl2", "admin", false, aces1);
		repoFile3 = createMockRepositoryFile("/dir1/dir2/file1", false);
		RepositoryFile badRepoFile = createMockRepositoryFile("/baddir/dir2/file1", false);
		
		try {
  		exportManifest.add(repoDir2, repoDir2Acl);
  		exportManifest.add(repoFile3, null);
		}
  	catch (Exception e) {
  	  fail(e.getStackTrace().toString());
  	}
		
		try {
		  exportManifest.add(badRepoFile, null);
		  fail("Bad path did not generate a ExportManifestFormatException");
		} catch (ExportManifestFormatException e) {
		  
		}
		
		//entity1 = exportManifest.getExportManifestEntity("dir1");
		//assertNotNull(entity1);
		entity2 = exportManifest.getExportManifestEntity("dir2");
		assertNotNull(entity2);
		entity3 = exportManifest.getExportManifestEntity("dir2/file1");
		assertNotNull(entity3);

	}

	@Test
	public void testEntityAccess() {
		ExportManifestEntity entityR1 = exportManifest
				.getExportManifestEntity("dir2");
		assertNotNull(entityR1);
		assertEquals("Path value", "dir2", entityR1.getPath());
	}

	@Test
	public void testMarshal() {
		try {
			exportManifest.toXml(System.out);
		} catch (Exception e) {
			fail("Could not marshal to XML " + e);
		}
	}
	
	@Test
	public void testUnMarshal() {
		String xml = XmlToString();
		ExportManifest importManifest;
		ByteArrayInputStream input = new ByteArrayInputStream (xml.getBytes());
		try {
		importManifest = ExportManifest.fromXml(input);
		} catch (JAXBException e) {
			fail("Could not un-marshal to object " + e);
		}
		ExportManifestEntity fileEntity = exportManifest
				.getExportManifestEntity("dir2/file1");
		assertNotNull(fileEntity);
		assertEquals("dir2/file1", fileEntity.getPath());
		assertNotNull(fileEntity.getEntityMetaData());
		assertFalse(fileEntity.getEntityMetaData().getIsFolder());
		
		fileEntity = exportManifest
				.getExportManifestEntity("dir2");
		assertNotNull(fileEntity);
		assertNotNull(fileEntity.getEntityMetaData());
		assertTrue(fileEntity.getEntityMetaData().getIsFolder());
		
		RepositoryFile r = fileEntity.getRepositoryFile();
		try {
			RepositoryFileAcl rfa = fileEntity.getRepositoryFileAcl();
			assertNotNull(rfa.getAces());
		} catch (ExportManifestFormatException e) {
			e.printStackTrace();
			fail("Could not un-marshal to RepositoryFileAcl");
		}
		
	}
	
	@Test
	public void testXmlToString() {
		String s = XmlToString();
		assertNotNull(s);
	}
	
	private String XmlToString() {
		String s = null;
		try {
			s = exportManifest.toXmlString();
		} catch (JAXBException e) {
			fail("Could not marshal to XML to string " + e);
		}
		return s;
	}
	
	private RepositoryFile createMockRepositoryFile(String path, boolean isFolder) {
		Date createdDate = new Date();
		Date lastModeDate = new Date();
		Date lockDate = new Date();
		Date deletedDate = new Date();
		String baseName = path.substring(path.lastIndexOf("/") + 1);
		RepositoryFile mockRepositoryFile = new RepositoryFile("12345", baseName,
				isFolder, false, false, "versionId", path, createdDate, lastModeDate,
				false, "lockOwner", "lockMessage", lockDate, "en_US", "title",
				"description", "/original/parent/folder/path", deletedDate, 4096,
				"creatorId", null);
		return mockRepositoryFile;
	}

	private RepositoryFileAcl createMockRepositoryAcl(Serializable id,
			String owner, boolean entriesInheriting, List<RepositoryFileAce> aces) {
		RepositoryFileSid ownerSid = new RepositoryFileSid(owner);
		return new RepositoryFileAcl(id, ownerSid, entriesInheriting, aces);
	}

	private RepositoryFileAce createMockAce(String recipientName,
			String recipientType, RepositoryFilePermission first,
			RepositoryFilePermission... rest) {
		RepositoryFileSid.Type type = RepositoryFileSid.Type.valueOf(recipientType);
		RepositoryFileSid recipient = new RepositoryFileSid(recipientName, type);
		return new RepositoryFileAce(recipient, EnumSet.of(first, rest));
	}

}
