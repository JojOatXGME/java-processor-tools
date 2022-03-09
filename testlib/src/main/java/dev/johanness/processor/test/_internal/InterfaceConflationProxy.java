package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;

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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Proxy which sabotages {@code instanceof} checks. As documented in the Javadoc
 * of {@link Element} and {@link TypeMirror}, you must not use {@code
 * instanceof} to check the specific kind of any element or type. This proxy
 * shall help to find such prohibited checks.
 */
public final class InterfaceConflationProxy implements InvocationHandler {

  // We need separate constructors because Element.getKind() and
  // TypeMirror.getKind() share the same signature but have an incompatible
  // return type. For this reason, java.lang.reflect.Proxy does not allow to
  // create a proxy class which implements both of these interfaces.
  private static final @NotNull Constructor<?> typeConstructor = createConstructor(
      AnnotationMirror.class,
      AnnotationValue.class,
      TypeMirror.class,
      IntersectionType.class,
      UnionType.class,
      ExecutableType.class,
      NoType.class,
      PrimitiveType.class,
      WildcardType.class,
      ReferenceType.class,
      ArrayType.class,
      NullType.class,
      DeclaredType.class,
      TypeVariable.class,
      ErrorType.class);
  private static final @NotNull Constructor<?> elementConstructor = createConstructor(
      Element.class,
      QualifiedNameable.class,
      Parameterizable.class,
      ModuleElement.class,
      PackageElement.class,
      ExecutableElement.class,
      VariableElement.class,
      TypeElement.class,
      TypeParameterElement.class);
  private static final @NotNull Constructor<?> moduleDirectiveConstructor = createConstructor(
      ModuleElement.Directive.class,
      ModuleElement.UsesDirective.class,
      ModuleElement.OpensDirective.class,
      ModuleElement.ProvidesDirective.class,
      ModuleElement.RequiresDirective.class,
      ModuleElement.ExportsDirective.class);

  private final @NotNull Object implementation;

  private InterfaceConflationProxy(@NotNull Object implementation) {
    this.implementation = implementation;
  }

  public static @NotNull AnnotationMirror create(@NotNull AnnotationMirror implementation) {
    return newInstance(typeConstructor, implementation, AnnotationMirror.class);
  }

  public static @NotNull AnnotationValue create(@NotNull AnnotationValue implementation) {
    return newInstance(typeConstructor, implementation, AnnotationValue.class);
  }

  public static <T extends TypeMirror> @NotNull T create(@NotNull T implementation, @NotNull Class<T> type) {
    return newInstance(typeConstructor, implementation, type);
  }

  public static <T extends Element> @NotNull T create(@NotNull T implementation, @NotNull Class<T> type) {
    return newInstance(elementConstructor, implementation, type);
  }

  public static <T extends ModuleElement.Directive> @NotNull T create(@NotNull T implementation, @NotNull Class<T> type) {
    return newInstance(moduleDirectiveConstructor, implementation, type);
  }

  public static <T> @NotNull T getImplementation(@NotNull Object object, @NotNull Class<T> type) {
    Object impl = getImplementation0(object);
    return type.cast(impl);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (method.getDeclaringClass().isInstance(implementation)) {
        return method.invoke(implementation, args);
      }
      else {
        Method resolved = implementation.getClass().getMethod(method.getName(), method.getParameterTypes());
        return resolved.invoke(implementation, args);
      }
    }
    catch (InvocationTargetException e) {
      throw e.getCause(); // Unwrap exception
    }
  }

  private static <T> @NotNull T newInstance(@NotNull Constructor<?> constructor, @NotNull T implementation, @NotNull Class<T> type) {
    if (implementation instanceof PrivateInterface) {
      // Check if we can cast the underlying object to fail early.
      type.cast(getImplementation0(implementation));
      return implementation;
    }
    else {
      InterfaceConflationProxy handler = new InterfaceConflationProxy(implementation);
      try {
        return type.cast(constructor.newInstance(handler));
      }
      catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static @NotNull Object getImplementation0(@NotNull Object object) {
    if (object instanceof PrivateInterface) {
      InterfaceConflationProxy handler = (InterfaceConflationProxy) Proxy.getInvocationHandler(object);
      return handler.implementation;
    }
    else {
      return object;
    }
  }

  private static @NotNull Constructor<?> createConstructor(@NotNull Class<?>... interfaces) {
    // The method getProxyClass is deprecated because it might cause problems
    // with the JPMS (aka Jigsaw). However, according to my benchmarks, using
    // newProxyInstance would be multiple times slower. For this reason, I
    // continue to use getProxyClass. The private interface ensures that the
    // proxy class is generated within this package. As I understand the
    // restrictions, this should ensure its accessibility. Note that this works
    // only because we know that all other interfaces are public, otherwise
    // getProxyClass would throw an exception.
    Class<?>[] allInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
    allInterfaces[interfaces.length] = PrivateInterface.class;
    @SuppressWarnings("deprecation")
    Class<?> proxyClass = Proxy.getProxyClass(InterfaceConflationProxy.class.getClassLoader(), allInterfaces);
    try {
      return proxyClass.getConstructor(InvocationHandler.class);
    }
    catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  // Private interface to avoid potential problems with the JPMS, see createConstructor.
  private interface PrivateInterface {}
}
