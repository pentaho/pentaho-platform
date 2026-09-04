package org.pentaho.platform.web.http.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategySessionFactory;
import org.pentaho.platform.web.http.session.PentahoHttpSession;
import org.pentaho.platform.web.jwt.JWTPreAuthenticatedSessionHolderMapper;
import org.pentaho.platform.web.jwt.KeycloakAuthResult;
import org.pentaho.platform.web.jwt.KeycloakAuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import jakarta.ws.rs.core.SecurityContext;
;

//@WebFilter(filterName = "JwtAuthFilter", urlPatterns = "/*")
public class JwtAuthFilter implements Filter {

    private static Log LOGGER = LogFactory.getLog( JwtAuthFilter.class );

    private KeycloakAuthService authService;

    private CredentialsStrategySessionFactory credentialsStrategySessionFactory;

    private JWTPreAuthenticatedSessionHolderMapper jwtPreAuthenticatedSessionHolderMapper;

    public JwtAuthFilter(KeycloakAuthService authService, JWTPreAuthenticatedSessionHolderMapper jwtPreAuthenticatedSessionHolderMapper, CredentialsStrategySessionFactory credentialsStrategySessionFactory) {
        LOGGER.error("-----------------JwtAuthFilter----------------------");
        this.authService = authService;
        this.jwtPreAuthenticatedSessionHolderMapper = jwtPreAuthenticatedSessionHolderMapper;
        this.credentialsStrategySessionFactory = credentialsStrategySessionFactory;
        LOGGER.error("------------------123---------------------");
    }

//    @Override
//    public void init(FilterConfig filterConfig) {
//        String authServerUrl = getParam(filterConfig, "KEYCLOAK_AUTH_SERVER_URL", "https://sso.example.com");
//        String realm         = getParam(filterConfig, "KEYCLOAK_REALM", "myrealm");
//        String clientId      = getParam(filterConfig, "KEYCLOAK_CLIENT_ID", "myclient");
//        String clientSecret  = getParam(filterConfig, "KEYCLOAK_CLIENT_SECRET", "mysecret");
//
//        KeycloakConfig cfg = new KeycloakConfig(authServerUrl, realm, clientId, clientSecret);
//        this.authService = new KeycloakAuthService(cfg, new JWTPreAuthenticatedSessionHolderMapper());
//    }

    private String getParam(FilterConfig cfg, String name, String def) {
        String v = System.getenv(name);
        if (v != null && !v.isBlank()) return v;
        v = cfg.getServletContext().getInitParameter(name);
        return (v == null || v.isBlank()) ? def : v;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {

            HttpServletRequest request = (HttpServletRequest) servletRequest;

            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return;
            }

            String token = authorizationHeader.substring("Bearer ".length()).trim();
            KeycloakAuthResult authResult = authService.introspect(token);


            if (!authResult.active) {
                //Token inactive or invalid, so proceed with default actions
                LOGGER.debug("User was not authorized, JWT token is inactive or invalid");
                return;
            }


            if (!jwtPreAuthenticatedSessionHolderMapper.restoreSessionByJWT(token)) {
                //authentificate and save session

                int authsSize = authResult.getRoles() != null ? authResult.getRoles().size() : 0;
                List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();//(Arrays.asList(auths));
                if(authsSize == 0){
                    //TODO: refactor this!!!
                    authorities.add(new SimpleGrantedAuthority("Administrator"));
                    authorities.add(new SimpleGrantedAuthority("Authenticated"));
                }else {
                    for (String role : authResult.getRoles()) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                    authorities.add(new SimpleGrantedAuthority("Authenticated"));
                }


                // Add default role to all authenticating users

                User user = new User(authResult.getUsername(), "", authResult.isActive(), authResult.isActive(), authResult.isActive(),
                        authResult.isActive(), authorities);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

//                PentahoSessionHolder.getSession()


                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

               //old version
//                IPentahoSession session = new PentahoHttpSession(authResult.getUsername(), request.getSession(), request.getLocale(),
//                        null);
//
//                session.setAuthenticated(authResult.getUsername());
//
//                PentahoSessionHolder.setSession( session );
//                PentahoSystem.sessionStartup(session);

                //vamsi version:
                IPentahoSession pentahoSession = new StandaloneSession( authentication.getName() );
                pentahoSession.setAuthenticated( authentication.getName() );
                PentahoSystem.sessionStartup( pentahoSession );
                PentahoSessionHolder.setSession( pentahoSession );
                pentahoSession.setAttribute( "roles", authentication.getAuthorities() );



//                Session jcrSession = credentialsStrategySessionFactory.getAdminSession();

                request.getSession().setAttribute( PentahoSystem.PENTAHO_SESSION_KEY, pentahoSession );

                jwtPreAuthenticatedSessionHolderMapper.captureCurrentSession(token);
            }

        } catch (Exception e) {
            //Introspection failed, so proceed with default actions
            LOGGER.debug("User not authorized, JWT token is invalid, so introspection failed", e);
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }
    protected IPentahoSession generatePentahoSession( final HttpServletRequest httpRequest , String name) {
        IPentahoSession pentahoSession;

        HttpSession httpSession = httpRequest.getSession( false );
//        if ( httpSession != null ) {
            pentahoSession = new PentahoHttpSession( null, httpSession, httpRequest.getLocale(), null );
//        } else {
//            pentahoSession = new HttpSessionPentahoSessionIntegrationFilter.NoDestroyStandaloneSession( null );
//        }

//        if ( callSetAuthenticatedForAnonymousUsers ) {
            pentahoSession.setAuthenticated( name );
//        }

        ITempFileDeleter deleter = PentahoSystem.get( ITempFileDeleter.class, pentahoSession );
        if ( deleter != null ) {
            pentahoSession.setAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE, deleter );
        }

        return pentahoSession;
    }

}
