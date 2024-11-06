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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement( name = "settings" )
@XmlAccessorType( XmlAccessType.FIELD )
public class SettingsWrapper {

    @XmlElement( name = "setting" )
    private List<Setting> settings;

    public SettingsWrapper() {
    }

    public SettingsWrapper( List<Setting> settings ) {
        this.settings = settings;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void setSettings( List<Setting> settings ) {
        this.settings = settings;
    }
}
