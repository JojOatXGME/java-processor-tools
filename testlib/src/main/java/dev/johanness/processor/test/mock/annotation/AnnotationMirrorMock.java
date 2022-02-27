package dev.johanness.processor.test.mock.annotation;

import dev.johanness.processor.test._internal.AnnotationValueMap;
import dev.johanness.processor.test._internal.SafeValue;
import dev.johanness.processor.test.mock.type.DeclaredTypeMock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationMirrorMock<A extends Annotation> implements AnnotationMirror {
  // TODO: Test this class
  private static final @NotNull AnnotationMirrorMock.UnsetException UNSET_EXCEPTION = new UnsetException();
  private static final @NotNull StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  private @Nullable Class<A> clazz;
  private @Nullable DeclaredType type;
  private @Nullable AnnotationValueMap values;

  public AnnotationMirrorMock() {
  }

  //region Static methods

  @Contract("-> fail")
  public static <T> T unset() {
    if (STACK_WALKER.walk(stream -> stream.noneMatch(frame -> frame.getDeclaringClass() == AnnotationMirrorMock.class))) {
      throw new IllegalStateException(
          "AnnotationMirrorMock.unset() must only be used by annotations given to AnnotationMirrorMock.set(Annotation)");
    }
    throw UNSET_EXCEPTION;
  }

  //endregion
  //region Builder methods

  @Contract(value = "_ -> this", mutates = "this")
  public AnnotationMirrorMock<?> type(@NotNull DeclaredType type) {
    if (this.type != null) {
      throw new IllegalStateException("Type cannot be changed");
    }
    else {
      assert this.clazz == null;
      this.type = type;
      this.values = new AnnotationValueMap(type);
      return this;
    }
  }

  @SuppressWarnings("unchecked")
  @Contract(value = "_ -> this", mutates = "this")
  public <T extends Annotation> AnnotationMirrorMock<T> type(@NotNull Class<T> type) {
    if (this.type != null) {
      throw new IllegalStateException("Type cannot be changed");
    }
    else if (!type.isAnnotation()) {
      throw new IllegalArgumentException(type + " is not an annotation");
    }
    else {
      assert this.clazz == null;
      AnnotationMirrorMock<T> thiz = (AnnotationMirrorMock<T>) this;
      thiz.clazz = type;
      thiz.type = new DeclaredTypeMock(type);
      thiz.values = new AnnotationValueMap(this.type);
      return thiz;
    }
  }

  @Contract(value = "_ -> this", mutates = "this")
  public AnnotationMirrorMock<A> set(@NotNull A annotation) {
    if (type != null && clazz == null) {
      throw new IllegalStateException(
          "AnnotationMirrorMock.set(Annotation) is not supported in combination " +
          "with AnnotationMirrorMock.type(DeclaredType)");
    }
    if (type != null && !clazz.isInstance(annotation)) {
      throw new IllegalStateException(String.format(
          "%s does not implement %s",
          annotation.getClass(), clazz));
    }
    else if (type == null) {
      assert clazz == null;
      Set<Class<? extends Annotation>> candidates = findAnnotationClasses(annotation.getClass())
          .collect(Collectors.toUnmodifiableSet());
      if (candidates.isEmpty()) {
        throw new IllegalStateException(String.format(
            "%s does not implement any annotation interface",
            annotation.getClass()));
      }
      else if (candidates.size() > 1) {
        throw new IllegalStateException(String.format(
            "Ambiguous annotation type for %s: %s",
            clazz, candidates));
      }
      else {
        type(candidates.iterator().next());
        assert clazz != null;
      }
    }
    for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
      try {
        set(method.getName(), method.invoke(annotation));
      }
      catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
      catch (InvocationTargetException e) {
        if (e.getCause() == UNSET_EXCEPTION) {
          set(method.getName(), null);
        }
        else {
          throw new IllegalStateException(e.getCause());
        }
      }
    }
    return this;
  }

  private static @NotNull Stream<Class<? extends Annotation>> findAnnotationClasses(@NotNull Class<?> clazz) {
    if (clazz == Object.class) {
      return Stream.empty();
    }
    else if (clazz.isAnnotation()) {
      return Stream.of(clazz.asSubclass(Annotation.class));
    }
    else {
      return Stream.concat(Arrays.stream(clazz.getInterfaces()), Stream.of(clazz.getSuperclass()))
          .flatMap(AnnotationMirrorMock::findAnnotationClasses);
    }
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public AnnotationMirrorMock<A> set(@NotNull String method, @Nullable Object value) {
    return set(method, value == null ? null : new AnnotationValueMock(value));
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public AnnotationMirrorMock<A> set(@NotNull String method, @Nullable AnnotationValue value) {
    if (type == null) {
      throw new IllegalStateException("AnnotationMirrorMock not yet initialized");
    }
    assert values != null;
    values.put(method, value);
    return this;
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public <T> AnnotationMirrorMock<A> set(@NotNull Method<A, T> method, @Nullable T value) {
    if (clazz == null) {
      throw new IllegalStateException(
          "AnnotationMirrorMock.set(Method, Object) is only supported if the " +
          "type was given by class. Use Mocks.annotation(Class) or AnnotationMirrorMock.type(Class) " +
          "before you can use this method.");
    }
    String[] name = new String[1];
    method.call(clazz.cast(Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] {clazz},
        (proxy, method1, args) -> {
          name[0] = method1.getName();
          return SafeValue.safeReturn(method1);
        })));
    return set(name[0], value);
  }

  //endregion
  //region Interface methods

  @Override
  public DeclaredType getAnnotationType() {
    if (type == null) {
      throw new IllegalStateException("AnnotationMirrorMock not yet initialized");
    }
    return type;
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
    if (type == null) {
      throw new IllegalStateException("AnnotationMirrorMock not yet initialized");
    }
    assert values != null;
    return values.asUnmodifiableMap();
  }

  @Override
  public String toString() {
    if (type == null) {
      throw new IllegalStateException("{uninitialized AnnotationMirrorMock}");
    }
    else {
      assert values != null;
      return "@" + type + values.asUnmodifiableMap().entrySet().stream()
          .map(entry -> entry.getKey().getSimpleName() + " = " + entry.getValue())
          .collect(Collectors.joining(", ", "(", ")"));
    }
  }

  //endregion
  //region Nested classes

  @FunctionalInterface
  public interface Method<A, T> {
    @SuppressWarnings("UnusedReturnValue")
    T call(A annotation);
  }

  private static final class UnsetException extends RuntimeException {
    private UnsetException() {
      super(null, null, false, false);
    }
  }

  //endregion
}
