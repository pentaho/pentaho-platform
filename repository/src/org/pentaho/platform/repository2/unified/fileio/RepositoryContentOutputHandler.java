package org.pentaho.platform.repository2.unified.fileio;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryContentOutputHandler extends BaseOutputHandler {

  public IContentItem getFileOutputContentItem() {
    String filePath = getSolutionPath();
    if (filePath.startsWith("~/") || filePath.startsWith("~\\") || filePath.equals("~")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      filePath = ClientRepositoryPaths.getUserHomeFolderPath(getSession().getName()) + "/"; //$NON-NLS-1$
      filePath = filePath + (getSolutionPath().length() > 1 ? getSolutionPath().substring(2) : getSolutionPath().substring(1));
    }
    IContentItem contentItem = null;
    String requestedFileExtension = MimeHelper.getExtension(getMimeType());
    if (requestedFileExtension == null) {
      contentItem = new RepositoryFileContentItem(filePath);
    } else {
      String tempFilePath = FilenameUtils.getFullPathNoEndSeparator(filePath) + "/" + FilenameUtils.getBaseName(filePath) + requestedFileExtension;
      contentItem = new RepositoryFileContentItem(tempFilePath);
    }
    return contentItem;
  }


}
