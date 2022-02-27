package dev.johanness.processor;

import dev.johanness.processor._types.SomeEnum;
import dev.johanness.processor._types.TopLevelAnnotation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ValueTypeTest {

  private static final AnnotationTypeMock ANNOTATION_TYPE_1 = new AnnotationTypeMock(TopLevelAnnotation.class);
  private static final AnnotationTypeMock ANNOTATION_TYPE_2 = new AnnotationTypeMock(TopLevelAnnotation.NestedAnnotation.class);
  private static final AnnotationValue SIMPLE_MOCKED_VALUE = Mockito.mock(AnnotationValue.class);
  private static final TypeMirror SIMPLE_MOCKED_TYPE = Mockito.mock(TypeMirror.class);
  private static final VariableElement SIMPLE_MOCKED_VARIABLE = Mockito.mock(VariableElement.class);
  private static final AnnotationMirror SIMPLE_MOCKED_MIRROR = new AnnotationMirrorMock(TopLevelAnnotation.class);

  @ParameterizedTest
  @EnumSource(ConvertCase.class)
  void testConvert(@NotNull ConvertCase arg) {
    assertEquals(arg.expectedResult, arg.type.convert(arg.value));
  }

  private enum ConvertCase {
    UNTYPED(ValueType.untyped(), SIMPLE_MOCKED_VALUE, SIMPLE_MOCKED_VALUE),
    BOOLEAN_TRUE(ValueType.bool(), v -> v.visitBoolean(true, null), true),
    BOOLEAN_FALSE(ValueType.bool(), v -> v.visitBoolean(false, null), false),
    BYTE(ValueType.byte_(), v -> v.visitByte((byte) 42, null), (byte) 42),
    SHORT_FROM_BYTE(ValueType.short_(), v -> v.visitByte((byte) 42, null), (short) 42),
    SHORT_FROM_SHORT(ValueType.short_(), v -> v.visitShort((short) 1337, null), (short) 1337),
    INTEGER_FROM_BYTE(ValueType.integer(), v -> v.visitByte((byte) 42, null), 42),
    INTEGER_FROM_SHORT(ValueType.integer(), v -> v.visitShort((short) 1337, null), 1337),
    INTEGER_FROM_INT(ValueType.integer(), v -> v.visitInt(Integer.MIN_VALUE, null), Integer.MIN_VALUE),
    LONG_FROM_BYTE(ValueType.long_(), v -> v.visitByte((byte) 42, null), 42L),
    LONG_FROM_SHORT(ValueType.long_(), v -> v.visitShort((short) 1337, null), 1337L),
    LONG_FROM_INT(ValueType.long_(), v -> v.visitInt(Integer.MIN_VALUE, null), (long) Integer.MIN_VALUE),
    LONG_FROM_LONG(ValueType.long_(), v -> v.visitLong(Long.MAX_VALUE, null), Long.MAX_VALUE),
    FLOAT(ValueType.float_(), v -> v.visitFloat(-1.1f, null), -1.1f),
    DOUBLE_FROM_FLOAT(ValueType.double_(), v -> v.visitFloat(-1.1f, null), (double) -1.1f),
    DOUBLE_FROM_DOUBLE(ValueType.double_(), v -> v.visitDouble(Double.MIN_VALUE, null), Double.MIN_VALUE),
    CHARACTER(ValueType.character(), v -> v.visitChar('x', null), 'x'),
    STRING(ValueType.string(), v -> v.visitString("<$str$>", null), "<$str$>"),
    CLASS(ValueType.class_(), v -> v.visitType(SIMPLE_MOCKED_TYPE, null), SIMPLE_MOCKED_TYPE),
    UNTYPED_ENUM(ValueType.enum_(), v -> v.visitEnumConstant(SIMPLE_MOCKED_VARIABLE, null), SIMPLE_MOCKED_VARIABLE),
    UNTYPED_ANNOTATION(ValueType.annotation(), v -> v.visitAnnotation(SIMPLE_MOCKED_MIRROR, null), SIMPLE_MOCKED_MIRROR),
    UNTYPED_ARRAY(ValueType.array(), v -> v.visitArray(List.of(SIMPLE_MOCKED_VALUE), null), List.of(SIMPLE_MOCKED_VALUE)),
    UNTYPED_ARRAY_EMPTY(ValueType.array(), v -> v.visitArray(List.of(), null), List.of()),
    // TODO: Add tests for remaining case; i.e. enum_(Class)
    ANNOTATION(ValueType.annotation(ANNOTATION_TYPE_1), v -> v.visitAnnotation(SIMPLE_MOCKED_MIRROR, null), new MockProxy(SIMPLE_MOCKED_MIRROR)),
    TYPED_ARRAY(ValueType.array(ValueType.integer()), v -> v.visitArray(List.of(value(u -> u.visitInt(42, null)), value(u -> u.visitInt(1337, null))), null), List.of(42, 1337)),
    TYPED_ARRAY_EMPTY(ValueType.array(ValueType.integer()), v -> v.visitArray(List.of(), null), List.of()),

    ;

    private final @NotNull ValueType<?> type;
    private final @NotNull AnnotationValue value;
    private final @NotNull Object expectedResult;

    ConvertCase(
        @NotNull ValueType<?> type,
        @NotNull AnnotationValue value,
        @NotNull Object expectedResult)
    {
      this.type = type;
      this.value = value;
      this.expectedResult = expectedResult;
    }

    ConvertCase(
        @NotNull ValueType<?> type,
        @NotNull Consumer<AnnotationValueVisitor<Void, Void>> value,
        @NotNull Object expectedResult)
    {
      this.type = type;
      this.value = value(value);
      this.expectedResult = expectedResult;
    }
  }

  @ParameterizedTest
  @EnumSource(BasicCase.class)
  void testToString(@NotNull BasicCase arg) {
    ValueType<?> type = arg.create();
    assertEquals(arg.expectedString, type.toString());
  }

  @ParameterizedTest
  @EnumSource(BasicCase.class)
  @SuppressWarnings("SimplifiableAssertion")
  void testEquals(@NotNull BasicCase arg) {
    ValueType<?> type = arg.create();
    assertAll(Arrays.stream(BasicCase.values()).map(other -> () -> {
      if (other == arg) {
        assertTrue(type.equals(other.create()), other.toString());
      }
      else {
        assertFalse(type.equals(other.create()), other.toString());
      }
    }));
  }

  private enum BasicCase {
    UNTYPED(ValueType::untyped, "NoneType()"),
    BOOLEAN(ValueType::bool, "SimpleType(boolean)"),
    BYTE(ValueType::byte_, "SimpleType(byte)"),
    SHORT(ValueType::short_, "SimpleType(short)"),
    INTEGER(ValueType::integer, "SimpleType(integer)"),
    LONG(ValueType::long_, "SimpleType(long)"),
    FLOAT(ValueType::float_, "SimpleType(float)"),
    DOUBLE(ValueType::double_, "SimpleType(double)"),
    CHARACTER(ValueType::character, "SimpleType(character)"),
    STRING(ValueType::string, "SimpleType(string)"),
    CLASS(ValueType::class_, "SimpleType(class)"),

    UNTYPED_ENUM(ValueType::enum_, "SimpleType(enum constant)"),
    UNTYPED_ANNOTATION(ValueType::annotation, "SimpleType(annotation)"),
    UNTYPED_ARRAY(ValueType::array, "SimpleType(array)"),

    TYPED_ENUM1(
        () -> ValueType.enum_(SomeEnum.class),
        "EnumType(" + SomeEnum.class + ")"),
    TYPED_ENUM2(
        () -> ValueType.enum_(BasicCase.class),
        "EnumType(" + BasicCase.class + ")"),
    TYPED_ANNOTATION1(
        () -> ValueType.annotation(ANNOTATION_TYPE_1),
        "AnnotationType(" + ANNOTATION_TYPE_1.nameWithModule() + ")"),
    TYPED_ANNOTATION2(
        () -> ValueType.annotation(ANNOTATION_TYPE_2),
        "AnnotationType(" + ANNOTATION_TYPE_2.nameWithModule() + ")"),
    TYPED_ARRAY(
        () -> ValueType.array(ValueType.untyped()),
        "ArrayType(NoneType())"),
    TYPED_ARRAY_WITH_SIMPLE_TYPE(
        () -> ValueType.array(ValueType.string()),
        "ArrayType(string)"),
    TYPED_ARRAY_WITH_ENUM_TYPE(
        () -> ValueType.array(ValueType.enum_(SomeEnum.class)),
        "ArrayType(" + SomeEnum.class + ")"),
    TYPED_ARRAY_WITH_ANNOTATION_TYPE(
        () -> ValueType.array(ValueType.annotation(ANNOTATION_TYPE_1)),
        "ArrayType(" + ANNOTATION_TYPE_1.nameWithModule() + ")"),

    ;

    private final @NotNull Supplier<ValueType<?>> factory;
    private final @NotNull String expectedString;

    BasicCase(@NotNull Supplier<ValueType<?>> factory, @NotNull String expectedString) {
      this.factory = factory;
      this.expectedString = expectedString;
    }

    private @NotNull ValueType<?> create() {
      return factory.get();
    }
  }

  /**
   * Creates a mock for an {@link AnnotationValue}.
   */
  @SuppressWarnings("unchecked")
  private static @NotNull AnnotationValue value(Consumer<AnnotationValueVisitor<Void, Void>> type) {
    Method[] visitorMethod = new Method[1];
    Object[] visitorArgument = new Object[1];
    type.accept((AnnotationValueVisitor<Void, Void>) Proxy.newProxyInstance(
        ValueTypeTest.class.getClassLoader(),
        new Class[] {AnnotationValueVisitor.class},
        (proxy, method, args) -> {
          assert visitorMethod[0] == null;
          assert method.getName().startsWith("visit");
          visitorMethod[0] = method;
          visitorArgument[0] = args[0];
          //noinspection SuspiciousInvocationHandlerImplementation
          return null;
        }));
    return new AnnotationValue() {
      @Override
      public Object getValue() {
        throw new UnsupportedOperationException("getValue()");
      }

      @Override
      public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
        try {
          return (R) visitorMethod[0].invoke(v, visitorArgument[0], p);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
    };
  }

  private static final class AnnotationTypeMock extends AnnotationType<MockProxy> {
    private AnnotationTypeMock(@NotNull Class<? extends Annotation> clazz) {
      super(clazz);
    }

    @Override
    protected @NotNull MockProxy createProxy(@NotNull AnnotationMirror mirror) {
      return new MockProxy(mirror);
    }
  }

  private static final class MockProxy extends AnnotationProxy {
    private MockProxy(@NotNull AnnotationMirror mirror) {
      super(mirror);
    }
  }

  private static final class AnnotationMirrorMock implements AnnotationMirror {
    private final @NotNull DeclaredType type;

    private AnnotationMirrorMock(@NotNull Class<? extends Annotation> clazz) {
      this.type = Mockito.mock(DeclaredType.class);
      TypeElement element = Mockito.mock(TypeElement.class);
      Name name = Mockito.mock(Name.class);
      Mockito.when(type.asElement()).thenReturn(element);
      Mockito.when(name.toString()).thenReturn(clazz.getCanonicalName());
      Mockito.when(name.contentEquals(Mockito.any())).thenReturn(false);
      Mockito.when(name.contentEquals(clazz.getCanonicalName())).thenReturn(true);
      Mockito.when(element.getQualifiedName()).thenReturn(name);
      Mockito.when(element.accept(Mockito.any(), Mockito.any())).then(invocation -> {
        ElementVisitor<Object, Object> visitor = invocation.getArgument(0);
        return visitor.visitType(element, invocation.getArgument(1));
      });
    }

    @Override
    public DeclaredType getAnnotationType() {
      return type;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
      throw new UnsupportedOperationException("getElementValues()");
    }
  }
}
