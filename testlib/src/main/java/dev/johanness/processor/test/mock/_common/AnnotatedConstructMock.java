package dev.johanness.processor.test.mock._common;

import dev.johanness.processor.test._internal.AnnotationProxy;
import dev.johanness.processor.test._internal.ComparisonUtil;
import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import dev.johanness.processor.test.mock.annotation.AnnotationMirrorMock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class AnnotatedConstructMock<THIS extends AnnotatedConstructMock<THIS>> implements AnnotatedConstruct {
  private final @NotNull List<AnnotationMirror> annotations = new ArrayList<>();

  protected AnnotatedConstructMock(@NotNull Class<THIS> self) {
    // Verify that `this` is actually an instance of THIS, as it is assumed by `thiz()`.
    self.cast(this);
  }

  //region Builder methods

  @Contract(value = "_ -> this", mutates = "this")
  public THIS addAnnotation(@NotNull AnnotationMirror mirror) {
    annotations.add(InterfaceConflationProxy.create(mirror));
    return thiz();
  }

  @Contract(value = "_ -> this", mutates = "this")
  public THIS addAnnotation(@NotNull Annotation annotation) {
    return addAnnotation(new AnnotationMirrorMock<>().set(annotation));
  }

  @Contract(value = "_ -> this", mutates = "this")
  public THIS setAnnotation(@NotNull AnnotationMirror mirror) {
    removeAnnotations(mirror.getAnnotationType());
    return addAnnotation(mirror);
  }

  @Contract(value = "_ -> this", mutates = "this")
  public THIS setAnnotation(@NotNull Annotation annotation) {
    return setAnnotation(new AnnotationMirrorMock<>().set(annotation));
  }

  @Contract(value = "_ -> this", mutates = "this")
  public THIS removeAnnotations(@NotNull DeclaredType type) {
    annotations.removeIf(a -> ComparisonUtil.matches(a.getAnnotationType(), type));
    return thiz();
  }

  @Contract(value = "_ -> this", mutates = "this")
  public THIS removeAnnotations(@NotNull Class<?> type) {
    annotations.removeIf(a -> ComparisonUtil.matches(a.getAnnotationType(), type));
    return thiz();
  }

  //endregion
  //region Interface methods

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return annotations;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return annotations.stream()
        .filter(a -> ComparisonUtil.matches(a.getAnnotationType(), annotationType))
        .map(a -> AnnotationProxy.create(annotationType, a))
        .findFirst().orElse(null);
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return annotations.stream()
        .filter(a -> ComparisonUtil.matches(a.getAnnotationType(), annotationType))
        .map(a -> AnnotationProxy.create(annotationType, a))
        .toArray(length -> newArray(annotationType, length));
  }

  //endregion
  //region Internal methods

  @SuppressWarnings("unchecked")
  protected final @NotNull THIS thiz() {
    // The validity of this cast is verified in the constructor.
    return (THIS) this;
  }

  @SuppressWarnings("unchecked")
  private static <A> @NotNull A @NotNull [] newArray(@NotNull Class<A> type, int length) {
    return (A[]) Array.newInstance(type, length);
  }

  //endregion
}
