package org.pentaho.platform.repository2.unified.exportManifest;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class SchemaGen extends SchemaOutputResolver {
		 
    public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
        File file = new File(suggestedFileName);
        StreamResult result = new StreamResult(file);
        result.setSystemId(file.toURI().toURL().toString());
        return result;
    }
 
    public static void main (String[] args) {
    	Class[] classes = new Class[1]; 
    	classes[0] = org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto.class; 

		try {
    	JAXBContext jaxbContext = JAXBContext.newInstance(classes);
    	 
    	SchemaOutputResolver sor = new SchemaOutputResolver() {
         @Override
         public Result createOutput(String namespace, String schema) throws IOException {
        	 	 String parentFolder = "/tmp";
        	 	 String schemaName="RepositoryFileDto";
             return new StreamResult(new File(parentFolder, schemaName));
         }
    	};
    	jaxbContext.generateSchema(sor);
    	} catch (Exception e){
    		
    	}
    }
}
