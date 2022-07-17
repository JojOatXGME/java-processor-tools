package dev.johanness.processor.processor;

import dev.johanness.processor.AnnotationType;
import dev.johanness.processor.processor.annotation.Annotations;
import dev.johanness.processor.processor.visitor.AnnotationTypesVisitor;
import dev.johanness.processor.segmented.ProcessorConfig;
import dev.johanness.processor.segmented.SegmentedProcessor;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import java.util.Set;

public final class ToolsProcessor extends SegmentedProcessor {

  public ToolsProcessor() {
    super(SourceVersion.latestSupported(), Set.of(Annotations.GENERATE_ANNOTATION_TYPES));
  }

  @Override
  protected void startProcessing(@NotNull ProcessorConfig processorConfig) {
    checkAnnotation(processorConfig, Annotations.GENERATE_ANNOTATION_TYPES);
    processorConfig.addVisitor(new AnnotationTypesVisitor(), true);
  }

  private void checkAnnotation(@NotNull ProcessorConfig processorConfig, @NotNull AnnotationType<?> type) {
    if (processorConfig.tryResolveAnnotation(type) == null) {
      processorConfig.messager().printMessage(Diagnostic.Kind.WARNING, String.format(
          "Annotation interface @%s not found on the module path.",
          type.canonicalName()));
    }
  }
}
