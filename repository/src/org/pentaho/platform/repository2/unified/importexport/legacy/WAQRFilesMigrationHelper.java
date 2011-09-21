package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;

public class WAQRFilesMigrationHelper {
  public static final String OLD_WAQR_XACTION_EXT = "waqr.xaction"; //$NON-NLS-1$

  public static final String OLD_WAQR_XML_EXT = "waqr.xml"; //$NON-NLS-1$

  public static final String OLD_WAQR_XREPORTSPEC_EXT = "waqr.xreportspec"; //$NON-NLS-1$

  public static final String NEW_WAQR_XACTION_EXT = "xwaqr"; //$NON-NLS-1$

  public static final String NEW_WAQR_XML_EXT = "xml"; //$NON-NLS-1$

  public static final String NEW_WAQR_XREPORTSPEC_EXT = "xreportspec"; //$NON-NLS-1$
  
  public static final String OLD_METADATA_XMI = "steel-wheels/metadata.xmi";//$NON-NLS-1$
  
  public static final String NEW_METADATA_XMI = "steel-wheels.xmi";//$NON-NLS-1$

  public static String convertToNewExtension(String fileName) {
    int xactionIndex = fileName.indexOf(OLD_WAQR_XACTION_EXT);
    int xmlIndex = fileName.indexOf(OLD_WAQR_XML_EXT);
    int xreportspecIndex = fileName.indexOf(OLD_WAQR_XREPORTSPEC_EXT);

    if (xactionIndex >= 0) {
      return fileName.substring(0, xactionIndex) + NEW_WAQR_XACTION_EXT;
    } else if (xmlIndex >= 0) {
      return fileName.substring(0, xmlIndex) + NEW_WAQR_XML_EXT;
    } else if (xreportspecIndex >= 0) {
      return fileName.substring(0, xreportspecIndex) + NEW_WAQR_XREPORTSPEC_EXT;
    } else {
      return fileName;
    }
  }

  public static boolean hideFileCheck(String fileName) {
    int xmlIndex = fileName.indexOf(OLD_WAQR_XML_EXT);
    int xreportspecIndex = fileName.indexOf(OLD_WAQR_XREPORTSPEC_EXT);
    if (xmlIndex >= 0) {
      return true;
    } else if (xreportspecIndex >= 0) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isOldXWAQRFile(String fileName) {
    return fileName.indexOf(OLD_WAQR_XACTION_EXT) != -1;
  }
  
  public static boolean isOldXreportSpecFile(String fileName) {
    return fileName.indexOf(OLD_WAQR_XREPORTSPEC_EXT) != -1;
  }
  
  public static File convertToNewXWAQR(File file) {
    try {
      convertToNewXWAQR(new FileInputStream(file), new FileOutputStream(file));
      return file;
    } catch (Exception e) {
      return file;
    }
  }
  
  public static void convertToNewXWAQR(InputStream inputStream, OutputStream outputStream) {

    try {
      String fileAsString = IOUtils.toString(inputStream);
      fileAsString = StringUtils.replace(fileAsString, OLD_WAQR_XML_EXT, NEW_WAQR_XML_EXT);
      fileAsString = StringUtils.replace(fileAsString, OLD_WAQR_XACTION_EXT, NEW_WAQR_XACTION_EXT);
      outputStream.write(fileAsString.getBytes());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static File convertToNewXreportSpec(File file) {
    try {
      convertToNewXWAQR(new FileInputStream(file), new FileOutputStream(file));
      return file;
    } catch (Exception e) {
      return file;
    }
  }
  
  public static void convertToNewXreportSpec(InputStream inputStream, OutputStream outputStream) {

    try {
      String fileAsString = IOUtils.toString(inputStream);
      fileAsString = StringUtils.replace(fileAsString, OLD_METADATA_XMI, NEW_METADATA_XMI);
      outputStream.write(fileAsString.getBytes());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
