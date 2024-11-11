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


package org.pentaho.platform.plugin.services.metadata;

/**
 * @author Rowell Belen
 */
public interface IModelAnnotationsAwareMetadataDomainRepositoryImporter {

  public static final String PROPERTY_NAME_ANNOTATIONS = "annotations-xml";
  public static final String ANNOTATIONS_FILE_ID_POSTFIX = "-annotations";

  String loadAnnotationsXml( String domainId );

  void storeAnnotationsXml( String domainId, String annotationsXml );
}
