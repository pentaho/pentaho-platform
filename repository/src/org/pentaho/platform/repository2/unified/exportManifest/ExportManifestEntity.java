package org.pentaho.platform.repository2.unified.exportManifest;

import java.util.List;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.CustomProperty;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.EntityAcl;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.EntityMetaData;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestEntityDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestProperty;

public class ExportManifestEntity {
	private ExportManifestEntityDto rawExportManifestEntity;
	private List<ExportManifestProperty> rawPropertyList;
	private EntityMetaData entityMetaData;
	private EntityAcl entityAcl;
	private List<CustomProperty> customProperties;

	public ExportManifestEntity() {
		rawExportManifestEntity = new ExportManifestEntityDto();
		rawPropertyList = rawExportManifestEntity.getExportManifestProperty();
	}
	
	public ExportManifestEntity(RepositoryFile repositoryFile, RepositoryFileAcl repositoryFileAcl) {
		this();
		ExportManifestProperty rawExportManifestProperty = new ExportManifestProperty();
		createEntityMetaData(repositoryFile);
		createEntityAcl(repositoryFileAcl);
		rawExportManifestProperty.setEntityMetaData(entityMetaData);
		rawExportManifestProperty.setEntityAcl(entityAcl);
	}
	
	private void createEntityMetaData(RepositoryFile repositoryFile){
		entityMetaData = new EntityMetaData();
		entityMetaData.setCreatedBy(repositoryFile.getCreatorId());
		entityMetaData.setCreatedDate(XmlGregorianCalendarConverter.asXMLGregorianCalendar(repositoryFile.getCreatedDate()));
		entityMetaData.setDescription(repositoryFile.getDescription());
		entityMetaData.setIsHidden(repositoryFile.isHidden());
		entityMetaData.setIsFolder(repositoryFile.isFolder());
		entityMetaData.setLocale(repositoryFile.getLocale());
		entityMetaData.setName(repositoryFile.getName());
		entityMetaData.setPath(repositoryFile.getPath());
		entityMetaData.setTitle(repositoryFile.getTitle());
		setPath(repositoryFile.getPath());
	}
	
	private void createEntityAcl(RepositoryFileAcl repositoryFileAcl) {
		if (repositoryFileAcl == null) return;
		entityAcl = new EntityAcl();
		entityAcl.setEntriesInheriting(repositoryFileAcl.isEntriesInheriting());
		entityAcl.setOwner(repositoryFileAcl.getOwner().getName());
		entityAcl.setOwnerType(repositoryFileAcl.getOwner().getType().name());
		List<EntityAcl.Aces> aces = entityAcl.getAces();
		aces.clear();

		for (RepositoryFileAce repositoryFileAce: repositoryFileAcl.getAces()) {
			EntityAcl.Aces ace = new EntityAcl.Aces();
			ace.setRecipient(repositoryFileAce.getSid().getName());
			ace.setRecipientType(repositoryFileAce.getSid().getType().name());
			List<String> permissions = ace.getPermissions();
			for (RepositoryFilePermission permission: repositoryFileAce.getPermissions()) {
				permissions.add(permission.toString());
			}
			aces.add(ace);
		}
	}
	
	/**
	 * Builds an ExportManifestEntityDto for use by the ExportManifest Package.
	 * @return
	 */
	ExportManifestEntityDto getExportManifestEntityDto() {
		//Property list is not kept in sync.  Create it now
		List<ExportManifestProperty> rawProperties = rawExportManifestEntity.getExportManifestProperty();
		rawProperties.clear();
		if (entityMetaData != null) {
			ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
			exportManifestProperty.setEntityMetaData(entityMetaData);
			rawProperties.add(exportManifestProperty);
		}
		
		if (entityAcl != null) {
			ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
			exportManifestProperty.setEntityAcl(entityAcl);
			rawProperties.add(exportManifestProperty);
		}
		
		if (customProperties != null && customProperties.size() > 0) {
			ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
			exportManifestProperty.getCustomProperty().addAll(customProperties);
			rawProperties.add(exportManifestProperty);
		}
		return rawExportManifestEntity;
	}

	/**
	 * Create this object from the Jaxb bound version of the object.
	 * 
	 * @param exportManifestEntity
	 */
	public ExportManifestEntity(ExportManifestEntityDto exportManifestEntity){
		this.rawExportManifestEntity = exportManifestEntity;
  	for (ExportManifestProperty exportManifestProperty : exportManifestEntity.getExportManifestProperty()){
  		if (exportManifestProperty.getEntityMetaData() != null) {
  			entityMetaData = exportManifestProperty.getEntityMetaData();
  		} else if (exportManifestProperty.getEntityAcl() != null) {
  			entityAcl = exportManifestProperty.getEntityAcl();
  		} else if (exportManifestProperty.getCustomProperty() != null && exportManifestProperty.getCustomProperty().size() > 0) {
  			customProperties = exportManifestProperty.getCustomProperty();
  		}
  	}
  }
	
	public boolean isValid() {
		if (entityMetaData == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return the entityMetaData
	 */
	public EntityMetaData getEntityMetaData() {
		return entityMetaData;
	}

	/**
	 * @param entityMetaData
	 *          the entityMetaData to set
	 */
	public void setEntityMetaData(EntityMetaData entityMetaData) {
		this.entityMetaData = entityMetaData;
	}

	/**
	 * @return the entityAcl
	 */
	public EntityAcl getEntityAcl() {
		return entityAcl;
	}

	/**
	 * @param entityAcl
	 *          the entityAcl to set
	 */
	public void setEntityAcl(EntityAcl entityAcl) {
		this.entityAcl = entityAcl;
	}

	/**
	 * @return the customProperty
	 */
	public List<CustomProperty> getCustomProperties() {
		return customProperties;
	}

	/**
	 * @param customProperty
	 *          the customProperty to set
	 */
	public void setCustomProperties(List<CustomProperty> customProperties) {
		this.customProperties = customProperties;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return rawExportManifestEntity.getPath();
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		rawExportManifestEntity.setPath(path);
	}

}
