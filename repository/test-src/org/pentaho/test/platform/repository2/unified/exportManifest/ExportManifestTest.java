package org.pentaho.test.platform.repository2.unified.exportManifest;

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
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;

public class ExportManifestTest extends TestCase {
	ExportManifest exportManifest;
	RepositoryFile repoDir1;
	RepositoryFile repoDir2;
	RepositoryFile repoFile3;
	RepositoryFileAcl repoDir1Acl;
	RepositoryFileAcl repoDir2Acl;
	RepositoryFileAcl repoFile3Acl;
	ExportManifestEntity entity1;
	ExportManifestEntity entity2;
	ExportManifestEntity entity3;

	public ExportManifestTest() {
		repoDir1 = createMockRepositoryFile("/dir1", true);
		List<RepositoryFileAce> aces1 = new ArrayList<RepositoryFileAce>();
		aces1.add(createMockAce("joe-/pentaho/tenant0", "USER",
				RepositoryFilePermission.READ, RepositoryFilePermission.WRITE));
		aces1.add(createMockAce("TenantAdmin-/pentaho/tenant0", "ROLE",
				RepositoryFilePermission.READ));
		repoDir1Acl = createMockRepositoryAcl("acl1", "joe", false, aces1);
		entity1 = new ExportManifestEntity(repoDir1, repoDir1Acl);
		assertNotNull(entity1);

		repoDir2 = createMockRepositoryFile("/dir1/dir2", true);
		entity2 = new ExportManifestEntity(repoDir2, null);
		assertNotNull(entity2);

		repoFile3 = createMockRepositoryFile("/dir1/dir2/file1", false);
		entity3 = new ExportManifestEntity(repoFile3, null);
		assertNotNull(entity3);

		exportManifest = new ExportManifest();
		ExportManifestDto.ExportManifestInformation exportManifestInformation = exportManifest
				.getManifestInformation();
		exportManifestInformation.setExportBy("MickeyMouse");
		exportManifestInformation.setExportDate("2013-01-01");

		exportManifest.add(entity1);
		exportManifest.add(entity2);
		exportManifest.add(entity3);
	}

	@Test
	public void testEntityAccess() {
		ExportManifestEntity entityR1 = exportManifest
				.getExportManifestEntity("/dir1/dir2");
		assertNotNull(entityR1);
		assertEquals("Path value", "/dir1/dir2", entityR1.getPath());
	}

	public void testMarshal() {
		try {
			exportManifest.toXml(System.out);
		} catch (JAXBException e) {
			fail("Could not marshal to XML " + e);
		}
	}

	private RepositoryFile createMockRepositoryFile(String path, boolean isFolder) {
		Date createdDate = new Date();
		Date lastModeDate = new Date();
		Date lockDate = new Date();
		Date deletedDate = new Date();
		String baseName = path.substring(path.lastIndexOf("/") + 1);
		RepositoryFile mockRepositoryFile = new RepositoryFile("12345", baseName,
				isFolder, false, false, "versionId", path, createdDate, lastModeDate,
				false, "lockOwner", "lockMessage", lockDate, "local", "title", null,
				"description", null, "/original/parent/folder/path", deletedDate, 4096,
				"creatorId");
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
