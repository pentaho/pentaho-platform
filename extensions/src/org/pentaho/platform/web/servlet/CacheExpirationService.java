package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * User: rfellows
 * Date: 10/25/11
 * Time: 2:36 PM
 */
public class CacheExpirationService extends ServletBase {

  private static final Log logger = LogFactory.getLog(CacheExpirationService.class);
  private ICacheExpirationRegistry cacheExpirationRegistry;

  public CacheExpirationService() {
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if(cacheExpirationRegistry == null) {
      cacheExpirationRegistry = PentahoSystem.get(ICacheExpirationRegistry.class, null);
    }
    OutputStream outputStream = response.getOutputStream();

    try {
      response.setContentType("text/xml"); //$NON-NLS-1$
      response.setStatus(HttpServletResponse.SC_OK);
      outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));
      outputStream.write(SoapHelper.openSoapResponse().getBytes(LocaleHelper.getSystemEncoding()));

      outputStream.write(getXml().getBytes(LocaleHelper.getSystemEncoding()));

      outputStream.write(SoapHelper.closeSoapResponse().getBytes(LocaleHelper.getSystemEncoding()));
      outputStream.write(SoapHelper.getSoapFooter().getBytes(LocaleHelper.getSystemEncoding()));

    } finally {
      outputStream.close();
    }
  }

  /**
   * this should only be used for granular unit testing.
   * The ICacheExpirationRegistry is provided by PentahoSystem if this is never called
   * @param registry
   */
  public void setExpirationRegistry(ICacheExpirationRegistry registry) {
    this.cacheExpirationRegistry = registry;
  }

  public String getXml() {
    StringBuilder sb = new StringBuilder();
    sb.append("<cache-expiration-items>");
    if(cacheExpirationRegistry != null) {
      List<ILastModifiedCacheItem> cacheItems = cacheExpirationRegistry.getCachedItems();
      for(ILastModifiedCacheItem item : cacheItems) {
        sb.append("<cache-item>"); //$NON-NLS-1$
        sb.append("<key>"); //$NON-NLS-1$
        sb.append(item.getCacheKey()); //$NON-NLS-1$
        sb.append("</key>"); //$NON-NLS-1$
        sb.append("<last-modified>"); //$NON-NLS-1$
        sb.append(Long.toString(item.getLastModified()));
        sb.append("</last-modified>"); //$NON-NLS-1$
        sb.append("</cache-item>"); //$NON-NLS-1$
      }
    }
    sb.append("</cache-expiration-items>"); //$NON-NLS-1$
    return sb.toString();
  }

}
