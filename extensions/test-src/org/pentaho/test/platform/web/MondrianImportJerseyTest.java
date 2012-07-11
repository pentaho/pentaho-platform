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
  private static final String PARAMETERS = "parameters";

  private static final String IMPORT_URL = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/import";

  private static final String MONDRIAN_URL = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/importSchema";

  private static final String TEST_RES_IMPORT_TEST_FOODMART_XML = "test-res/ImportTest/FoodMart.xml";

  private static String domainName = "Mondrian FoodMart";

  private static String username = "joe";

  private static String pw = "password";

  private static final String MONDRIAN_URL_REMOVE = "http://localhost:8080/pentaho/plugin/data-access/api/mondrian/removeSchema";

  /**
   * @param args
   */
  public static void main(String[] args) {
    testMondrianSchemaImport(true);
    removeSchema();
  }


  private static void testRemoveInsert() {
    //mutipleImportTest();   
    String ans;
    removeSchema();//clean up first
    //bad password test
    // username = "foo";
    // pw = "foobar";
    ans = testMondrianSchemaImport(false); //first time 3
    username = "joe";
    pw = "password";
    ans = testMondrianSchemaImport(false); //should return 8 -existing schema
    ans = testMondrianSchemaImport(true); //3 - success
    removeSchema();//clean up 
  }
  private static void testPasswordUserName() {
    //mutipleImportTest();   
    String ans;
    removeSchema();//clean up first
    //bad password test
     username = "foo";
     pw = "foobar";
    ans = testMondrianSchemaImport(false); //first time error
    username = "joe";
    pw = "password";
    ans = testMondrianSchemaImport(false); //should return 3 -new schema
    ans = testMondrianSchemaImport(true); //3 - success
    removeSchema();//clean up 
  }

  private static void mutipleImportTest() {
    String ans;
    removeSchema();//clean up first
    ans = testMondrianSchemaImport(false); //first time
    try {
      ans = testMondrianSchemaImport(false);//should throw exception
    } catch (Exception ex) {
      System.out.println("Success overwrite was false and schema existing:" + ex.getMessage());
    }
    try {
      ans = testMondrianSchemaImport(true);
    } catch (Exception ex) {
      System.out.println("Error overwrite did not work" + ex.getMessage());
    }
    removeSchema();
  }

  private static void removeSchema() {

    try {
      FormDataMultiPart part = new FormDataMultiPart().field(PARAMETERS, "", MediaType.MULTIPART_FORM_DATA_TYPE).field(
          "datasourceName", domainName, MediaType.MULTIPART_FORM_DATA_TYPE);
      // If the import service needs the file name do the following.

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter(username, pw));
      WebResource resource = client.resource(MONDRIAN_URL_REMOVE);
      String response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(String.class, part);
      System.out.println("Remove Schema response=" + response);
    } catch (Exception e) {
      System.out.println("Remove Error " + e.getMessage());
    }
  }

  private static String testMondrianSchemaImport(boolean overwriteInRepos) {
    InputStream inputStream = null;
    String respsonse = null;
    try {
      File importFile = new File(TEST_RES_IMPORT_TEST_FOODMART_XML);
      inputStream = new FileInputStream(importFile);
      if (inputStream == null)
        return "-1";
      String parameters = buildParameters();
      FormDataMultiPart part = new FormDataMultiPart().field(PARAMETERS, parameters, MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("schemaFile", inputStream, MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("datasourceName", domainName, MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("overwrite", overwriteInRepos ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE)
          .field("xmlaEnabledFlag", "true", MediaType.MULTIPART_FORM_DATA_TYPE);

      // If the import service needs the file name do the following.
      part.getField("schemaFile").setContentDisposition(
          FormDataContentDisposition.name("schemaFile").fileName("test.foodmart.xml").build());

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter(username, pw));

      WebResource resource = client.resource(MONDRIAN_URL);
      respsonse = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(String.class, part);

    } catch (Exception e) {
      System.out.println("ImportSchema Error " + e.getMessage());
      e.printStackTrace();
    }
    System.out.println("testMondrianSchemaImport response=" + respsonse);
    return respsonse;
  }

  private static String testimportAnalysisDatasource() {
    InputStream inputStream = null;
    String response = "-1";
    try {
      inputStream = new FileInputStream(new File(TEST_RES_IMPORT_TEST_FOODMART_XML));
      if (inputStream == null)
        return "-1";
     
      String parms = "";
      FormDataMultiPart part = new FormDataMultiPart().field(PARAMETERS, buildParameters(), MediaType.TEXT_PLAIN_TYPE)
          .field("analysisFile", inputStream.toString(), MediaType.TEXT_PLAIN_TYPE)
          .field("databaseConnection", "SampleData", MediaType.TEXT_PLAIN_TYPE);

      // If the import service needs the file name do the following.
      part.getField("analysisFile").setContentDisposition(
          FormDataContentDisposition.name("analysisFile").fileName("test.mondrian.xml").build());

      Client client = Client.create();

      // Credentials here
      client.addFilter(new HTTPBasicAuthFilter(username, pw));

      WebResource resource = client.resource(IMPORT_URL);
      response = resource.type(MediaType.TEXT_PLAIN).put(String.class, part);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return response;
  }

  private static String buildParameters() {
    return "Provider=Mondrain;Datasource=FoodMart;overwrite=true;xmlaEnabled=false";
  }

}
