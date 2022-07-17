package dev.johanness.processor.examples.annotation_proxy;

import dev.johanness.processor.annotation.GenerateAnnotationTypes;
import dev.johanness.processor.examples.annotation_proxy.annotation.ComplexAnnotation;
import dev.johanness.processor.examples.annotation_proxy.annotation.EmptyAnnotation;
import dev.johanness.processor.examples.annotation_proxy.annotation.SimpleAnnotation;

@GenerateAnnotationTypes
final class AnnotationsGenerator {
  Class<ComplexAnnotation> COMPLEX;
  Class<EmptyAnnotation> EMPTY;
  Class<SimpleAnnotation> SIMPLE;
}
