package org.pentaho.platform.plugin.services.importer;

import java.util.Map;

/**
 * Resolves mime-types by extension.
 *
 * User: nbaker
 * Date: 6/18/12
 */
public class NameBaseMimeResolver implements IPlatformImportMimeResolver {

  private Map<String, String> extensionToMimeMap;

  public NameBaseMimeResolver(Map<String, String> extensionToMimeMap){
    if(extensionToMimeMap == null){
      throw new IllegalStateException("Missing extension map");
    }
    this.extensionToMimeMap = extensionToMimeMap;
  }

  @Override
  public String resolveMimeForBundle(IPlatformImportBundle bundle) {
    return (bundle.getMimeType() != null) ? bundle.getMimeType() : extensionToMimeMap.get(extractExtension(bundle.getName()));
  }

  private String extractExtension(String name) {
    if( name == null ) {
      return null;
    }
    int idx = name.lastIndexOf(".");
    if(idx == -1 || idx == name.length()){
      return name;
    }
    return name.substring(idx+1);
  }


}
