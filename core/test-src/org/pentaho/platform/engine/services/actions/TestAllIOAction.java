package org.pentaho.platform.engine.services.actions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.action.IStreamingAction;

@SuppressWarnings("nls")
public class TestAllIOAction implements IStreamingAction {

  private OutputStream myContentOutput;
  private String message;
  private InputStream embeddedXmlResource;
  private List<String> addressees;
  private Long count;
  private Map<String, String> veggieData;
  private List<Map<String, String>> fruitData;
  private boolean executeWasCalled = false;

  public OutputStream getMyContentOutput() {
    return myContentOutput;
  }

  public void setOutputStream(OutputStream outputStream) {
    setMyContentOutputStream(outputStream);
  }

  public void setMyContentOutputStream(OutputStream outputStream) {
    this.myContentOutput = outputStream;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public InputStream getEmbeddedXmlResource() {
    return embeddedXmlResource;
  }

  public void setEmbeddedXmlResource(InputStream embeddedXmlResource) {
    this.embeddedXmlResource = embeddedXmlResource;
  }

  public List<String> getAddressees() {
    return addressees;
  }

  public void setAddressees(List<String> addressees) {
    this.addressees = addressees;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public Map<String, String> getVeggieData() {
    return veggieData;
  }

  public void setVeggieData(Map<String, String> veggieData) {
    this.veggieData = veggieData;
  }

  public List<Map<String, String>> getFruitData() {
    return fruitData;
  }

  public void setFruitData(List<Map<String, String>> fruitData) {
    this.fruitData = fruitData;
  }

  public String getEchoMessage() {
    return "Test String Output";
  }
  
  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public String getMimeType(String streamPropertyName) {
    return "text/html";
  }

}
