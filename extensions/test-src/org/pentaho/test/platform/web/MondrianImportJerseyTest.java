package org.pentaho.test.platform.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.plugin.services.importer.MondrianImportHandler;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

public class MondrianImportJerseyTest {
  private static final String IMPORT_URL = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/import";

  private static final String MONDRIAN_URL = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/importSchema";

  private static final String TEST_RES_IMPORT_TEST_FOODMART_XML = "test-res/ImportTest/FoodMart.xml";

  private static final String domainName = "Mondrian FoodMart";

  private static final String MONDRIAN_URL_REMOVE = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/removeSchema";

  /**
   * @param args
   */
  public static void main(String[] args) {

    removeSchema();//clean up first
    testMondrianSchemaImport(false); //first time
    try {
      testMondrianSchemaImport(false);//should throw exception
    } catch (Exception ex) {
      System.out.println("Success " + ex.getMessage());
    }
    try {
      testMondrianSchemaImport(true);
    } catch (Exception ex) {
      System.out.println("Error " + ex.getMessage());
    }
    removeSchema();
  }

  private static void removeSchema() {

    try {
      FormDataMultiPart part = new FormDataMultiPart().field("parameters", "", MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("datasourceName", domainName, MediaType.MULTIPART_FORM_DATA_TYPE);
      // If the import service needs the file name do the following.
   

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter("joe", "password"));
      WebResource resource = client.resource(MONDRIAN_URL_REMOVE);
      String response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(String.class, part);
      System.out.println("response " + response);
    } catch (Exception e) {
      System.out.println("Remove Error " + e.getMessage());
    }
  }

  private static void testMondrianSchemaImport(boolean overwriteInRepos) {
    InputStream inputStream = null;
    try {
      File importFile = new File(TEST_RES_IMPORT_TEST_FOODMART_XML);
      inputStream = new FileInputStream(importFile);
      if (inputStream == null)
        return;

      FormDataMultiPart part = new FormDataMultiPart().field("parameters", "", MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("schemaFile", inputStream, MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("datasourceName", domainName, MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("overwrite", overwriteInRepos?"true":"false", MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("xmlaEnabledFlag", "true", MediaType.MULTIPART_FORM_DATA_TYPE);

      // If the import service needs the file name do the following.
      part.getField("schemaFile").setContentDisposition(
          FormDataContentDisposition.name("schemaFile").fileName("test.foodmart.xml").build());

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter("joe", "password"));

      WebResource resource = client.resource(MONDRIAN_URL);
      String response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(String.class, part);
      System.out.println("response " + response);
    } catch (Exception e) {
      System.out.println("ImportSchema Error " + e.getMessage());
    }
  }

  private static void testimportAnalysisDatasource() {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File(TEST_RES_IMPORT_TEST_FOODMART_XML));
      if (inputStream == null)
        return;
      //(String parameters, @QueryParam("analysisFile")
      //String analysisFile, @QueryParam("databaseConnection")
      // String databaseConnection)
      String parms = "";
      FormDataMultiPart part = new FormDataMultiPart().field("parameters", parms, MediaType.TEXT_PLAIN_TYPE)
          .field("analysisFile", inputStream.toString(), MediaType.TEXT_PLAIN_TYPE)
          .field("databaseConnection", "SampleData", MediaType.TEXT_PLAIN_TYPE);

      // If the import service needs the file name do the following.
      part.getField("analysisFile").setContentDisposition(
          FormDataContentDisposition.name("analysisFile").fileName("test.mondrian.xml").build());

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter("joe", "password"));

      WebResource resource = client.resource(IMPORT_URL);
      String response = resource.type(MediaType.TEXT_PLAIN).put(String.class, part);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

    
}
