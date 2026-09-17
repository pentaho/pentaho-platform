package org.pentaho.platform.web.jwt;

import java.util.Map;
import java.util.Set;

public class KeycloakAuthResult {
    public final boolean active;
    public final String username;
    public final Set<String> roles;
    public final Map<String, Object> attributes;

    public KeycloakAuthResult(boolean active, String username, Set<String> roles, Map<String, Object> attributes) {
        this.active = active;
        this.username = username;
        this.roles = roles;
        this.attributes = attributes;
    }

    public boolean isActive() {
        return active;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
