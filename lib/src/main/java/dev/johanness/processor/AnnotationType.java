package dev.johanness.processor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Processor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleElementVisitor9;
import java.lang.annotation.*;

public abstract class AnnotationType<P> {
  private final @NotNull String binaryName;
  private final @NotNull String canonicalName;
  private final @Nullable String moduleName;
  private final @NotNull String nameWithModule;

  /**
   * Initializes the instance from the class instance of an annotation.
   *
   * @param clazz the class instance from an annotation as obtained by {@code
   *              MyAnnotation.class}.
   */
  protected AnnotationType(@NotNull Class<? extends Annotation> clazz) {
    this(clazz.getName(), clazz.getModule().getName());
    if (!clazz.isAnnotation()) {
      throw new IllegalArgumentException("Not an annotation: " + clazz);
    }
  }

  /**
   * Initializes the instance from the given metadata.
   *
   * @param binaryName the binary name of tha annotation interface as returned
   *                   by {@link Class#getName()}.
   * @param moduleName the module name of the annotation interface as returned
   *                   by {@link Module#getName()}.
   */
  protected AnnotationType(@NotNull String binaryName, @Nullable String moduleName) {
    if (moduleName != null && moduleName.isEmpty()) {
      throw new IllegalArgumentException("The empty string is not a valid module name");
    }
    this.binaryName = binaryName;
    this.canonicalName = binaryName.replace('$', '.');
    this.moduleName = moduleName;
    this.nameWithModule = moduleName == null ? canonicalName : moduleName + "/" + canonicalName;
  }

  /**
   * Returns the binary name of the annotation interface. The binary name is the
   * name as returned by {@link Class#getName()}.
   *
   * @return the binary name of the annotation interface.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-13.html#jls-13.1">
   * Java Language Specification, Java SE 11 Edition, Section 13.1</a>
   */
  public final @NotNull String binaryName() {
    return binaryName;
  }

  /**
   * Returns the canonical name of the annotation interface. The canonical name
   * is the name as returned by {@link Class#getCanonicalName()}.
   *
   * @return the canonical name of the annotation interface.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-6.html#jls-6.7">
   * Java Language Specification, Java SE 11 Edition, Section 6.7</a>
   */
  public final @NotNull String canonicalName() {
    return canonicalName;
  }

  /**
   * Returns the name of the module which contains the annotation interface.
   *
   * @return the name of the module if it exists, and {@code null} otherwise.
   */
  public final @Nullable String moduleName() {
    return moduleName;
  }

  /**
   * Returns a unique name of the annotation interface, including the module
   * name. Specifically, this method returns the name as defined by {@link
   * Processor#getSupportedAnnotationTypes()}.
   *
   * @return the canonical name of the annotation interface prefixed with the
   * module name.
   */
  public final @NotNull String nameWithModule() {
    return nameWithModule;
  }

  /**
   * Constructs a proxy for the given annotation mirror. The proxy can be used
   * to access values specified on the annotation.
   *
   * @param mirror the annotation mirror which forms the backend of the proxy.
   * @return the proxy for accessing the annotation.
   */
  public final @NotNull P proxy(@NotNull AnnotationMirror mirror) {
    if (!getCanonicalName(mirror.getAnnotationType()).contentEquals(canonicalName)) {
      throw new IllegalArgumentException(String.format(
          "Annotation mirror does not belong to %s: %s",
          binaryName, mirror));
    }
    return createProxy(mirror);
  }

  @Override
  public String toString() {
    return nameWithModule();
  }

  @ApiStatus.OverrideOnly
  protected abstract @NotNull P createProxy(@NotNull AnnotationMirror mirror);

  private static @NotNull Name getCanonicalName(@NotNull DeclaredType type) {
    return type.asElement().accept(new SimpleElementVisitor9<>() {
      @Override
      public Name visitType(TypeElement e, Object ignore) {
        return e.getQualifiedName();
      }

      @Override
      protected Name defaultAction(Element e, Object o) {
        throw new IllegalStateException("Illegal annotation type: " + e);
      }
    }, null);
  }
}
