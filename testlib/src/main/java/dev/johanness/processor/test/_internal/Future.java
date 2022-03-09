package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

public final class Future {
  private static final @Nullable Method IS_RECORD_METHOD = getMethod(Class.class, "isRecord");
  private static final @Nullable Method IS_SEALED_METHOD = getMethod(Class.class, "isSealed");
  private static final @Nullable Modifier MODIFIER_SEALED = getEnumConstant(Modifier::valueOf, "SEALED");
  private static final @Nullable ElementKind KIND_RECORD = getEnumConstant(ElementKind::valueOf, "RECORD");

  private Future() {} // Cannot be instantiated

  public static boolean isRecord(@NotNull Class<?> clazz) {
    try {
      return IS_RECORD_METHOD != null && (Boolean) IS_RECORD_METHOD.invoke(clazz);
    }
    catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean isRecord(@NotNull ElementKind kind) {
    return Objects.requireNonNull(kind) == KIND_RECORD;
  }

  public static boolean isSealed(@NotNull Class<?> clazz) {
    try {
      return IS_SEALED_METHOD != null && (Boolean) IS_SEALED_METHOD.invoke(clazz);
    }
    catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  public static @NotNull Modifier modifierSealed() {
    return Objects.requireNonNull(MODIFIER_SEALED);
  }

  private static <E> @Nullable E getEnumConstant(@NotNull Function<String, E> getter, @NotNull String name) {
    try {
      return getter.apply(name);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static @Nullable Method getMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... params) {
    try {
      return clazz.getMethod(name, params);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }
}
