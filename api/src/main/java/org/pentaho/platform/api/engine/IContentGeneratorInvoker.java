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


package org.pentaho.platform.api.engine;

public interface IContentGeneratorInvoker {

  /**
   * Returns a IContentGenerator
   *
   * @return IContentGenerator
   */
  IContentGenerator getContentGenerator();

  /**
   * Predicate that answers whether the related IContentGenerator is able to support the content identified
   * by a contentTypeId
   *
   * contentTypeId is usually the aggregation of a content type ( such as ktr, prpt, ... ) with an action/perspective
   * ( such as viewer, edit, generatedContent, .. )
   *
   * @param contentTypeId the content type Id of the item; examples: ktr.viewer, prpt.edit, ...

   * @return true if the contentType + perspectiveName pair is supported/handled by the related IContentGenerator
   */
  boolean isSupportedContent( String contentTypeId );
}
