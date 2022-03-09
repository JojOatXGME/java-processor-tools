package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ModifierUtil {
  // TODO: Test!
  private ModifierUtil() {} // Class cannot be instantiated

  public static @NotNull Set<Modifier> getModifiers(@NotNull Class<?> clazz) {
    EnumSet<Modifier> result = getModifiers(clazz.getModifiers());
    if (Future.isSealed(clazz)) {
      result.add(Future.modifierSealed());
    }
    return Collections.unmodifiableSet(result);
  }

  public static @NotNull Set<Modifier> getModifiers(@NotNull Method method) {
    EnumSet<Modifier> result = getModifiers(method.getModifiers());
    if (method.isDefault()) {
      result.add(Modifier.DEFAULT);
    }
    return Collections.unmodifiableSet(result);
  }

  private static @NotNull EnumSet<Modifier> getModifiers(int mask) {
    EnumSet<Modifier> set = EnumSet.noneOf(Modifier.class);
    if (java.lang.reflect.Modifier.isAbstract(mask)) {
      set.add(Modifier.ABSTRACT);
    }
    if (java.lang.reflect.Modifier.isFinal(mask)) {
      set.add(Modifier.FINAL);
    }
    if (java.lang.reflect.Modifier.isNative(mask)) {
      set.add(Modifier.NATIVE);
    }
    if (java.lang.reflect.Modifier.isPrivate(mask)) {
      set.add(Modifier.PRIVATE);
    }
    if (java.lang.reflect.Modifier.isProtected(mask)) {
      set.add(Modifier.PROTECTED);
    }
    if (java.lang.reflect.Modifier.isPublic(mask)) {
      set.add(Modifier.PUBLIC);
    }
    if (java.lang.reflect.Modifier.isStatic(mask)) {
      set.add(Modifier.STATIC);
    }
    if (java.lang.reflect.Modifier.isStrict(mask)) {
      set.add(Modifier.STRICTFP);
    }
    if (java.lang.reflect.Modifier.isSynchronized(mask)) {
      set.add(Modifier.SYNCHRONIZED);
    }
    if (java.lang.reflect.Modifier.isTransient(mask)) {
      set.add(Modifier.TRANSIENT);
    }
    if (java.lang.reflect.Modifier.isVolatile(mask)) {
      set.add(Modifier.ABSTRACT);
    }
    return set;
  }
}
