package dev.johanness.processor.test.mock.annotation;

import dev.johanness.processor.test._internal.Characters;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class AnnotationValueMock implements AnnotationValue {
  // TODO: Test this class
  @SuppressWarnings("rawtypes")
  private static final @NotNull Class<? extends List> UNTYPED_LIST_CLASS = List.class;
  @SuppressWarnings("unchecked")
  private static final @NotNull Class<List<? extends AnnotationValue>> ARRAY_CLASS = (Class<List<? extends AnnotationValue>>) UNTYPED_LIST_CLASS;

  private static final @NotNull Map<Class<?>, TypeData<?>> TYPES = Map.ofEntries(
      type(Boolean.class, AnnotationValueVisitor::visitBoolean, Object::toString),
      type(Character.class, AnnotationValueVisitor::visitChar, AnnotationValueMock::charLiteral),
      type(Byte.class, AnnotationValueVisitor::visitByte, Object::toString),
      type(Short.class, AnnotationValueVisitor::visitShort, Object::toString),
      type(Integer.class, AnnotationValueVisitor::visitInt, Object::toString),
      type(Long.class, AnnotationValueVisitor::visitLong, AnnotationValueMock::longLiteral),
      type(Float.class, AnnotationValueVisitor::visitFloat, AnnotationValueMock::floatLiteral),
      type(Double.class, AnnotationValueVisitor::visitDouble, AnnotationValueMock::doubleLiteral),
      type(String.class, AnnotationValueVisitor::visitString, AnnotationValueMock::stringLiteral),
      type(TypeMirror.class, AnnotationValueVisitor::visitType, AnnotationValueMock::classLiteral),
      type(VariableElement.class, AnnotationValueVisitor::visitEnumConstant, Object::toString),
      type(AnnotationMirror.class, AnnotationValueVisitor::visitAnnotation, Object::toString),
      type(ARRAY_CLASS, AnnotationValueVisitor::visitArray, AnnotationValueMock::arrayLiteral));

  private final @NotNull Object value;

  public AnnotationValueMock(@NotNull Object value) {
    if (!TYPES.containsKey(value.getClass())) {
      throw new IllegalArgumentException(String.format(
          "Invalid type %s of annotation value: %s",
          value.getClass(), value));
    }
    this.value = value;
  }

  @Override
  public @NotNull Object getValue() {
    return value;
  }

  @Override
  public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
    return TYPES.get(value.getClass()).accept(value, v, p);
  }

  @Override
  public String toString() {
    return TYPES.get(value.getClass()).toString(value);
  }

  private static <T> @NotNull Entry<Class<T>, TypeData<T>> type(
      @NotNull Class<T> type,
      @NotNull AcceptMethod<T> acceptMethod,
      @NotNull ToStringMethod<T> toStringMethod)
  {
    return Map.entry(type, new TypeData<>(type, acceptMethod, toStringMethod));
  }

  @FunctionalInterface
  private interface AcceptMethod<T> {
    <R, P> R call(@NotNull AnnotationValueVisitor<R, P> v, T av, P p);
  }

  @FunctionalInterface
  private interface ToStringMethod<T> {
    String call(@NotNull T value);
  }

  private static final class TypeData<T> {
    private final @NotNull Class<T> type;
    private final @NotNull AcceptMethod<T> acceptMethod;
    private final @NotNull ToStringMethod<T> toStringMethod;

    private TypeData(@NotNull Class<T> type, @NotNull AcceptMethod<T> acceptMethod, @NotNull ToStringMethod<T> toStringMethod) {
      this.type = type;
      this.acceptMethod = acceptMethod;
      this.toStringMethod = toStringMethod;
    }

    private <R, P> R accept(@NotNull Object value, @NotNull AnnotationValueVisitor<R, P> v, P p) {
      return acceptMethod.call(v, type.cast(value), p);
    }

    private @NotNull String toString(@NotNull Object value) {
      return toStringMethod.call(type.cast(value));
    }
  }

  private static @NotNull String charLiteral(char c) {
    StringBuilder builder = new StringBuilder("\\u0000".length()).append('\'');
    if (c == '"') {
      builder.append(c);
    }
    else {
      Characters.escape(builder, c);
    }
    return builder.append('\'').toString();
  }

  private static @NotNull String longLiteral(long l) {
    return l + "L";
  }

  private static @NotNull String floatLiteral(float f) {
    // TODO: Ensure that we do not loose precision.
    return f + "f";
  }

  private static @NotNull String doubleLiteral(double d) {
    // TODO: Ensure that we do not loose precision.
    return d + "d";
  }

  private static @NotNull String stringLiteral(@NotNull String str) {
    StringBuilder builder = new StringBuilder(str.length()).append('"');
    for (char c : str.toCharArray()) {
      if (c == '\'') {
        builder.append(c);
      }
      else {
        Characters.escape(builder, c);
      }
    }
    return builder.append('"').toString();
  }

  private static @NotNull String classLiteral(@NotNull TypeMirror type) {
    return type + ".class";
  }

  private static @NotNull String arrayLiteral(@NotNull List<? extends AnnotationValue> array) {
    if (array.isEmpty()) {
      return "{}";
    }
    else if (array.size() == 1) {
      return array.get(0).toString();
    }
    else {
      return array.stream()
          .map(Object::toString)
          .collect(Collectors.joining(", ", "{", "}"));
    }
  }
}
