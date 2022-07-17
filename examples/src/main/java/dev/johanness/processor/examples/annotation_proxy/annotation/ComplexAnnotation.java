package dev.johanness.processor.examples.annotation_proxy.annotation;

public @interface ComplexAnnotation {
  String name();
  Class<?> implementation();
  SimpleAnnotation[] others();
}
