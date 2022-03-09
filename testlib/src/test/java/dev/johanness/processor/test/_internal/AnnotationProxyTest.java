package dev.johanness.processor.test._internal;

import dev.johanness.processor.test.mock.annotation.AnnotationMirrorMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public final class AnnotationProxyTest {

  @ParameterizedTest
  @ValueSource(classes = {AnnotationWithAllTypes.class, AnnotationWithMissingValue.class})
  <A extends Annotation> void testAnnotationType(@NotNull Class<A> annotationType) {
    A proxy = AnnotationProxy.create(
        annotationType,
        new AnnotationMirrorMock<>().type(annotationType));
    assertEquals(annotationType, proxy.annotationType());
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void testWorksWithMissingValues() {
    AnnotationWithMissingValue proxy = AnnotationProxy.create(
        AnnotationWithMissingValue.class,
        new AnnotationMirrorMock<>()
            .type(AnnotationWithMissingValue.class)
            .set(AnnotationWithMissingValue::withValue, "EXPECTED"));
    assertEquals("DEFAULT", proxy.withDefault());
    assertEquals("EXPECTED", proxy.withValue());
    assertThrows(RuntimeException.class, proxy::withoutValue);
  }

  @ParameterizedTest
  @ArgumentsSource(AnnotationMethodSource.class)
  void testValueMethods(@NotNull Type arg) throws ReflectiveOperationException {
    AnnotationWithAllTypes proxy = AnnotationProxy.create(
        AnnotationWithAllTypes.class,
        new AnnotationMirrorMock<>().type(AnnotationWithAllTypes.class));
    if (arg.method.getReturnType() == Class.class) {
      try {
        arg.method.invoke(proxy);
        fail("Method must throw a MirroredTypeException");
      }
      catch (InvocationTargetException e) {
        if (e.getCause() instanceof MirroredTypeException) {
          // TODO: ...
        }
        else {
          throw e;
        }
      }
    }
    else if (arg.method.getReturnType() == Class[].class) {
      try {
        arg.method.invoke(proxy);
        fail("Method must throw a MirroredTypesException");
      }
      catch (InvocationTargetException e) {
        if (e.getCause() instanceof MirroredTypesException) {
          // TODO: ...
        }
        else {
          throw e;
        }
      }
    }
    else {
      assertEquals(arg.method.getDefaultValue(), arg.method.invoke(proxy));
    }
  }

  private static final class Type {
    private final @NotNull Method method;

    private Type(@NotNull Method method) {
      this.method = method;
    }

    @Override
    public String toString() {
      return method.getReturnType().getSimpleName() + ' ' + method.getName() + "()";
    }
  }

  private static final class AnnotationMethodSource implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Arrays.stream(AnnotationWithAllTypes.class.getDeclaredMethods())
          .map(Type::new)
          .map(Arguments::of);
    }
  }

  private @interface AnnotationWithMissingValue {
    String withDefault() default "DEFAULT";

    String withValue();

    @SuppressWarnings("UnusedReturnValue") String withoutValue();
  }

  @SuppressWarnings("unused") // Called by reflection
  private @interface AnnotationWithAllTypes {

    short short_() default 42;

    short[] shorts() default 42;

    byte byte_() default 42;

    byte[] bytes() default 42;

    int integer() default 42;

    int[] integers() default 42;

    long long_() default 42L;

    long[] longs() default {Long.MIN_VALUE, Long.MAX_VALUE};

    float float_() default 42f;

    float[] floats() default {-1f, Float.MAX_VALUE};

    double double_() default 42d;

    double[] doubles() default {-1d, Double.MIN_VALUE};

    char character() default '\0';

    char[] characters() default {'2', 'A'};

    boolean boolean_() default true;

    boolean[] booleans() default {};

    String string() default "42";

    String[] strings() default {"", ""};

    Class<?> class_() default Void.class;

    Class<?>[] classes() default {Object.class, Void.class};
  }
}
