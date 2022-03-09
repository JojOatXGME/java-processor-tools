module dev.johanness.processor {
  requires transitive java.compiler;
  requires static org.jetbrains.annotations;
  exports dev.johanness.processor;
  exports dev.johanness.processor.annotation;
}
