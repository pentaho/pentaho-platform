/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.api.repository2.unified;

import java.util.Map;

/**
 * Repository Content Converter Handler manages the repository content converter in the platform
 * 
 * 
 * @author rmansoor
 * 
 */
public interface IRepositoryContentConverterHandler {

  /**
   * Retrieves a map of content converters. Key is the extension and the value is the converter for that extension.
   * 
   * @return map of converters and extensions
   */
  Map<String, Converter> getConverters();

  /**
   * Retrieves a particular converter based on the extension provided
   * 
   * @param extension
   * @return content converter
   */
  Converter getConverter( String extension );

  /**
   * Adds a new converter with the extension to the platform
   * 
   * @param extension
   * @param converter
   */
  void addConverter( String extension, Converter converter );

}
