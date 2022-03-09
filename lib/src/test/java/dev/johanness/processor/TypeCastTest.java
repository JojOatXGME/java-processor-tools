package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TypeCastTest {
  private static final @NotNull List<Type<?>> TYPES = List.of(
      new Type<>(IntersectionType.class, TypeCast::asIntersectionType, TypeCast::toIntersectionType, TypeVisitor::visitIntersection),
      new Type<>(UnionType.class, TypeCast::asUnionType, TypeCast::toUnionType, TypeVisitor::visitUnion),
      new Type<>(ExecutableType.class, TypeCast::asExecutableType, TypeCast::toExecutableType, TypeVisitor::visitExecutable),
      new Type<>(NoType.class, TypeCast::asNoType, TypeCast::toNoType, TypeVisitor::visitNoType),
      new Type<>(PrimitiveType.class, TypeCast::asPrimitiveType, TypeCast::toPrimitiveType, TypeVisitor::visitPrimitive),
      new Type<>(WildcardType.class, TypeCast::asWildcardType, TypeCast::toWildcardType, TypeVisitor::visitWildcard),
      new Type<>(ArrayType.class, TypeCast::asArrayType, TypeCast::toArrayType, TypeVisitor::visitArray),
      new Type<>(NullType.class, TypeCast::asNullType, TypeCast::toNullType, TypeVisitor::visitNull),
      new Type<>(DeclaredType.class, TypeCast::asDeclaredType, TypeCast::toDeclaredType, TypeVisitor::visitDeclared),
      new Type<>(TypeVariable.class, TypeCast::asTypeVariable, TypeCast::toTypeVariable, TypeVisitor::visitTypeVariable),
      new Type<>(ReferenceType.class, TypeCast::asReferenceType, TypeCast::toReferenceType, null));

  @ParameterizedTest
  @ArgumentsSource(MethodProvider.class)
  void testNull(@NotNull MethodHolder arg) {
    assertNull(arg.method.convert(null));
  }

  private static final class MethodProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return TYPES.stream()
          .flatMap(type -> Stream.of(
              new MethodHolder("as" + type.clazz.getSimpleName(), type.asMethod),
              new MethodHolder("to" + type.clazz.getSimpleName(), type.toMethod)))
          .map(Arguments::of);
    }
  }

  private static final class MethodHolder {
    private final @NotNull String name;
    private final @NotNull ConversionMethod<?> method;

    private MethodHolder(@NotNull String name, @NotNull ConversionMethod<?> method) {
      this.name = name;
      this.method = method;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ConversionCaseProvider.class)
  <T extends TypeMirror> void testConversion(@NotNull ConversionCase<T> arg) {
    TypeMirror argument = createMock(arg.argumentType, arg.visitorMethod);
    switch (arg.expectedOutcome) {
      case SUCCESS:
        assertSame(argument, arg.method.convert(argument));
        break;
      case THROW:
        assertThrows(IllegalArgumentException.class, () -> arg.method.convert(argument));
        break;
      case NULL:
        assertNull(arg.method.convert(argument));
        break;
    }
  }

  private static final class ConversionCaseProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return TYPES.stream()
          .filter(source -> source.visitorMethod != null)
          .flatMap(source -> TYPES.stream()
              .flatMap(target -> casesOf(target, source)))
          .map(Arguments::of);
    }

    private <T extends TypeMirror> Stream<ConversionCase<T>> casesOf(@NotNull Type<?> target, @NotNull Type<T> source) {
      assert source.visitorMethod != null;
      boolean expectSuccess = target.clazz.isAssignableFrom(source.clazz);
      return Stream.of(
          new ConversionCase<>(
              "as" + target.clazz.getSimpleName() + '(' + source.clazz.getSimpleName() + ')',
              target.asMethod,
              source.clazz, source.visitorMethod,
              expectSuccess ? ExpectedOutcome.SUCCESS : ExpectedOutcome.NULL),
          new ConversionCase<>(
              "to" + target.clazz.getSimpleName() + '(' + source.clazz.getSimpleName() + ')',
              target.toMethod,
              source.clazz, source.visitorMethod,
              expectSuccess ? ExpectedOutcome.SUCCESS : ExpectedOutcome.THROW));
    }
  }

  private static final class ConversionCase<T extends TypeMirror> {
    private final @NotNull String name;
    private final @NotNull ConversionMethod<?> method;
    private final @NotNull Class<T> argumentType;
    private final @NotNull VisitorMethod<T> visitorMethod;
    private final @NotNull ExpectedOutcome expectedOutcome;

    private ConversionCase(
        @NotNull String name,
        @NotNull ConversionMethod<?> method,
        @NotNull Class<T> argumentType,
        @NotNull VisitorMethod<T> visitorMethod,
        @NotNull ExpectedOutcome expectedOutcome)
    {
      this.name = name;
      this.method = method;
      this.argumentType = argumentType;
      this.visitorMethod = visitorMethod;
      this.expectedOutcome = expectedOutcome;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private enum ExpectedOutcome {
    SUCCESS, THROW, NULL
  }

  private static <T extends TypeMirror> @NotNull T createMock(@NotNull Class<T> clazz, @NotNull VisitorMethod<T> visitorMethod) {
    T mock = Mockito.mock(clazz);
    Mockito.when(mock.accept(Mockito.any(), Mockito.any())).then(
        invocation -> visitorMethod.visit(
            invocation.getArgument(0),
            mock,
            invocation.getArgument(1)));
    Mockito.verifyNoMoreInteractions(mock);
    return mock;
  }

  private static final class Type<T extends TypeMirror> {
    private final @NotNull Class<T> clazz;
    private final @NotNull ConversionMethod<T> asMethod;
    private final @NotNull ConversionMethod<T> toMethod;
    private final @Nullable VisitorMethod<T> visitorMethod;

    private Type(
        @NotNull Class<T> clazz,
        @NotNull ConversionMethod<T> asMethod,
        @NotNull ConversionMethod<T> toMethod,
        @Nullable VisitorMethod<T> visitorMethod)
    {
      this.clazz = clazz;
      this.toMethod = toMethod;
      this.asMethod = asMethod;
      this.visitorMethod = visitorMethod;
    }
  }

  @FunctionalInterface
  private interface ConversionMethod<T extends TypeMirror> {
    T convert(TypeMirror element);
  }

  @FunctionalInterface
  private interface VisitorMethod<T extends TypeMirror> {
    <R, P> R visit(TypeVisitor<R, P> visitor, T element, P p);
  }
}
