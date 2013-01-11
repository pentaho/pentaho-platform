package org.pentaho.platform.repository2.unified.exportManifest;

import java.util.HashMap;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;

public class ExportManifest {
	private HashMap<String, ExportManifestEntity> exportManifestEntities;
	private ExportManifestDto.ExportManifestInformation manifestInformation;

	public ExportManifest(){
		this.exportManifestEntities = new HashMap<String, ExportManifestEntity>();
		this.manifestInformation = new ExportManifestDto.ExportManifestInformation();
	}
	
	public ExportManifest(ExportManifestDto exportManifestDto){
		this.manifestInformation = exportManifestDto.getExportManifestInformation();
		
	}
	
	public void add(ExportManifestEntity exportManifestEntity) {
		if (exportManifestEntity.isValid()) {
			exportManifestEntities.put(exportManifestEntity.getPath(), exportManifestEntity);
		}
	}
	
	public ExportManifestEntity getExportManifestEntity(String path) {
		return exportManifestEntities.get(path);
	}
	
	/**
	 * Factory method to deliver one ExportManifestEntity.  The Manifest is built by adding
	 * one ExportManifestEntity object for each file and folder in the export set. 
	 * 
	 * @return
	 */
	public ExportManifestEntity createExportManifestEntry() {
		return new ExportManifestEntity();
	}
	
	public boolean isValid() {
		if (this.exportManifestEntities.size() > 0) {
			for (ExportManifestEntity manEntity: exportManifestEntities.values()) {
				if (!manEntity.isValid()) {
					return false;
				}
			}
		}
		return true;
	}
	

	/**
	 * @return the manifestInformation
	 */
	public ExportManifestDto.ExportManifestInformation getManifestInformation() {
		return manifestInformation;
	}

	/**
	 * @param manifestInformation the manifestInformation to set
	 */
	public void setManifestInformation(
			ExportManifestDto.ExportManifestInformation manifestInformation) {
		this.manifestInformation = manifestInformation;
	}
	
	public ExportManifestDto.ExportManifestInformation createExportManifestInformation() {
		return new ExportManifestDto.ExportManifestInformation();
	}
		
}
