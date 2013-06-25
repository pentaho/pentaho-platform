package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@Path("/authorization/action")
public class AuthorizationActionResource {

  @SuppressWarnings("serial")
  private List<String> actionNames = new ArrayList<String>() {{
    add("org.pentaho.security.administerSecurity");
    add("org.pentaho.security.publish");
    add("org.pentaho.repository.create");
    add("org.pentaho.repository.read");
    add("org.pentaho.scheduler.manage");
    
  }};
    
  private List<IAuthorizationAction> authActionList; 
  
  public AuthorizationActionResource() {
  }
  
  public AuthorizationActionResource(List<IAuthorizationAction> authActionList) {
    
    this.authActionList = authActionList;
  }
  

  @GET
  @Path("/isauthorized")
  @Produces({ MediaType.TEXT_PLAIN })
  public Response validateAuth(@QueryParam("authAction") String authAction) {

    boolean validInput = false;

    /*
    for (IAuthorizationAction a : getActionList()) {
      if (a.getName().equals(authAction)) {
        validInput = true;
        break;
      }

    }
    */
    for (String s: actionNames) {
      if (s.equals(authAction)) {
        validInput= true;
        break;
      }
      
    }
    

    if (validInput) {
      IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
      boolean isAllowed = policy.isAllowed(authAction);
      if (isAllowed) {
        return Response.ok("true").build();
      }

    }

    return Response.ok("false").build();
  }

  private List<IAuthorizationAction> getActionList() {
    
    if (authActionList == null) {
      authActionList = PentahoSystem.getAll(IAuthorizationAction.class);
    }
    return authActionList;

  }
}
