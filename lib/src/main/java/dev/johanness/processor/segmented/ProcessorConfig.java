package dev.johanness.processor.segmented;

import dev.johanness.processor.AnnotationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Objects;

public final class ProcessorConfig {
  private final @NotNull ProcessingEnvironment processingEnv;
  private final @NotNull SegmentedExecutor executor;
  private final @NotNull Preliminary preliminary;

  ProcessorConfig(
      @NotNull ProcessingEnvironment processingEnv,
      @NotNull SegmentedExecutor executor)
  {
    this.processingEnv = processingEnv;
    this.executor = executor;
    this.preliminary = executor.getRoot();
  }

  public @NotNull ProcessingEnvironment processingEnv() {
    return processingEnv;
  }

  public @NotNull Messager messager() {
    return processingEnv.getMessager();
  }

  public <T> void set(@NotNull Key<T> key, @NotNull T value) {
    preliminary.set(key, value);
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, boolean recursive) {
    executor.addVisitor(visitor, recursive);
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull AnnotationType<?>... annotationTypes) {
    executor.addVisitor(visitor, annotationTypes);
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull ElementKind... kinds) {
    executor.addVisitor(visitor, kinds);
  }

  public <T> void addFinalizer(T item, @NotNull SubProcessor<T> processor) {
    preliminary.finalize(item, processor);
  }

  public @Nullable TypeElement tryResolveAnnotation(@NotNull AnnotationType<?> type) {
    Elements elements = processingEnv.getElementUtils();
    String moduleName = Objects.requireNonNullElse(type.moduleName(), "");
    ModuleElement moduleElement = elements.getModuleElement(moduleName);
    if (moduleElement == null) {
      return elements.getTypeElement(type.canonicalName());
    }
    else {
      return elements.getTypeElement(moduleElement, type.canonicalName());
    }
  }
}
