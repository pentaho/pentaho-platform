/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2 as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, you can obtain
 * a copy at http://www.gnu.org/licenses/gpl-2.0.html or from the Free Software Foundation, Inc.,  51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2018 Hitachi Vantara.  All rights reserved.
 *
 */

package org.pentaho.platform.api.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Dmitriy Stepanov on 02.02.18.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Documented
@Repeatable( CacheRegionsRequired.class )
public @interface CacheRegionRequired {

  enum RegionPhase {
    SYSTEM, PLUGIN, SESSION
  }

  String region();

  RegionPhase phase() default RegionPhase.SYSTEM;
}
