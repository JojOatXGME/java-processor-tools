package dev.johanness.processor.processor.annotation;

import dev.johanness.processor.AnnotationType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.*;
import java.util.function.Function;

public final class Annotations {
  public static final AnnotationType<GenerateAnnotationTypes> GENERATE_ANNOTATION_TYPES = new Type<>(
      dev.johanness.processor.annotation.GenerateAnnotationTypes.class,
      GenerateAnnotationTypes::new);

  private static final class Type<P> extends AnnotationType<P> {
    private final @NotNull Function<AnnotationMirror, P> proxyFactory;

    private Type(@NotNull Class<? extends Annotation> clazz, @NotNull Function<AnnotationMirror, P> proxyFactory) {
      super(clazz);
      this.proxyFactory = proxyFactory;
    }

    @Override
    protected @NotNull P createProxy(@NotNull AnnotationMirror mirror) {
      return proxyFactory.apply(mirror);
    }
  }
}
