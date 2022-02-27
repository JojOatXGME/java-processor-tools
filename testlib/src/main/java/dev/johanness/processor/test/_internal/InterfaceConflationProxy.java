package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.DeclaredType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public final class InterfaceConflationProxy {
  private static final @NotNull Class<?>[] ALL_POSSIBLE_INTERFACES = {
      AnnotationMirror.class,
      AnnotationValue.class,
      DeclaredType.class,
  };

  private final @NotNull Object implementation;
  private final @NotNull Class<?> restriction;

  private InterfaceConflationProxy(@NotNull Object implementation, @NotNull Class<?> restriction) {
    this.implementation = implementation;
    this.restriction = restriction;
  }

  public static <T> @NotNull T create(@NotNull T implementation, @NotNull Class<T> restriction) {
    if (!Arrays.asList(ALL_POSSIBLE_INTERFACES).contains(restriction)) {
      throw new IllegalArgumentException("Type not supported by InterfaceConflationProxy: " + restriction);
    }
    InterfaceConflationProxy handler = new InterfaceConflationProxy(implementation, restriction);
    Object proxy = Proxy.newProxyInstance(
        InterfaceConflationProxy.class.getClassLoader(),
        ALL_POSSIBLE_INTERFACES,
        handler::invoke);
    return restriction.cast(proxy);
  }

  private Object invoke(Object proxy, Method method, Object[] args) {
    // TODO: Remember hashCode equals and toString
    return null;
  }
}
