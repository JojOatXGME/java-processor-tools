package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

public final class SafeValue {
  private static final @NotNull Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
      boolean.class, false,
      char.class, '\0',
      byte.class, (byte) 0,
      short.class, (short) 0,
      int.class, 0,
      long.class, 0L,
      float.class, 0f,
      double.class, 0d);

  private SafeValue() {} // Cannot be instantiated

  public static @Nullable Object safeReturn(@NotNull Method method) {
    return safeValue(method.getReturnType());
  }

  private static @Nullable Object safeValue(@NotNull Class<?> type) {
    if (type.isPrimitive()) {
      Object value = PRIMITIVE_DEFAULTS.get(type);
      if (value == null) {
        throw new IllegalStateException("Unknown primitive type: " + type);
      }
      return value;
    }
    else {
      return null;
    }
  }
}
