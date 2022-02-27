package dev.johanness.processor.processor.annotation;

import dev.johanness.processor.AnnotationType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;

public final class Annotations {
  public static final AnnotationType<GenerateAnnotationTypes>
      GENERATE_ANNOTATION_TYPES = new AnnotationType<>(dev.johanness.processor.annotation.GenerateAnnotationTypes.class) {
    @Override
    protected @NotNull GenerateAnnotationTypes createProxy(@NotNull AnnotationMirror mirror) {
      return new GenerateAnnotationTypes(mirror);
    }
  };
}
