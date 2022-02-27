package dev.johanness.processor.test.mock.type;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.*;
import java.lang.reflect.Type;
import java.util.List;

public final class DeclaredTypeMock implements DeclaredType {
  // TODO: Implement!

  public DeclaredTypeMock(@NotNull Type type) {
  }

  @Override
  public Element asElement() {
    return null;
  }

  @Override
  public TypeMirror getEnclosingType() {
    return null;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return null;
  }

  @Override
  public TypeKind getKind() {
    return null;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return null;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return null;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return null;
  }
}
