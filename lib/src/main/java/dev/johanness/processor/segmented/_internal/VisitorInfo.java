package dev.johanness.processor.segmented._internal;

import dev.johanness.processor.AnnotationAccess;
import dev.johanness.processor.AnnotationType;
import dev.johanness.processor.segmented.Preliminary;
import dev.johanness.processor.segmented.SubProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import java.util.Set;

public final class VisitorInfo implements SubProcessor<@NotNull Element> {
  private final @NotNull ElementVisitor<Boolean, Preliminary> visitor;
  private final @Nullable Set<ElementKind> kinds;
  private final @Nullable Set<AnnotationType<?>> annotationTypes;
  private final boolean recursive;

  public VisitorInfo(@NotNull ElementVisitor<Boolean, Preliminary> visitor,
                     @Nullable Set<ElementKind> kinds,
                     @Nullable Set<? extends AnnotationType<?>> annotationTypes,
                     boolean recursive)
  {
    this.visitor = visitor;
    this.kinds = kinds == null ? null : Set.copyOf(kinds);
    this.annotationTypes = annotationTypes == null ? null : Set.copyOf(annotationTypes);
    this.recursive = recursive;
    // TODO: We could validate the given visitor against the given kinds and annotation types.
    //  i.e. does the visitor implement all the methods it has to implement?
  }

  @Override
  public boolean process(@NotNull Element element, @NotNull Preliminary preliminary) {
    if (kindMatches(element, kinds) && annotationsMatch(element, annotationTypes)) {
      if (!element.accept(visitor, preliminary)) {
        return false;
      }
    }
    if (recursive && element.getKind() != ElementKind.MODULE && element.getKind() != ElementKind.PACKAGE) {
      for (Element enclosedElement : element.getEnclosedElements()) {
        preliminary.process(enclosedElement, this);
      }
    }
    return true;
  }

  private static boolean kindMatches(@NotNull Element construct, @Nullable Set<ElementKind> kinds) {
    return kinds == null || kinds.contains(construct.getKind());
  }

  private static boolean annotationsMatch(@NotNull AnnotatedConstruct construct, @Nullable Set<AnnotationType<?>> types) {
    if (types == null) {
      return true;
    }
    for (AnnotationType<?> type : types) {
      if (AnnotationAccess.hasAnnotation(construct, type)) {
        return true;
      }
    }
    return false;
  }
}
