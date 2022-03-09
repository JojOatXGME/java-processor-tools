package dev.johanness.processor.test._internal;

import dev.johanness.processor.ValueType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public final class AnnotationProxy implements InvocationHandler {
  private static final @NotNull Method EQUALS_METHOD = getMethod(Object.class, "equals", Object.class);
  private static final @NotNull Method HASHCODE_METHOD = getMethod(Object.class, "hashCode");
  private static final @NotNull Method TOSTRING_METHOD = getMethod(Object.class, "toString");
  private static final @NotNull Method TYPE_METHOD = getMethod(Annotation.class, "annotationType");

  private final @NotNull Class<?> clazz;
  private final @NotNull AnnotationWrapper annotation;

  private AnnotationProxy(@NotNull Class<?> clazz, @NotNull AnnotationMirror mirror) {
    this.clazz = clazz;
    this.annotation = new AnnotationWrapper(mirror);
  }

  public static <A extends Annotation> @NotNull A create(@NotNull Class<A> clazz, @NotNull AnnotationMirror mirror) {
    AnnotationProxy handler = new AnnotationProxy(clazz, mirror);
    Object proxy = Proxy.newProxyInstance(
        AnnotationProxy.class.getClassLoader(),
        new Class[] {Annotation.class, clazz},
        handler);
    return clazz.cast(proxy);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    if (method.equals(EQUALS_METHOD) || method.equals(HASHCODE_METHOD)) {
      throw new UnsupportedOperationException("equals() and hashCode() not yet supported");
    }
    else if (method.equals(TOSTRING_METHOD)) {
      return annotation.toString();
    }
    else if (method.equals(TYPE_METHOD)) {
      return clazz;
    }
    else if (method.getReturnType().equals(Class.class)) {
      TypeMirror result = annotation.readValue(method.getName(), ValueType.class_());
      throw new MirroredTypeException(result);
    }
    else if (method.getReturnType().equals(Class[].class)) {
      List<TypeMirror> result = annotation.readValue(method.getName(), ValueType.array(ValueType.class_()));
      throw new MirroredTypesException(result);
    }
    else {
      return annotation.readValue(method.getName(), ValueType.untyped());
    }
  }

  private static @NotNull Method getMethod(@NotNull Class<?> owner, @NotNull String name, @NotNull Class<?> @NotNull ... params) {
    try {
      return owner.getMethod(name, params);
    }
    catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  private static final class AnnotationWrapper extends dev.johanness.processor.AnnotationProxy {
    private AnnotationWrapper(@NotNull AnnotationMirror mirror) {
      super(mirror);
    }

    private <T> T readValue(@NotNull String name, @NotNull ValueType<T> type) {
      return readValue(null, name, type);
    }
  }
}
