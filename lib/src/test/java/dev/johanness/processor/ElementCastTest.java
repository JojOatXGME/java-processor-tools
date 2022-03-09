package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.JRE.JAVA_16;

final class ElementCastTest {
  private static final @NotNull List<Type<?>> TYPES = List.of(
      new Type<>(ModuleElement.class, ElementCast::toModuleElement, ElementCast::asModuleElement, ElementVisitor::visitModule),
      new Type<>(PackageElement.class, ElementCast::toPackageElement, ElementCast::asPackageElement, ElementVisitor::visitPackage),
      new Type<>(ExecutableElement.class, ElementCast::toExecutableElement, ElementCast::asExecutableElement, ElementVisitor::visitExecutable),
      new Type<>(VariableElement.class, ElementCast::toVariableElement, ElementCast::asVariableElement, ElementVisitor::visitVariable),
      new Type<>(TypeElement.class, ElementCast::toTypeElement, ElementCast::asTypeElement, ElementVisitor::visitType),
      new Type<>(TypeParameterElement.class, ElementCast::toTypeParameterElement, ElementCast::asTypeParameterElement, ElementVisitor::visitTypeParameter),
      new Type<>(QualifiedNameable.class, ElementCast::toQualifiedNameable, ElementCast::asQualifiedNameable, null),
      new Type<>(Parameterizable.class, ElementCast::toParameterizable, ElementCast::asParameterizable, null));

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
  <T extends Element> void testConversion(@NotNull ConversionCase<T> arg) {
    Element argument = createMock(arg.argumentType, arg.visitorMethod);
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

    private <T extends Element> Stream<ConversionCase<T>> casesOf(@NotNull Type<?> target, @NotNull Type<T> source) {
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

  private static final class ConversionCase<T extends Element> {
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

  @Nested
  @EnabledForJreRange(min = JAVA_16)
  @DisplayName("RecordComponentElement (Java 16 and above)")
  final class RecordComponentElementTest {
    @Test
    void testNullAsRecordComponentElement() {
      assertNull(ElementCast.asRecordComponentElement(null));
    }

    @Test
    void testNullToRecordComponentElement() {
      assertNull(ElementCast.toRecordComponentElement(null));
    }

    @Test
    void testConversionSuccess() {
      Element argument = createMock(RecordComponentElement.class, ElementVisitor::visitRecordComponent);
      assertSame(argument, ElementCast.asRecordComponentElement(argument));
      assertSame(argument, ElementCast.toRecordComponentElement(argument));
    }

    @Test
    void testConversionFailure() {
      Element argument = createMock(TypeElement.class, ElementVisitor::visitType);
      assertNull(ElementCast.asRecordComponentElement(argument));
      assertThrows(IllegalArgumentException.class, () -> ElementCast.toRecordComponentElement(argument));
    }
  }

  private static <T extends Element> @NotNull T createMock(@NotNull Class<T> clazz, @NotNull VisitorMethod<T> visitorMethod) {
    T mock = Mockito.mock(clazz);
    Mockito.when(mock.accept(Mockito.any(), Mockito.any())).then(
        invocation -> visitorMethod.visit(
            invocation.getArgument(0),
            mock,
            invocation.getArgument(1)));
    Mockito.verifyNoMoreInteractions(mock);
    return mock;
  }

  private static final class Type<T extends Element> {
    private final @NotNull Class<T> clazz;
    private final @NotNull ConversionMethod<T> toMethod;
    private final @NotNull ConversionMethod<T> asMethod;
    private final @Nullable VisitorMethod<T> visitorMethod;

    private Type(
        @NotNull Class<T> clazz,
        @NotNull ConversionMethod<T> toMethod,
        @NotNull ConversionMethod<T> asMethod,
        @Nullable VisitorMethod<T> visitorMethod)
    {
      this.clazz = clazz;
      this.toMethod = toMethod;
      this.asMethod = asMethod;
      this.visitorMethod = visitorMethod;
    }
  }

  @FunctionalInterface
  private interface ConversionMethod<T extends Element> {
    T convert(Element element);
  }

  @FunctionalInterface
  private interface VisitorMethod<T extends Element> {
    <R, P> R visit(ElementVisitor<R, P> visitor, T element, P p);
  }
}
