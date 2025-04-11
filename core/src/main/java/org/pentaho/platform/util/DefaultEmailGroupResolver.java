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

package org.pentaho.platform.util;

import org.pentaho.platform.api.scheduler2.IEmailGroupResolver;

public class DefaultEmailGroupResolver implements IEmailGroupResolver {
    @Override
    public String resolve(String param) {
        return param;
    }
}
