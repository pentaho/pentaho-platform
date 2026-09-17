package org.pentaho.platform.web.jwt;

public class KeycloakConfig {
    private final String authServerUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakConfig(String authServerUrl, String realm, String clientId, String clientSecret) {
        this.authServerUrl = stripTrailingSlash(authServerUrl);
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getIntrospectionUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
    }

    public String getUserInfoUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
    }

    public String getRealm() { return realm; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
