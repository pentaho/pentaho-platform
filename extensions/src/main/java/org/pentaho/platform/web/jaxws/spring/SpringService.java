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

package org.pentaho.platform.web.jaxws.spring;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.EndpointFactory;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.EntityResolver;

import jakarta.servlet.ServletContext;

import javax.xml.namespace.QName;

import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.Handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class SpringService implements FactoryBean, ServletContextAware, InitializingBean {

  @NotNull
  private Class<?> implType;

  private Invoker invoker;
  private QName serviceName;
  private QName portName;
  private Container container;

  private SDDocumentSource primaryWsdl;

  private Object primaryWSDLResource;

  private Collection<? extends SDDocumentSource> metadata;

  private Collection<Object> metadataResources;

  private EntityResolver resolver;

  private Object assembler;

  private WSBinding binding;

  private BindingID bindingID;
  private List<WebServiceFeature> features;

  private List<Handler> handlers;

  private ServletContext servletContext;

  public void setServletContext( ServletContext servletContext ) {
    this.servletContext = servletContext;
  }

  public void setImpl( Class implType ) {
    this.implType = implType;
  }

  public void setBean( Object sei ) {
    this.invoker = InstanceResolver.createSingleton( sei ).createInvoker();
    if ( this.implType == null ) {
      this.implType = sei.getClass();
    }
  }

  public void setInvoker( Invoker invoker ) {
    this.invoker = invoker;
  }

  public void setAssembler( Object assembler ) {
    if ( assembler instanceof TubelineAssembler || assembler instanceof TubelineAssemblerFactory ) {
      this.assembler = assembler;
    } else {
      throw new IllegalArgumentException("Invalid type for assembler " + assembler);
    }
  }

  public void setServiceName( QName serviceName ) {
    this.serviceName = serviceName;
  }

  public void setPortName( QName portName ) {
    this.portName = portName;
  }

  public void setContainer( Container container ) {
    this.container = container;
  }

  public void setBinding( WSBinding binding ) {
    this.binding = binding;
  }

  public void setBindingID( String id ) {
    this.bindingID = BindingID.parse( id );
  }

  public void setFeatures( List<WebServiceFeature> features ) {
    this.features = features;
  }

  public void setHandlers( List<Handler> handlers ) {
    this.handlers = handlers;
  }

  public void setPrimaryWsdl( Object primaryWsdl ) throws IOException {
    this.primaryWSDLResource = primaryWsdl;
  }

  public void setMetadata( Collection<Object> metadata ) {
    this.metadataResources = metadata;
  }

  public void setResolver( EntityResolver resolver ) {
    this.resolver = resolver;
  }

  private WSEndpoint<?> endpoint;

  public WSEndpoint getObject() throws Exception {
    if ( endpoint == null ) {
      if ( binding == null ) {
        if ( bindingID == null ) {
          bindingID = BindingID.parse(implType);
        }

        if ( features == null || features.isEmpty() ) {
          binding = BindingImpl.create(bindingID);
        } else {
          binding = BindingImpl.create(bindingID,
                  features.toArray(new WebServiceFeature[features.size()]));
        }
      } else {
        if ( bindingID != null ) {
          throw new IllegalStateException("Both bindingID and binding are configured");
        }

        if ( features != null ) {
          throw new IllegalStateException("Both features and binding are configured");
        }
      }

      if ( handlers != null ) {
        List<Handler> chain = binding.getHandlerChain();
        chain.addAll( handlers );
        binding.setHandlerChain( chain );
      }

      if ( primaryWsdl == null ) {
        EndpointFactory.verifyImplementorClass( implType );
        String wsdlLocation = EndpointFactory.getWsdlLocation( implType );
        if ( wsdlLocation != null ) {
          primaryWsdl = convertStringToSource(wsdlLocation);
        }
      }

      EntityResolver resolver = this.resolver;
      if ( resolver == null ) {
        if ( servletContext != null ) {
          resolver = XmlUtil.createEntityResolver( servletContext.getResource( "/WEB-INF/jax-ws-catalog.xml" ) );
        } else {
          resolver = XmlUtil.createEntityResolver( getClass().getClassLoader().getResource( "/META-INF/jax-ws-catalog.xml" ) );
        }
      }

      endpoint = WSEndpoint.create( implType, false, invoker, serviceName,
        portName, new ContainerWrapper(), binding, primaryWsdl, metadata, resolver, true );
    }
    return endpoint;
  }

  public void afterPropertiesSet() throws Exception {
    if ( this.primaryWSDLResource != null ) {
      this.primaryWsdl = this.resolveSDDocumentSource( this.primaryWSDLResource );
    }

    if ( this.metadataResources != null ) {
      List<SDDocumentSource> tempList =
        new ArrayList<SDDocumentSource>( this.metadataResources.size() );

      for ( Object resource : this.metadataResources ) {
        tempList.add( this.resolveSDDocumentSource( resource ) );
      }

      this.metadata = tempList;
    }
  }

  private SDDocumentSource resolveSDDocumentSource( Object resource ) {
    SDDocumentSource source;

    if ( resource instanceof String ) {
      source = this.convertStringToSource( ( String ) resource );
    } else if ( resource instanceof URL ) {
      source = SDDocumentSource.create( ( URL ) resource );
    } else if ( resource instanceof SDDocumentSource ) {
      source = ( SDDocumentSource ) resource;
    } else {
      throw new IllegalArgumentException( "Unknown type " + resource );
    }

    return source;
  }

  private SDDocumentSource convertStringToSource( String resourceLocation ) {
    URL url = null;

    if ( servletContext != null ) {
      try {
        url = servletContext.getResource( resourceLocation );
      } catch ( MalformedURLException e ) {

      }
    }

    if ( url == null ) {
      ClassLoader cl = implType.getClassLoader();
      url = cl.getResource( resourceLocation );
    }

    if (url == null) {
      try {
        url = new URL( resourceLocation );
      } catch ( MalformedURLException e ) {
      }
    }

    if ( url == null ) {
      throw new ServerRtException( "cannot.load.wsdl", resourceLocation );
    }

    return SDDocumentSource.create( url );
  }

  public boolean isSingleton() {
    return true;
  }

  public Class getObjectType() {
    return WSEndpoint.class;
  }

  private class ContainerWrapper extends Container {

    public <T> T getSPI( Class<T> spiType ) {
      // allow specified TubelineAssembler to be used
      if ( spiType == TubelineAssemblerFactory.class ) {
        if ( assembler instanceof TubelineAssemblerFactory ) {
          return spiType.cast(assembler);
        }

        if ( assembler instanceof TubelineAssembler ) {
          return spiType.cast( new TubelineAssemblerFactory() {
            public TubelineAssembler doCreate( BindingID bindingId ) {
              return ( TubelineAssembler ) assembler;
            }
          });
        }
      }
      if ( spiType == ServletContext.class ) {
        return spiType.cast( servletContext );
      }

      if ( container != null ) {
        T t = container.getSPI( spiType );
        if ( t != null ) {
          return t;
        }
      }

      if ( spiType == Module.class ) {
        return spiType.cast( module );
      }

      return null;
    }

    private final Module module = new Module() {
      private final List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

      public @NotNull List<BoundEndpoint> getBoundEndpoints() {
        return endpoints;
      }
    };
  }
}
