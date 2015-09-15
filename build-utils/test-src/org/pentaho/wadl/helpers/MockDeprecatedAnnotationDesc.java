package org.pentaho.wadl.helpers;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;

public class MockDeprecatedAnnotationDesc implements AnnotationDesc {

  @Override
  public AnnotationTypeDoc annotationType() {
    AnnotationTypeDoc annotationTypeDoc = new MockDeprecatedAnnotationTypeDoc();
    return annotationTypeDoc;
  }

  @Override
  public ElementValuePair[] elementValues() {
    return null;
  }

  @Override
  public String toString() {
    return "@java.lang.Deprecated";
  }

}
