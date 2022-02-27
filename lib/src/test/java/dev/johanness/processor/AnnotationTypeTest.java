package dev.johanness.processor;

import dev.johanness.processor._types.TopLevelAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class AnnotationTypeTest {

  @ParameterizedTest
  @ValueSource(classes = {TopLevelAnnotation.class, TopLevelAnnotation.NestedAnnotation.class})
  void testBinaryName(@NotNull Class<? extends Annotation> clazz) {
    AnnotationType<Proxy> type = new SimpleAnnotationType(clazz);
    assertEquals(clazz.getName(), type.binaryName());
  }

  @ParameterizedTest
  @ValueSource(classes = {TopLevelAnnotation.class, TopLevelAnnotation.NestedAnnotation.class})
  void testCanonicalName(@NotNull Class<? extends Annotation> clazz) {
    AnnotationType<Proxy> type = new SimpleAnnotationType(clazz);
    assertEquals(clazz.getCanonicalName(), type.canonicalName());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "org.example.module")
  void testModuleName(@Nullable String moduleName) {
    AnnotationType<Proxy> type = new SimpleAnnotationType("someName", moduleName);
    assertEquals(moduleName, type.moduleName());
  }

  @Test
  void testProxy() {
    AnnotationMirror mirror = mock(AnnotationMirror.class);
    DeclaredType annotationType = mock(DeclaredType.class);
    TypeElement typeElement = mock(TypeElement.class);
    Name typeName = mock(Name.class);

    when(mirror.getAnnotationType()).thenReturn(annotationType);
    when(annotationType.asElement()).thenReturn(typeElement);
    when(typeName.toString()).thenReturn(TopLevelAnnotation.class.getCanonicalName());
    when(typeName.contentEquals(TopLevelAnnotation.class.getCanonicalName())).thenReturn(true);
    when(typeElement.getQualifiedName()).thenReturn(typeName);
    when(typeElement.accept(any(), any())).then(invocation -> {
      ElementVisitor<Object, Object> visitor = invocation.getArgument(0);
      return visitor.visitType(typeElement, invocation.getArgument(1));
    });

    AnnotationType<Proxy> type = new SimpleAnnotationType(TopLevelAnnotation.class);
    assertSame(mirror, type.proxy(mirror).mirror);
  }

  private static final class SimpleAnnotationType extends AnnotationType<Proxy> {
    private SimpleAnnotationType(@NotNull Class<? extends Annotation> clazz) {
      super(clazz);
    }

    private SimpleAnnotationType(@NotNull String binaryName, @Nullable String moduleName) {
      super(binaryName, moduleName);
    }

    @Override
    protected @NotNull Proxy createProxy(@NotNull AnnotationMirror mirror) {
      return new Proxy(mirror);
    }
  }

  private static final class Proxy {
    private final @NotNull AnnotationMirror mirror;

    private Proxy(@NotNull AnnotationMirror mirror) {
      this.mirror = mirror;
    }
  }
}
