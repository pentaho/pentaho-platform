package org.pentaho.platform.web.jwt;

import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JWTPreAuthenticatedSessionHolderMapper {

    private final Map<String, String> jwtToSessionHolderKeyMap = Collections
            .synchronizedMap( new HashMap<String, String> ());

    private PreAuthenticatedSessionHolder preAuthenticatedSessionHolder;


    public JWTPreAuthenticatedSessionHolderMapper(PreAuthenticatedSessionHolder preAuthenticatedSessionHolder){
        this.preAuthenticatedSessionHolder = preAuthenticatedSessionHolder;
    }


    public Optional<String> getKeyByJWT(String jwt){
        return Optional.ofNullable(jwtToSessionHolderKeyMap.get(jwt));
    }

    public boolean restoreSessionByJWT(String jwt){
        Optional<String> key = getKeyByJWT(jwt);
        return key.isPresent() && preAuthenticatedSessionHolder.restoreSession(key.get());
    }

    public void captureCurrentSession(String jwt){
        String pentahoSessionKey = preAuthenticatedSessionHolder.captureSession();
        jwtToSessionHolderKeyMap.put(jwt, pentahoSessionKey);
    }
}
