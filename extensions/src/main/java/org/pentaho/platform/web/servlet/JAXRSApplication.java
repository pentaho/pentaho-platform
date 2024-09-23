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
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.fasterxml.jackson.core.util.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAXRSApplication is a class that extends ResourceConfig.
 * This can be used to configure the JAX-RS application of the Pentaho Platform.
 * This class will be configured as an initialization parameter for the Pentaho platform's {@link JAXRSServlet} in the web.xml file.
 */
public class JAXRSApplication extends ResourceConfig {

  /**
   * <p>The constructor for the JAXRSApplication class.
   * This constructor can be used for setting up the necessary configurations for the JAX-RS application.
   * It does this by registering the necessary packages and features.</p><br/>
   * <p>Jersey application will be recursively search into the following packages to find the resources within :
   * <li>"org.pentaho", "com.pentaho" - Pentaho platform.</li>
   * <li>"com.hitachivantara","org.hitachivantara" - Security-web.</li></p><br/>
   * <p> Following features are registered as part of Platform Jersey application initialization.
   * <li> MultiPartFeature : Provides support for handling multipart requests.</li>
   * <li> JacksonFeature : Provides support for JSON serialization and deserialization along with JAXB support.</li></p>
   */

  public JAXRSApplication() {
    packages( "com.pentaho", "org.pentaho","com.hitachivantara","org.hitachivantara" );
    register( MultiPartFeature.class );
    register( JacksonFeature.class );
  }
}
