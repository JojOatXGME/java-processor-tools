package dev.johanness.processor.processor.annotation;

import dev.johanness.processor.AnnotationProxy;
import dev.johanness.processor.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;

public final class GenerateAnnotationTypes extends AnnotationProxy {
  private @Nullable String className;

  GenerateAnnotationTypes(@NotNull AnnotationMirror mirror) {
    super(mirror);
  }

  public @NotNull String className() {
    className = readValue(className, "className", ValueType.string());
    return className;
  }
}
