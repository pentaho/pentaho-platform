/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * This service provides methods for listing, creating, downloading, uploading, and removal of files.
 *
 * @author aaron
 */
@Path ( "/repo/files/generatedContentForSchedule" )
public class FileResourceGeneratedContentForSchedule extends AbstractJaxRSResource {
  protected static final Log logger = LogFactory.getLog( FileResourceGeneratedContentForSchedule.class );

  protected SchedulerResource schedulerResource;

  public FileResourceGeneratedContentForSchedule() {
    schedulerResource = new SchedulerResource();
  }

  public FileResourceGeneratedContentForSchedule( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  /**
   * Retrieve the list of execute content by lineage id.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/repo/files/generatedContentForSchedule?lineageId=
   * </p>
   *
   * @param lineageId the path for the file.
   *
   * @return list of RepositoryFileDto objects.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;List&gt;
   * &lt;repositoryFileDto&gt;
   * &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   * &lt;fileSize&gt;3461&lt;/fileSize&gt;
   * &lt;folder&gt;false&lt;/folder&gt;
   * &lt;hidden&gt;false&lt;/hidden&gt;
   * &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   * &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   * &lt;locale&gt;en&lt;/locale&gt;
   * &lt;localePropertiesMapEntries&gt;
   * &lt;localeMapDto&gt;
   * &lt;locale&gt;default&lt;/locale&gt;
   * &lt;properties&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.title&lt;/key&gt;
   * &lt;value&gt;myFile&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;jcr:primaryType&lt;/key&gt;
   * &lt;value&gt;nt:unstructured&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;title&lt;/key&gt;
   * &lt;value&gt;myFile&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.description&lt;/key&gt;
   * &lt;value&gt;myFile Description&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;/properties&gt;
   * &lt;/localeMapDto&gt;
   * &lt;/localePropertiesMapEntries&gt;
   * &lt;locked&gt;false&lt;/locked&gt;
   * &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   * &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   * &lt;ownerType&gt;-1&lt;/ownerType&gt;
   * &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   * &lt;title&gt;myFile&lt;/title&gt;
   * &lt;versionId&gt;1.9&lt;/versionId&gt;
   * &lt;versioned&gt;true&lt;/versioned&gt;
   * &lt;/repositoryFileAclDto&gt;
   * &lt;/List&gt;
   * </pre>
   */
  @GET
  @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully got the generated content for schedule" ) } )
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( @QueryParam( "lineageId" ) String lineageId ) {
    return schedulerResource.doGetGeneratedContentForSchedule( lineageId );
  }

}


