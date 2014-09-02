package org.pentaho.platform.plugin.services.importer;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;

public class DefaultRepositoryContentConverterHandler implements IRepositoryContentConverterHandler {

  Map<String, Converter> converters;

  public DefaultRepositoryContentConverterHandler( final Map<String, Converter> converterMap ) {
    converters = new HashMap<String, Converter>();
    converters.putAll( converterMap );
  }

  @Override
  public Map<String, Converter> getConverters() {
    return converters;
  }

  @Override
  public void addConverter( String extension, Converter converter ) {
    converters.put( extension, converter );

  }

  @Override
  public Converter getConverter( String extension ) {
    return converters.get( extension );
  }
}
