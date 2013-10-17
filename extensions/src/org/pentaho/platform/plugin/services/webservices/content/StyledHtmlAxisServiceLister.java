/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.webservices.content;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.OutputStream;

public class StyledHtmlAxisServiceLister extends HtmlAxisServiceLister {

  private static final long serialVersionUID = 6592498636085258801L;

  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out )
    throws Exception {

    // write out the style sheet and the HTML document

    out.write( "<html>\n<head>".getBytes() ); //$NON-NLS-1$

    out.write( "<STYLE TYPE=\"text/css\" MEDIA=\"screen\">\n<!--\n".getBytes() ); //$NON-NLS-1$

    // IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);

    // FIXME: get style from system solution
    //    byte bytes[] = resLoader.getResourceAsBytes(this.getClass(), "resources/style.css" ); //$NON-NLS-1$
    // out.write( bytes );

    out.write( "\n-->\n</STYLE>\n".getBytes() ); //$NON-NLS-1$

    out.write( "</head>\n<body>\n".getBytes() ); //$NON-NLS-1$

    // get the list of services from the core ListServices
    super.createContent( axisConfiguration, context, out );

    out.write( "\n</html>\n".getBytes() ); //$NON-NLS-1$

  }

}
