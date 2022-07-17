package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.AnnotatedConstruct;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationAccess {
  // TODO: Add basic test

  private AnnotationAccess() {} // Cannot be instantiated

  public static boolean has(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<?> type) {
    return hasAnnotation(construct, type);
  }

  public static boolean hasAnnotation(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<?> type) {
    return streamAnnotations(construct, type).findAny().isPresent();
  }

  public static <P> @NotNull P find(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return findAnnotation(construct, type);
  }

  public static <P> @NotNull P findAnnotation(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return streamAnnotations(construct, type).findAny()
        .orElseThrow(() -> new NoSuchElementException(type.toString()));
  }

  public static <P> @Nullable P search(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return searchAnnotation(construct, type);
  }

  public static <P> @Nullable P searchAnnotation(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return streamAnnotations(construct, type).findAny().orElse(null);
  }

  public static <P> @NotNull List<P> findAll(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return findAnnotations(construct, type);
  }

  public static <P> @NotNull List<P> findAnnotations(@NotNull AnnotatedConstruct construct, @NotNull AnnotationType<P> type) {
    return streamAnnotations(construct, type).collect(Collectors.toUnmodifiableList());
  }

  private static <P> @NotNull Stream<P> streamAnnotations(
      @NotNull AnnotatedConstruct construct,
      @NotNull AnnotationType<P> type)
  {
    return construct.getAnnotationMirrors().stream()
        .filter(mirror -> type.matches(mirror.getAnnotationType()))
        .map(type::proxy);
  }
}
