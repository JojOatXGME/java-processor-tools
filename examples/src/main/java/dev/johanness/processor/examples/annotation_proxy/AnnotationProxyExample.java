package dev.johanness.processor.examples.annotation_proxy;

public final class AnnotationProxyExample {
  public static void main(String[] args) {
    System.out.println(Annotations.COMPLEX.nameWithModule());
    System.out.println(Annotations.EMPTY.nameWithModule());
    System.out.println(Annotations.SIMPLE.nameWithModule());
  }
}
