package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("SuspiciousInvocationHandlerImplementation")
final class InterfaceConflationProxyTest {

  @ParameterizedTest
  @ArgumentsSource(InterfaceSource.class)
  <T> void testGetImplementationOnImpl(@NotNull Interface<T> arg) {
    T impl = arg.createImplementation((__1, __2, __3) -> fail("No methods should be called"));
    assertSame(impl, InterfaceConflationProxy.getImplementation(impl, impl.getClass()));
  }

  @ParameterizedTest
  @ArgumentsSource(InterfaceSource.class)
  <T> void testGetImplementationOnProxy(@NotNull Interface<T> arg) {
    T impl = arg.createImplementation((__1, __2, __3) -> fail("No methods should be called"));
    T proxy = arg.createProxyInstance(impl);
    assertSame(impl, InterfaceConflationProxy.getImplementation(proxy, impl.getClass()));
    for (int i = 0; i < 4; ++i) {
      proxy = arg.createProxyInstance(proxy);
      assertSame(impl, InterfaceConflationProxy.getImplementation(proxy, impl.getClass()));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(InterfaceMethodSource.class)
  <T> void testCanCallAllMethods(@NotNull InterfaceWithMethod<T> arg) throws ReflectiveOperationException {
    AtomicBoolean calledFlag = new AtomicBoolean(false);
    T proxy = arg.type.createProxyInstance((__1, __2, __3) -> {
      calledFlag.set(true);
      return SafeValue.safeReturn(arg.method);
    });
    arg.method.invoke(proxy, SafeValue.safeArguments(arg.method));
    assertTrue(calledFlag.get());
  }

  @ParameterizedTest
  @ArgumentsSource(InterfaceMethodSource.class)
  <T> void testExceptionsAreNotWrapped(@NotNull InterfaceWithMethod<T> arg) throws IllegalAccessException {
    Throwable expectedThrowable = new IllegalStateException("EXPECTED");
    T proxy = arg.type.createProxyInstance((__1, __2, __3) -> {
      throw expectedThrowable;
    });
    try {
      arg.method.invoke(proxy, SafeValue.safeArguments(arg.method));
      fail("Exception is swallowed");
    }
    catch (InvocationTargetException e) {
      assertSame(expectedThrowable, e.getCause());
    }
  }

  private static final class InterfaceSource implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return streamInterfaceTypes()
          .map(Arguments::of);
    }
  }

  private static final class InterfaceMethodSource implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return streamInterfaceTypes()
          .flatMap(type -> Arrays.stream(type.interfaceType.getMethods())
              .map(method -> new InterfaceWithMethod<>(type, method)))
          .map(Arguments::of);
    }
  }

  private static @NotNull Stream<Interface<?>> streamInterfaceTypes() {
    return Stream.of(
        new Interface<>(AnnotationMirror.class, (impl, __) -> InterfaceConflationProxy.create(impl)),
        new Interface<>(AnnotationValue.class, (impl, __) -> InterfaceConflationProxy.create(impl)),
        new Interface<>(TypeMirror.class, InterfaceConflationProxy::create),
        new Interface<>(IntersectionType.class, InterfaceConflationProxy::create),
        new Interface<>(UnionType.class, InterfaceConflationProxy::create),
        new Interface<>(ExecutableType.class, InterfaceConflationProxy::create),
        new Interface<>(NoType.class, InterfaceConflationProxy::create),
        new Interface<>(PrimitiveType.class, InterfaceConflationProxy::create),
        new Interface<>(WildcardType.class, InterfaceConflationProxy::create),
        new Interface<>(ReferenceType.class, InterfaceConflationProxy::create),
        new Interface<>(ArrayType.class, InterfaceConflationProxy::create),
        new Interface<>(NullType.class, InterfaceConflationProxy::create),
        new Interface<>(DeclaredType.class, InterfaceConflationProxy::create),
        new Interface<>(TypeVariable.class, InterfaceConflationProxy::create),
        new Interface<>(ErrorType.class, InterfaceConflationProxy::create),
        new Interface<>(Element.class, InterfaceConflationProxy::create),
        new Interface<>(QualifiedNameable.class, InterfaceConflationProxy::create),
        new Interface<>(Parameterizable.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.class, InterfaceConflationProxy::create),
        new Interface<>(PackageElement.class, InterfaceConflationProxy::create),
        new Interface<>(ExecutableElement.class, InterfaceConflationProxy::create),
        new Interface<>(VariableElement.class, InterfaceConflationProxy::create),
        new Interface<>(TypeElement.class, InterfaceConflationProxy::create),
        new Interface<>(TypeParameterElement.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.Directive.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.UsesDirective.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.OpensDirective.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.ProvidesDirective.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.RequiresDirective.class, InterfaceConflationProxy::create),
        new Interface<>(ModuleElement.ExportsDirective.class, InterfaceConflationProxy::create));
  }

  private static final class Interface<T> {
    private final @NotNull Class<T> interfaceType;
    private final @NotNull BiFunction<T, Class<T>, T> proxyFactory;

    private Interface(@NotNull Class<T> interfaceType, @NotNull BiFunction<T, Class<T>, T> factory) {
      this.interfaceType = interfaceType;
      this.proxyFactory = factory;
    }

    private @NotNull T createImplementation(@NotNull InvocationHandler handler) {
      return interfaceType.cast(Proxy.newProxyInstance(
          getClass().getClassLoader(),
          new Class[] {interfaceType},
          handler));
    }

    private @NotNull T createProxyInstance(@NotNull InvocationHandler handler) {
      return createProxyInstance(createImplementation(handler));
    }

    private @NotNull T createProxyInstance(@NotNull T impl) {
      return proxyFactory.apply(impl, interfaceType);
    }

    @Override
    public String toString() {
      return interfaceType.getSimpleName();
    }
  }

  private static final class InterfaceWithMethod<T> {
    private final @NotNull Interface<T> type;
    private final @NotNull Method method;

    private InterfaceWithMethod(@NotNull Interface<T> type, @NotNull Method method) {
      this.type = type;
      this.method = method;
    }

    @Override
    public String toString() {
      return type + "." + method.getName();
    }
  }
}
