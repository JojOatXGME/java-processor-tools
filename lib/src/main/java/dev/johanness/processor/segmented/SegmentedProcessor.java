package dev.johanness.processor.segmented;

import dev.johanness.processor.AnnotationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SegmentedProcessor implements javax.annotation.processing.Processor {
  private final @NotNull SourceVersion supportedSourceVersion;
  private final @Nullable Set<AnnotationType<?>> annotationTypes;
  private @Nullable ProcessingEnvironment processingEnv;
  private @Nullable SegmentedExecutor executor;

  protected SegmentedProcessor(
      @NotNull SourceVersion supportedSourceVersion,
      @Nullable Collection<? extends AnnotationType<?>> annotationTypes)
  {
    this.supportedSourceVersion = supportedSourceVersion;
    this.annotationTypes = annotationTypes == null ? null : Set.copyOf(annotationTypes);
  }

  @Override
  public final void init(ProcessingEnvironment processingEnv) {
    if (this.processingEnv != null) {
      throw new IllegalStateException(String.format(
          "%s.init(ProcessingEnvironment) must not be called more then once",
          getClass().getSimpleName()));
    }
    if (processingEnv == null) {
      throw new NullPointerException("processingEnv");
    }
    this.processingEnv = processingEnv;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    checkProcessingEnvironment();
    if (annotationTypes == null) {
      return Set.of("*");
    }
    else {
      boolean withModule = processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_9) >= 0;
      return annotationTypes.stream()
          .map(withModule ? AnnotationType::nameWithModule : AnnotationType::canonicalName)
          .collect(Collectors.toUnmodifiableSet());
    }
  }

  @Override
  public final @NotNull SourceVersion getSupportedSourceVersion() {
    checkProcessingEnvironment();
    return supportedSourceVersion;
  }

  @Override
  public final @NotNull Set<String> getSupportedOptions() {
    checkProcessingEnvironment();
    return Set.of();
  }

  @Override
  public final @NotNull Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
    checkProcessingEnvironment();
    return Set.of();
  }

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    checkProcessingEnvironment();
    if (executor == null) {
      executor = new SegmentedExecutor(processingEnv);
      ProcessorConfig config = new ProcessorConfig(processingEnv, executor);
      startProcessing(config);
    }
    executor.process(roundEnv);
    return false;
  }

  protected abstract void startProcessing(@NotNull ProcessorConfig processorConfig);

  private void checkProcessingEnvironment() {
    if (processingEnv == null) {
      throw new IllegalStateException(String.format(
          "%s.init(ProcessingEnvironment) must be called first",
          getClass().getSimpleName()));
    }
  }
}
