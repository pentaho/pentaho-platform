package org.pentaho.wadl.helpers;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.WildcardType;

public class MockDeprecatedAnnotationTypeDoc implements AnnotationTypeDoc {

  @Override
  public String name() {
    return "deprecated";
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  public boolean isSerializable() {
    return false;
  }

  @Override
  public boolean isExternalizable() {
    return false;
  }

  @Override
  public MethodDoc[] serializationMethods() {
    return null;
  }

  @Override
  public FieldDoc[] serializableFields() {
    return null;
  }

  @Override
  public boolean definesSerializableFields() {
    return false;
  }

  @Override
  public ClassDoc superclass() {
    return null;
  }

  @Override
  public Type superclassType() {
    return null;
  }

  @Override
  public boolean subclassOf( ClassDoc cd ) {
    return false;
  }

  @Override
  public ClassDoc[] interfaces() {
    return null;
  }

  @Override
  public Type[] interfaceTypes() {
    return null;
  }

  @Override
  public TypeVariable[] typeParameters() {
    return null;
  }

  @Override
  public ParamTag[] typeParamTags() {
    return null;
  }

  @Override
  public FieldDoc[] fields() {
    return null;
  }

  @Override
  public FieldDoc[] fields( boolean filter ) {
    return null;
  }

  @Override
  public FieldDoc[] enumConstants() {
    return null;
  }

  @Override
  public MethodDoc[] methods() {
    return null;
  }

  @Override
  public MethodDoc[] methods( boolean filter ) {
    return null;
  }

  @Override
  public ConstructorDoc[] constructors() {
    return null;
  }

  @Override
  public ConstructorDoc[] constructors( boolean filter ) {
    return null;
  }

  @Override
  public ClassDoc[] innerClasses() {
    return null;
  }

  @Override
  public ClassDoc[] innerClasses( boolean filter ) {
    return null;
  }

  @Override
  public ClassDoc findClass( String className ) {
    return null;
  }

  @Override
  public ClassDoc[] importedClasses() {
    return null;
  }

  @Override
  public PackageDoc[] importedPackages() {
    return null;
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
  public AnnotationDesc[] annotations() {
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
  public String typeName() {
    return null;
  }

  @Override
  public String qualifiedTypeName() {
    return null;
  }

  @Override
  public String simpleTypeName() {
    return null;
  }

  @Override
  public String dimension() {
    return null;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public ClassDoc asClassDoc() {
    return null;
  }

  @Override
  public ParameterizedType asParameterizedType() {
    return null;
  }

  @Override
  public TypeVariable asTypeVariable() {
    return null;
  }

  @Override
  public WildcardType asWildcardType() {
    return null;
  }

  @Override
  public AnnotationTypeDoc asAnnotationTypeDoc() {
    return null;
  }

  @Override
  public AnnotationTypeElementDoc[] elements() {
    return null;
  }

}
