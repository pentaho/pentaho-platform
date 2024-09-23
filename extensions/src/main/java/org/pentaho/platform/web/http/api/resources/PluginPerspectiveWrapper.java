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

package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import java.util.List;

@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class PluginPerspectiveWrapper {

    @XmlElement( name = "pluginPerspective" )
    private List<PluginPerspective> pluginPerspectives;

    public PluginPerspectiveWrapper() {

    }

    public PluginPerspectiveWrapper( List<PluginPerspective> pluginPerspectives ) {
        this.pluginPerspectives = pluginPerspectives;
    }

    public List<PluginPerspective> getPluginPerspectives() {
        return pluginPerspectives;
    }

    public void setPluginPerspectives( List<PluginPerspective> pluginPerspectives ) {
        this.pluginPerspectives = pluginPerspectives;
    }
}
