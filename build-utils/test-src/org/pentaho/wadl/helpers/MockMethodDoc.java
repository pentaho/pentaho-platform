package org.pentaho.wadl.helpers;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;

public class MockMethodDoc implements MethodDoc {

  @Override
  public String name() {
    return "methodName";
  }

  @Override
  public String commentText() {
    return "comment text";
  }

  @Override
  public AnnotationDesc[] annotations() {
    AnnotationDesc[] annotations = new AnnotationDesc[2];
    annotations[0] = new MockDeprecatedAnnotationDesc();
    annotations[1] = new MockPathAnnotationDesc();
    return annotations;
  }

  @Override
  public ClassDoc[] thrownExceptions() {
    return null;
  }

  @Override
  public Type[] thrownExceptionTypes() {
    return null;
  }

  @Override
  public boolean isNative() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public Parameter[] parameters() {
    return null;
  }

  @Override
  public ThrowsTag[] throwsTags() {
    return null;
  }

  @Override
  public ParamTag[] paramTags() {
    return null;
  }

  @Override
  public ParamTag[] typeParamTags() {
    return null;
  }

  @Override
  public String signature() {
    return null;
  }

  @Override
  public String flatSignature() {
    return null;
  }

  @Override
  public TypeVariable[] typeParameters() {
    return null;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public ClassDoc containingClass() {
    return null;
  }

  @Override
  public PackageDoc containingPackage() {
    return null;
  }

  @Override
  public String qualifiedName() {
    return null;
  }

  @Override
  public int modifierSpecifier() {
    return 0;
  }

  @Override
  public String modifiers() {
    return null;
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  @Override
  public boolean isProtected() {
    return false;
  }

  @Override
  public boolean isPrivate() {
    return false;
  }

  @Override
  public boolean isPackagePrivate() {
    return false;
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isFinal() {
    return false;
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
  public boolean isAbstract() {
    return false;
  }

  @Override
  public Type returnType() {
    return null;
  }

  @Override
  public ClassDoc overriddenClass() {
    return null;
  }

  @Override
  public Type overriddenType() {
    return null;
  }

  @Override
  public MethodDoc overriddenMethod() {
    return null;
  }

  @Override
  public boolean overrides( MethodDoc meth ) {
    return false;
  }

}
