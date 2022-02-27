module dev.johanness.processor.test {
  requires java.compiler;
  requires static org.jetbrains.annotations;
  exports dev.johanness.processor.test;
  exports dev.johanness.processor.test.mock.annotation;
  //exports dev.johanness.processor.test.mock.element;
  exports dev.johanness.processor.test.mock.type;
}
