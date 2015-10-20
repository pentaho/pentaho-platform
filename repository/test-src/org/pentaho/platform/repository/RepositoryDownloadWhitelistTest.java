package org.pentaho.platform.repository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;


public class RepositoryDownloadWhitelistTest {

  @Test
  public void testDefaultWhitelistPositive() {
    RepositoryDownloadWhitelist rdwl = new RepositoryDownloadWhitelist();
    assertTrue( rdwl.accept( "my picture.gif" ) );
    assertTrue( rdwl.accept( "my picture.jpg" ) );
    assertTrue( rdwl.accept( "my picture.jpeg" ) );
    assertTrue( rdwl.accept( "my picture.png" ) );
    assertTrue( rdwl.accept( "my picture.bmp" ) );
    assertTrue( rdwl.accept( "my picture.tiff" ) );
    assertTrue( rdwl.accept( "my file.csv" ) );
    assertTrue( rdwl.accept( "my file.xls" ) );
    assertTrue( rdwl.accept( "my file.xlsx" ) );
    assertTrue( rdwl.accept( "my file.pdf" ) );
    assertTrue( rdwl.accept( "my file.txt" ) );
    assertTrue( rdwl.accept( "my file.css" ) );
    assertTrue( rdwl.accept( "my file.html" ) );
    assertTrue( rdwl.accept( "my file.js" ) );
    assertTrue( rdwl.accept( "my file.xml" ) );
    assertTrue( rdwl.accept( "my file.doc" ) );
    assertTrue( rdwl.accept( "my file.ppt" ) );

    assertTrue( rdwl.accept( "my picture.giF" ) );
    assertTrue( rdwl.accept( "my picture.jPg" ) );
    assertTrue( rdwl.accept( "my picture.jPeg" ) );
    assertTrue( rdwl.accept( "my picture.pNg" ) );
    assertTrue( rdwl.accept( "my picture.bmP" ) );
    assertTrue( rdwl.accept( "my picture.tiFf" ) );
    assertTrue( rdwl.accept( "my file.Csv" ) );
    assertTrue( rdwl.accept( "my file.Xls" ) );
    assertTrue( rdwl.accept( "my file.xlSx" ) );
    assertTrue( rdwl.accept( "my file.pDf" ) );
    assertTrue( rdwl.accept( "my file.tXt" ) );
    assertTrue( rdwl.accept( "my file.csS" ) );
    assertTrue( rdwl.accept( "my file.htMl" ) );
    assertTrue( rdwl.accept( "my file.Js" ) );
    assertTrue( rdwl.accept( "my file.xMl" ) );
    assertTrue( rdwl.accept( "my file.dOc" ) );
    assertTrue( rdwl.accept( "my file.pPt" ) );
  }

  @Test
  public void testDefaultWhitelistNegative() {
    RepositoryDownloadWhitelist rdwl = new RepositoryDownloadWhitelist();
    assertFalse( rdwl.accept( "my picture.giff" ) );
    assertFalse( rdwl.accept( "my picture.dll" ) );
    assertFalse( rdwl.accept( "my picture.exe" ) );
    assertFalse( rdwl.accept( "my picture" ) );
    assertFalse( rdwl.accept( "my picture.psd" ) );
    assertFalse( rdwl.accept( "my picture.xslt" ) );
  }

  @Test
  public void testNonDefault() {
    RepositoryDownloadWhitelist rdwl = new RepositoryDownloadWhitelist();
    rdwl.setExtensions( "exe,dll" );
    assertTrue( rdwl.accept( "my file.exe" ) );
    assertTrue( rdwl.accept( "my file.eXe" ) );
    assertTrue( rdwl.accept( "my file.dll" ) );
    assertTrue( rdwl.accept( "my file.dlL" ) );
    assertFalse( rdwl.accept( "my picture.tiff" ) );
    assertFalse( rdwl.accept( "my picture.jpg" ) );
    assertFalse( rdwl.accept( "my picture.png" ) );
    assertTrue( rdwl.getExtensions() .equals( "exe,dll" ) );
  }
}
