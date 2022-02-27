package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor9;
import java.util.Objects;

public abstract class AnnotationProxy {
  protected final @NotNull AnnotationMirror mirror;

  protected AnnotationProxy(@NotNull AnnotationMirror mirror) {
    this.mirror = mirror;
  }

  @Override
  public String toString() {
    return mirror.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnnotationProxy that = (AnnotationProxy) o;
    return mirror.equals(that.mirror);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mirror);
  }

  protected final <T> T readValue(@Nullable T cached, @NotNull String name, @NotNull ValueType<T> type) {
    return cached != null ? cached : type.convert(readValue(name));
  }

  private @NotNull AnnotationValue readValue(@NotNull String name) {
    Element annotationType = mirror.getAnnotationType().asElement();
    for (var entry : mirror.getElementValues().entrySet()) {
      ExecutableElement method = entry.getKey();
      AnnotationValue value = entry.getValue();
      assert method.getEnclosingElement().equals(annotationType);
      if (method.getSimpleName().contentEquals(name)) {
        return value;
      }
    }
    for (Element member : annotationType.getEnclosedElements()) {
      if (member.getKind() == ElementKind.METHOD &&
          member.getSimpleName().contentEquals(name)) {
        AnnotationValue value = asExecutableElement(member).getDefaultValue();
        if (value == null) {
          throw new IllegalStateException(String.format(
              "Unset annotation value without default: %s.%s()",
              asTypeElement(annotationType).getQualifiedName(),
              member.getSimpleName()));
        }
        return value;
      }
    }
    throw new IllegalArgumentException(String.format(
        "%s() not defined on @%s",
        name, asTypeElement(annotationType).getQualifiedName()));
  }

  private static @NotNull TypeElement asTypeElement(@NotNull Element element) {
    return element.accept(new SimpleElementVisitor9<>() {
      @Override
      public TypeElement visitType(TypeElement e, Object ignore) {
        return e;
      }

      @Override
      protected TypeElement defaultAction(Element e, Object ignore) {
        throw new IllegalArgumentException("Not a TypeElement: " + e);
      }
    }, null);
  }

  private static @NotNull ExecutableElement asExecutableElement(@NotNull Element element) {
    return element.accept(new SimpleElementVisitor9<>() {
      @Override
      public ExecutableElement visitExecutable(ExecutableElement e, Object ignore) {
        return e;
      }

      @Override
      protected ExecutableElement defaultAction(Element e, Object ignore) {
        throw new IllegalArgumentException("Not an ExecutableElement: " + e);
      }
    }, null);
  }
}
