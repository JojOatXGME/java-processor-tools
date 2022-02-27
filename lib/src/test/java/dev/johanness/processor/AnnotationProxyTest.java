package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class AnnotationProxyTest {
  @Test
  void testReadValue() {
    // TODO: 27.02.2022 Test AnnotationProxy.readValue(...)
  }

  @Test
  void testEquals() {
    AnnotationMirror mirror1 = new EqualsMockAnnotationMirror(42);
    AnnotationMirror mirror2 = new EqualsMockAnnotationMirror(42);
    AnnotationMirror mirror3 = new EqualsMockAnnotationMirror(1337);
    assertEquals(new MyProxy(mirror1), new MyProxy(mirror1));
    assertEquals(new MyProxy(mirror1), new MyProxy(mirror2));
    assertNotEquals(new MyProxy(mirror1), new MyProxy(mirror3));
  }

  @Test
  void testHashCode() {
    AnnotationMirror mirror1 = new EqualsMockAnnotationMirror(42);
    AnnotationMirror mirror2 = new EqualsMockAnnotationMirror(42);
    AnnotationMirror mirror3 = new EqualsMockAnnotationMirror(1337);
    assertEquals(new MyProxy(mirror1).hashCode(), new MyProxy(mirror2).hashCode());
    assertNotEquals(new MyProxy(mirror1).hashCode(), new MyProxy(mirror3).hashCode());
  }

  @Test
  void testToString() {
    AnnotationMirror mirror = Mockito.mock(AnnotationMirror.class);
    Mockito.when(mirror.toString()).thenReturn("Hello, World!");
    Mockito.verifyNoMoreInteractions(mirror);
    MyProxy proxy = new MyProxy(mirror);
    assertEquals("Hello, World!", proxy.toString());
  }

  private static final class MyProxy extends AnnotationProxy {
    private MyProxy(@NotNull AnnotationMirror mirror) {
      super(mirror);
    }
  }

  private static final class EqualsMockAnnotationMirror implements AnnotationMirror {
    private final int hashCode;

    private EqualsMockAnnotationMirror(int hashCode) {
      this.hashCode = hashCode;
    }

    @Override
    public DeclaredType getAnnotationType() {
      throw new UnsupportedOperationException("getAnnotationType()");
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
      throw new UnsupportedOperationException("getElementValues()");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EqualsMockAnnotationMirror that = (EqualsMockAnnotationMirror) o;
      return hashCode == that.hashCode;
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
