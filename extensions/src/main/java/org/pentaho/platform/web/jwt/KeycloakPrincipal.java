package org.pentaho.platform.web.jwt;

import java.security.Principal;
import java.util.Set;

public class KeycloakPrincipal implements Principal {
    private final String name;
    private final Set<String> roles;

    public KeycloakPrincipal(String name, Set<String> roles) {
        this.name = name;
        this.roles = Set.copyOf(roles);
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean hasRole(String role) {
        return roles.contains(role) || roles.contains("realm:" + role);
    }

    public Set<String> getRoles() {
        return roles;
    }
}
