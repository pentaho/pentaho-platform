package org.pentaho.platform.repository2.unified.exportManifest;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestEntityDto;

public class ExportManifest {
	private HashMap<String, ExportManifestEntity> exportManifestEntities;
	private ExportManifestDto.ExportManifestInformation manifestInformation;

	public ExportManifest() {
		this.exportManifestEntities = new HashMap<String, ExportManifestEntity>();
		this.manifestInformation = new ExportManifestDto.ExportManifestInformation();
	}

	public ExportManifest(ExportManifestDto exportManifestDto) {
		this.manifestInformation = exportManifestDto.getExportManifestInformation();

	}

	public void add(ExportManifestEntity exportManifestEntity) {
		if (exportManifestEntity.isValid()) {
			exportManifestEntities.put(exportManifestEntity.getPath(),
					exportManifestEntity);
		}
	}

	public ExportManifestEntity getExportManifestEntity(String path) {
		return exportManifestEntities.get(path);
	}

	/**
	 * Mashalls the manifest object into xml on the given output stream
	 * 
	 * @param outputStream
	 * @throws JAXBException
	 */
	public void toXml(OutputStream outputStream) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext
				.newInstance(ExportManifestDto.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<ExportManifestDto>(new QName("",
				"ExportManifest"), ExportManifestDto.class, getExportManifestDto()),
				System.out);
	}

	ExportManifestDto getExportManifestDto() {
		ExportManifestDto rawExportManifest = new ExportManifestDto();
		List<ExportManifestEntityDto> rawEntityList = rawExportManifest
				.getExportManifestEntity();
		rawExportManifest.setExportManifestInformation(manifestInformation);
		TreeSet<String> ts = new TreeSet<String>(exportManifestEntities.keySet());
		for (String path : ts) {
			rawEntityList.add(exportManifestEntities.get(path)
					.getExportManifestEntityDto());
		}
		return rawExportManifest;
	}

	/**
	 * Factory method to deliver one ExportManifestEntity. The Manifest is built
	 * by adding one ExportManifestEntity object for each file and folder in the
	 * export set.
	 * 
	 * @return
	 */
	public ExportManifestEntity createExportManifestEntry() {
		return new ExportManifestEntity();
	}

	public boolean isValid() {
		if (this.exportManifestEntities.size() > 0) {
			for (ExportManifestEntity manEntity : exportManifestEntities.values()) {
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
	 * @param manifestInformation
	 *          the manifestInformation to set
	 */
	public void setManifestInformation(
			ExportManifestDto.ExportManifestInformation manifestInformation) {
		this.manifestInformation = manifestInformation;
	}

	public ExportManifestDto.ExportManifestInformation createExportManifestInformation() {
		return new ExportManifestDto.ExportManifestInformation();
	}

}
