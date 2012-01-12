package org.pentaho.platform.repository.pmd;

import java.io.InputStream;
import java.util.Map;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public interface IPentahoMetadataDomainRepositoryExporter {
  public Map<String, InputStream> getDomainFilesData(final String domainId);
}
