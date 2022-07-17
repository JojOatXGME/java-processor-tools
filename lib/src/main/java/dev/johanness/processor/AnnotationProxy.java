package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.util.Objects;

import static dev.johanness.processor.ElementCast.toExecutableElement;
import static dev.johanness.processor.ElementCast.toTypeElement;

public abstract class AnnotationProxy {
  protected final @NotNull AnnotationMirror mirror;

  protected AnnotationProxy(@NotNull AnnotationMirror mirror) {
    this.mirror = mirror;
  }

  @Override
  public String toString() {
    return mirror.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnnotationProxy that = (AnnotationProxy) o;
    return mirror.equals(that.mirror);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mirror);
  }

  /**
   * Reads a value from this annotation. The following code snipped shows the
   * intended usage.
   * <pre>
   *   public @NotNull String value() {
   *     cachedValue = readValue(cachedValue, "value", ValueType.string());
   *     return cachedValue;
   *   }
   * </pre>
   * If no value is provided by the annotation, the default from the
   * corresponding {@link ExecutableElement} of the annotation method is used.
   *
   * @param cached cached value which will be returned if not {@code null},
   *               otherwise this argument is ignored.
   * @param name   the name of the annotation method.
   * @param type   the expected value type.
   * @param <T>    type which is returned by this method.
   * @return the value from the annotation, or the default if no value is
   * provided.
   */
  protected final <T> @NotNull T readValue(@Nullable T cached, @NotNull String name, @NotNull ValueType<T> type) {
    return cached != null ? cached : type.convert(readValue(name));
  }

  private @NotNull AnnotationValue readValue(@NotNull String name) {
    Element annotationType = mirror.getAnnotationType().asElement();
    for (var entry : mirror.getElementValues().entrySet()) {
      ExecutableElement method = entry.getKey();
      AnnotationValue value = entry.getValue();
      assert method.getEnclosingElement().equals(annotationType);
      if (method.getSimpleName().contentEquals(name)) {
        return value;
      }
    }
    for (Element member : annotationType.getEnclosedElements()) {
      if (member.getKind() == ElementKind.METHOD &&
          member.getSimpleName().contentEquals(name)) {
        AnnotationValue value = toExecutableElement(member).getDefaultValue();
        if (value == null) {
          throw new IllegalStateException(String.format(
              "Unset annotation value without default: %s.%s()",
              toTypeElement(annotationType).getQualifiedName(),
              member.getSimpleName()));
        }
        return value;
      }
    }
    throw new IllegalArgumentException(String.format(
        "%s() not defined on @%s",
        name, toTypeElement(annotationType).getQualifiedName()));
  }
}
