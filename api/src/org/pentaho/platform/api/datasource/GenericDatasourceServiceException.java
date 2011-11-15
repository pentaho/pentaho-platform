package org.pentaho.platform.api.datasource;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class GenericDatasourceServiceException extends PentahoCheckedChainedException {

  /**
  * 
  */
 private static final long serialVersionUID = -6089798664483298023L;

 /**
  * 
  */
 public GenericDatasourceServiceException() {
   super();
 }

 /**
  * @param message
  */
 public GenericDatasourceServiceException(String message) {
   super(message);
 }

 /**
  * @param message
  * @param reas
  */
 public GenericDatasourceServiceException(String message, Throwable reas) {
   super(message, reas);
 }

 /**
  * @param reas
  */
 public GenericDatasourceServiceException(Throwable reas) {
   super(reas);
 }

}
