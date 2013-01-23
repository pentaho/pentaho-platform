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

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.Converter;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Converts stream of binary or character data.
 * 
 * @author mlowery
 */
public class StreamToJobNodeConverter implements Converter {

  IUnifiedRepository unifiedRepository;

  /**
   *
   * @param unifiedRepository
   */
  public StreamToJobNodeConverter(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }

  /**
   *
   * @param data
   * @return
   */
  public InputStream convert(final IRepositoryFileData data) {
    throw new UnsupportedOperationException();
  }

  /**
   *
   * @param fileId
   * @return
   */
  public InputStream convert(final Serializable fileId) {
    InputStream is = null;

    try{
      // this will change in the future if PDI no longer has its
      // own repository. For now, get the reference
      Repository repository = PDIImportUtil.connectToRepository(null);

      TransformationDelegate delegate = new TransformationDelegate(repository);

      // get the node file data
      NodeRepositoryFileData fileData = unifiedRepository.getDataForRead(fileId, NodeRepositoryFileData.class);

      // get the actual node
      DataNode node = fileData.getNode();

      // use the transformation delegate to get the repo element
      RepositoryElementInterface element = delegate.dataNodeToElement(node);

      // just cast it to pdi transmeta
      TransMeta transMeta = (TransMeta) element;

      // send back the input stream using transmeta's getXML
      return new ByteArrayInputStream(transMeta.getXML().getBytes());

    } catch(Exception e){
      e.printStackTrace();
    }
    return is;
  }

  /**
   *
   * @param inputStream
   * @param charset
   * @param mimeType
   * @return
   */
  public IRepositoryFileData convert(final InputStream inputStream, final String charset, final String mimeType) {
    try {
      JobMeta jobMeta = new JobMeta();
      Repository repository = PDIImportUtil.connectToRepository(null);
      Document doc = PDIImportUtil.loadXMLFrom(inputStream);
      jobMeta.loadXML(doc.getDocumentElement(), repository, null);
      JobDelegate delegate = new JobDelegate(repository); 
      delegate.saveSharedObjects(jobMeta, null);
      return new NodeRepositoryFileData(delegate.elementToDataNode(jobMeta, unifiedRepository.getReservedChars()));
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
