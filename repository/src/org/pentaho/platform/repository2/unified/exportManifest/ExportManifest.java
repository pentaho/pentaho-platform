/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.repository2.unified.exportManifest;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestEntityDto;

/**
 * The Primary Object which represents the ExportManifest XML file by the same name 
 * stored in the Repository Export zip file during a repository export.
 * 
 * @author tkafalas
 */
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
		Marshaller marshaller = getMarshaller();
		marshaller.marshal(new JAXBElement<ExportManifestDto>(new QName("",
				"ExportManifest"), ExportManifestDto.class, getExportManifestDto()),
				outputStream);
	}

	public String toXmlString() throws JAXBException {
		StringWriter sw = new StringWriter();
		Marshaller marshaller = getMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<ExportManifestDto>(new QName(
				"http://www.pentaho.com/schema/", "ExportManifest"),
				ExportManifestDto.class, getExportManifestDto()), sw);
		return sw.toString();
	}

	private Marshaller getMarshaller() throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext
				.newInstance(ExportManifestDto.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	public static ExportManifest fromXml(ByteArrayInputStream input)
			throws JAXBException {
		JAXBContext jc = JAXBContext
				.newInstance("org.pentaho.platform.repository2.unified.exportManifest.bindings");
		Unmarshaller u = jc.createUnmarshaller();

		try {
			JAXBElement<ExportManifestDto> o = (JAXBElement) (u.unmarshal(input));
			ExportManifestDto exportManifestDto = o.getValue();
			ExportManifest exportManifest = new ExportManifest(exportManifestDto);
			return exportManifest;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
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
