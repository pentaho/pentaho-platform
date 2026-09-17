package org.pentaho.platform.web.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JWTPreAuthenticatedSessionHolderMapperTest {

    private PreAuthenticatedSessionHolder holder;
    private JWTPreAuthenticatedSessionHolderMapper mapper;

    @BeforeEach
    void setUp() {
        holder = Mockito.mock(PreAuthenticatedSessionHolder.class);
        mapper = new JWTPreAuthenticatedSessionHolderMapper(holder);
    }

    @Test
    void getKeyByJWT_returnsEmpty_whenNothingCaptured() {
        assertTrue(mapper.getKeyByJWT("missing.jwt").isEmpty());
    }

    @Test
    void captureAndGetKey_returnsCapturedKey() {
        when(holder.captureSession()).thenReturn("SESSION-123");

        mapper.captureCurrentSession("jwt-1");

        Optional<String> key = mapper.getKeyByJWT("jwt-1");
        assertTrue(key.isPresent());
        assertTrue(key.get().contains("SESSION-123"));
        verify(holder, times(1)).captureSession();
    }

    @Test
    void multipleDifferentJwts_areIndependent() {
        when(holder.captureSession()).thenReturn("S-A", "S-B");

        mapper.captureCurrentSession("jwt-A");
        mapper.captureCurrentSession("jwt-B");

        assertTrue(mapper.getKeyByJWT("jwt-A").isPresent());
        assertTrue(mapper.getKeyByJWT("jwt-A").get().contains("S-A"));

        assertTrue(mapper.getKeyByJWT("jwt-B").isPresent());
        assertTrue(mapper.getKeyByJWT("jwt-B").get().contains("S-B"));
    }
}