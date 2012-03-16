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
package org.pentaho.platform.plugin.services.importexport.pdi;

import java.io.InputStream;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.Converter;
import org.w3c.dom.Document;

/**
 * Converts stream of binary or character data.
 * 
 * @author rmansoor
 */
public class StreamToTransNodeConverter implements Converter {
  IUnifiedRepository unifiedRepository;
  
  public StreamToTransNodeConverter(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }
  
  public InputStream convert(final IRepositoryFileData data) {
    throw new UnsupportedOperationException();
  }

  public IRepositoryFileData convert(final InputStream inputStream, final String charset, final String mimeType) {
    try {
      TransMeta transMeta = new TransMeta();
      Repository repository = PDIImportUtil.connectToRepository(null);
      Document doc = PDIImportUtil.loadXMLFrom(inputStream);
      transMeta.loadXML(doc.getDocumentElement(), repository, false);
      TransformationDelegate delegate = new TransformationDelegate(repository);
      delegate.saveSharedObjects(transMeta, null);
      return new NodeRepositoryFileData(delegate.elementToDataNode(transMeta, unifiedRepository.getReservedChars()));
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
