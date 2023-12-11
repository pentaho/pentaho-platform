package org.pentaho.platform.util;

import org.pentaho.platform.api.scheduler2.IEmailGroupResolver;

public class DefaultEmailGroupResolver implements IEmailGroupResolver {
    @Override
    public String resolve(String param) {
        return param;
    }
}
