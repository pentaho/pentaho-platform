package org.pentaho.wadl.helpers;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;

public class MockRootDoc implements RootDoc {

  @Override
  public ClassDoc[] classes() {
    ClassDoc[] classDocs = new ClassDoc[1];
    ClassDoc classDoc = new MockClassDoc();
    classDocs[0] = classDoc;
    return classDocs;
  }

  @Override
  public String commentText() {
    return null;
  }

  @Override
  public Tag[] tags() {
    return null;
  }

  @Override
  public Tag[] tags( String tagname ) {
    return null;
  }

  @Override
  public SeeTag[] seeTags() {
    return null;
  }

  @Override
  public Tag[] inlineTags() {
    return null;
  }

  @Override
  public Tag[] firstSentenceTags() {
    return null;
  }

  @Override
  public String getRawCommentText() {
    return null;
  }

  @Override
  public void setRawCommentText( String rawDocumentation ) {
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public int compareTo( Object obj ) {
    return 0;
  }

  @Override
  public boolean isField() {
    return false;
  }

  @Override
  public boolean isEnumConstant() {
    return false;
  }

  @Override
  public boolean isConstructor() {
    return false;
  }

  @Override
  public boolean isMethod() {
    return false;
  }

  @Override
  public boolean isAnnotationTypeElement() {
    return false;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isException() {
    return false;
  }

  @Override
  public boolean isError() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isAnnotationType() {
    return false;
  }

  @Override
  public boolean isOrdinaryClass() {
    return false;
  }

  @Override
  public boolean isClass() {
    return false;
  }

  @Override
  public boolean isIncluded() {
    return false;
  }

  @Override
  public SourcePosition position() {
    return null;
  }

  @Override
  public void printError( String msg ) {
  }

  @Override
  public void printError( SourcePosition pos, String msg ) {
  }

  @Override
  public void printWarning( String msg ) {
  }

  @Override
  public void printWarning( SourcePosition pos, String msg ) {
  }

  @Override
  public void printNotice( String msg ) {
  }

  @Override
  public void printNotice( SourcePosition pos, String msg ) {
  }

  @Override
  public String[][] options() {
    return null;
  }

  @Override
  public PackageDoc[] specifiedPackages() {
    return null;
  }

  @Override
  public ClassDoc[] specifiedClasses() {
    return null;
  }

  @Override
  public PackageDoc packageNamed( String name ) {
    return null;
  }

  @Override
  public ClassDoc classNamed( String qualifiedName ) {
    return null;
  }
}
