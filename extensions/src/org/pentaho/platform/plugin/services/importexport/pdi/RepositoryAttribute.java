/**
 * The Pentaho proprietary code is licensed under the terms and conditions
 * of the software license agreement entered into between the entity licensing
 * such code and Pentaho Corporation. 
 */
package org.pentaho.platform.plugin.services.importexport.pdi;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

public class RepositoryAttribute implements RepositoryAttributeInterface, java.io.Serializable {

  private static final long serialVersionUID = -5787096049770518000L; /* EESOURCE: UPDATE SERIALVERUID */

	private DataNode	dataNode;
	private List<DatabaseMeta>	databases;

	public RepositoryAttribute(DataNode dataNode, List<DatabaseMeta> databases) {
		this.dataNode = dataNode;
		this.databases = databases;
	}

	public void setAttribute(String code, String value) {
		dataNode.setProperty(code, value);
	}

	public String getAttributeString(String code) {
		DataProperty property = dataNode.getProperty(code);
		if (property!=null) {
			return property.getString();
		}
		return null;
	}

	public void setAttribute(String code, boolean value) {
		dataNode.setProperty(code, value);
	}

	public boolean getAttributeBoolean(String code) {
		DataProperty property = dataNode.getProperty(code);
		if (property!=null) {
			return property.getBoolean();
		}
		return false;
	}

	public void setAttribute(String code, long value) {
		dataNode.setProperty(code, value);
	}

	public long getAttributeInteger(String code) {
		DataProperty property = dataNode.getProperty(code);
		if (property!=null) {
			return property.getLong();
		}
		return 0L;
	}

	public void setAttribute(String code, DatabaseMeta databaseMeta) {
		dataNode.setProperty(code, databaseMeta.getObjectId().getId());
	}

	public DatabaseMeta getAttributeDatabaseMeta(String code) {
		DataProperty property =  dataNode.getProperty(code);
		if (property==null || Const.isEmpty(property.getString())) {
			return null;
		}
		ObjectId id = new StringObjectId(property.getString());
		return DatabaseMeta.findDatabase(databases, id);
	}
}
