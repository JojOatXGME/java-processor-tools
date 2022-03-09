package dev.johanness.processor.test;

import dev.johanness.processor.test.mock._common.NameMock;
import dev.johanness.processor.test.mock.annotation.AnnotationMirrorMock;
import dev.johanness.processor.test.mock.annotation.AnnotationValueMock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Name;
import java.lang.annotation.*;

public final class Mocks {
  private Mocks() {} // Cannot be instantiated

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Name name(@NotNull CharSequence value) {
    return new NameMock(value.toString());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull AnnotationValueMock value(@NotNull Object value) {
    return new AnnotationValueMock(value);
  }

  @Contract(value = "-> new", pure = true)
  public static @NotNull AnnotationMirrorMock<?> annotation() {
    return new AnnotationMirrorMock<>();
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull AnnotationMirrorMock<?> annotation(@NotNull Annotation annotation) {
    return new AnnotationMirrorMock<>().set(annotation);
  }

  @Contract(value = "_ -> new", pure = true)
  public static <A extends Annotation> @NotNull AnnotationMirrorMock<A> annotation(@NotNull Class<A> type) {
    return new AnnotationMirrorMock<>().type(type);
  }
}
