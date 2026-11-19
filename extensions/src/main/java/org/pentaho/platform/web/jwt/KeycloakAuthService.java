package org.pentaho.platform.web.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class KeycloakAuthService {

    private final KeycloakConfig keycloakConfig;
    private final HttpClient httpClient;

    JWTPreAuthenticatedSessionHolderMapper jwtPreAuthenticatedSessionHolderMapper;
    private final ObjectMapper om = new ObjectMapper();

    public KeycloakAuthService(KeycloakConfig keycloakConfig, JWTPreAuthenticatedSessionHolderMapper jwtMapper) {
        this.keycloakConfig = keycloakConfig;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.jwtPreAuthenticatedSessionHolderMapper = jwtMapper;
    }

    public KeycloakAuthResult introspect(String accessToken) throws IOException, InterruptedException {
        String body = formUrlEncoded(Map.of(
                "token", accessToken,
                "client_id", keycloakConfig.getClientId(),
                "client_secret", keycloakConfig.getClientSecret()
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(keycloakConfig.getIntrospectionUrl()))
                .timeout(Duration.ofSeconds(7))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            return new KeycloakAuthResult(false, null, Set.of(), Map.of());
        }

        JsonNode json = om.readTree(resp.body());
        boolean active = json.path("active").asBoolean(false);
        if (!active) {
            return new KeycloakAuthResult(false, null, Set.of(), Map.of());
        }

        // username/“preferred_username”/“sub”
        String username = firstNonBlank(
                json.path("username").asText(null),
                json.path("preferred_username").asText(null),
                json.path("sub").asText(null)
        );

        // Roles from the realm_access.roles
        Set<String> roles = new HashSet<>();
        JsonNode realmAccess = json.path("realm_access");
        if (realmAccess.isObject()) {
            JsonNode rolesNode = realmAccess.get("roles");
            if (rolesNode != null && rolesNode.isArray()) {
                for (JsonNode r : rolesNode) roles.add(r.asText());
            }
        }
        // Roles from the resource_access[client_id].roles
        JsonNode resourceAccess = json.path("resource_access");
        if (resourceAccess.isObject()) {
            Iterator<String> it = resourceAccess.fieldNames();
            while (it.hasNext()) {
                String client = it.next();
                JsonNode clientObj = resourceAccess.get(client);
                if (clientObj != null && clientObj.isObject()) {
                    JsonNode rolesNode = clientObj.get("roles");
                    if (rolesNode != null && rolesNode.isArray()) {
                        for (JsonNode r : rolesNode) roles.add(client + ":" + r.asText());
                    }
                }
            }
        }

        // do we need this info???
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("exp", json.path("exp").asLong(0));
        attrs.put("scope", json.path("scope").asText(""));

        return new KeycloakAuthResult(true, username, roles, attrs);
    }

    private static String formUrlEncoded(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
