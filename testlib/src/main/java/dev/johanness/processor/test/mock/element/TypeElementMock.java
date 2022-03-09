package dev.johanness.processor.test.mock.element;

import dev.johanness.processor.test._internal.Future;
import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import dev.johanness.processor.test._internal.ModifierUtil;
import dev.johanness.processor.test.mock._common.NameMock;
import dev.johanness.processor.test.mock.type.DeclaredTypeMock;
import dev.johanness.processor.test.mock.type.NoTypeMock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor9;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TypeElementMock extends AbstractElementMock<TypeElementMock> implements TypeElement {
  private final @Nullable Class<?> clazz;
  private final @NotNull Set<Element> enclosedElements = new LinkedHashSet<>();
  private @Nullable ElementKind kind;
  private @Nullable String name;
  private @Nullable Element enclosingElement;
  private @Nullable TypeMirror superClass;
  private @Nullable Set<Modifier> modifiers;

  public TypeElementMock(@Nullable Class<?> clazz) {
    super(TypeElementMock.class);
    if (clazz != null && (clazz.isArray() || clazz.isPrimitive() || clazz == void.class)) {
      throw new IllegalArgumentException("Invalid class: " + clazz);
    }
    this.clazz = clazz;
  }

  //region Builder methods

  @Contract(value = "_ -> this", mutates = "this")
  public TypeElementMock setKind(@NotNull ElementKind kind) {
    if (kind != ElementKind.ANNOTATION_TYPE &&
        kind != ElementKind.CLASS &&
        kind != ElementKind.ENUM &&
        kind != ElementKind.INTERFACE &&
        !Future.isRecord(kind)) {
      throw new IllegalArgumentException("Invalid kind for TypeElement: " + kind);
    }
    else if (this.kind != null) {
      throw new IllegalStateException("Kind already set");
    }
    else {
      this.kind = kind;
      return this;
    }
  }

  @Contract(value = "_ -> this", mutates = "this")
  public TypeElementMock setParent(@NotNull Element parentElement) {
    if (this.enclosingElement != null) {
      throw new IllegalStateException("Parent element already set");
    }
    parentElement.accept(new SimpleElementVisitor9<>() {
      @Override
      public Object visitPackage(PackageElement e, Object o) {
        InterfaceConflationProxy.getImplementation(e, PackageElementMock.class)
            .addType(TypeElementMock.this);
        return null;
      }

      @Override
      public Object visitType(TypeElement e, Object o) {
        InterfaceConflationProxy.getImplementation(e, TypeElementMock.class)
            .addElement(TypeElementMock.this);
        return null;
      }

      @Override
      protected Object defaultAction(Element e, Object o) {
        // TODO: Add support for local classes
        throw new IllegalArgumentException("Invalid parent: " + e);
      }
    }, null);
    this.enclosingElement = InterfaceConflationProxy.create(parentElement, Element.class);
    return this;
  }

  @Contract(value = "_ -> this", mutates = "this")
  public TypeElementMock setSuperClass(@NotNull TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      throw new IllegalArgumentException("Invalid super class: " + type);
    }
    else if (this.superClass != null) {
      throw new IllegalStateException("Super class already set");
    }
    else {
      this.superClass = type;
      return this;
    }
  }

  @Contract(value = "_ -> this", mutates = "this")
  public TypeElementMock setModifiers(@NotNull Modifier... modifiers) {
    if (this.modifiers != null) {
      throw new IllegalStateException("Modifiers already set");
    }
    else {
      this.modifiers = Set.of(modifiers);
      return this;
    }
  }

  @Contract(value = "_ -> this", mutates = "this")
  public TypeElementMock addElement(@NotNull Element element) {
    if (enclosedElements.add(InterfaceConflationProxy.create(element, Element.class))) {
      // TODO: Add element
    }
    return this;
  }

  //endregion
  //region Interface methods

  @Override
  public @NotNull Name getSimpleName() {
    enforceInitialization();
    assert name != null;
    return new NameMock(name);
  }

  @Override
  public @NotNull Name getQualifiedName() {
    enforceInitialization();
    assert name != null;
    return new NameMock(buildQualifiedName());
  }

  @Override
  public @NotNull NestingKind getNestingKind() {
    enforceInitialization();
    assert name != null;
    assert enclosingElement != null;
    if (name.isEmpty()) {
      return NestingKind.ANONYMOUS;
    }
    else if (buildQualifiedName().isEmpty()) {
      return NestingKind.LOCAL;
    }
    else if (enclosingElement.getKind() == ElementKind.PACKAGE) {
      return NestingKind.TOP_LEVEL;
    }
    else {
      return NestingKind.MEMBER;
    }
  }

  @Override
  public @NotNull TypeMirror getSuperclass() {
    enforceInitialization();
    assert superClass != null;
    return superClass;
  }

  @Override
  public @NotNull List<? extends TypeMirror> getInterfaces() {
    enforceInitialization();
    // TODO: ...
    return null;
  }

  @Override
  public @NotNull List<? extends TypeParameterElement> getTypeParameters() {
    enforceInitialization();
    // TODO: ...
    return null;
  }

  @Override
  public @NotNull TypeMirror asType() {
    enforceInitialization();
    return new DeclaredTypeMock(this);
  }

  @Override
  public @NotNull ElementKind getKind() {
    enforceInitialization();
    assert kind != null;
    return kind;
  }

  @Override
  public @NotNull Set<Modifier> getModifiers() {
    enforceInitialization();
    assert modifiers != null;
    return modifiers;
  }

  @Override
  public @NotNull Element getEnclosingElement() {
    enforceInitialization();
    assert enclosingElement != null;
    return enclosingElement;
  }

  @Override
  public @NotNull List<? extends Element> getEnclosedElements() {
    // TODO: Add elements from `this.clazz`
    return List.copyOf(enclosedElements);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    enforceInitialization();
    return v.visitType(thisAs(TypeElement.class), p);
  }

  @Override
  public String toString() {
    if (isInitialized()) {
      return "TypeElementMock{" + buildQualifiedName() + '}';
    }
    else {
      return "{uninitialized TypeElementMock}";
    }
  }

  @Override
  public boolean equals(Object o) {
    enforceInitialization();
    assert name != null;
    assert enclosingElement != null;
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TypeElementMock that = (TypeElementMock) o;
    return name.equals(that.name) && enclosingElement.equals(that.enclosingElement);
  }

  @Override
  public int hashCode() {
    enforceInitialization();
    assert name != null;
    assert enclosingElement != null;
    return Objects.hash(name, enclosingElement);
  }

  //endregion
  //region Internal methods

  private boolean isInitialized() {
    return clazz != null ||
           kind != null && name != null;
  }

  private void enforceInitialization() {
    if (kind == null) {
      if (clazz != null && clazz.isAnnotation()) {
        kind = ElementKind.ANNOTATION_TYPE;
      }
      else if (clazz != null && clazz.isEnum()) {
        kind = ElementKind.ENUM;
      }
      else if (clazz != null && clazz.isInterface()) {
        kind = ElementKind.INTERFACE;
      }
      else if (clazz != null && Future.isRecord(clazz)) {
        kind = ElementKind.valueOf("RECORD");
      }
      else {
        kind = ElementKind.CLASS;
      }
    }
    if (name == null) {
      if (clazz == null) {
        throw new IllegalStateException("Name not set");
      }
      else {
        name = clazz.getSimpleName();
      }
    }
    if (enclosingElement == null) {
      // TODO: Add support for local and anonymous classes
      // TODO: Add element to parent? How to avoid a StackOverflow?
      Class<?> enclosingClass;
      if (clazz == null) {
        enclosingElement = new PackageElementMock("");
      }
      else if ((enclosingClass = clazz.getEnclosingClass()) != null) {
        enclosingElement = new TypeElementMock(enclosingClass);
      }
      else {
        enclosingElement = new PackageElementMock(clazz.getPackage());
      }
    }
    if (superClass == null) {
      Class<?> newSuperClass = clazz == null ? Object.class : clazz.getSuperclass();
      if (newSuperClass == null) {
        superClass = NoTypeMock.NONE;
      }
      else {
        superClass = InterfaceConflationProxy.create(
            new DeclaredTypeMock(new TypeElementMock(newSuperClass)),
            TypeMirror.class);
      }
    }
    if (modifiers == null) {
      if (clazz == null) {
        modifiers = Set.of();
      }
      else {
        modifiers = ModifierUtil.getModifiers(clazz);
      }
    }
  }

  private @NotNull String buildQualifiedName() {
    assert name != null || clazz != null;
    if (name == null) {
      return Objects.requireNonNullElse(clazz.getCanonicalName(), "");
    }
    else if (enclosingElement == null || name.isEmpty()) {
      return name;
    }
    else {
      return enclosingElement.accept(new SimpleElementVisitor9<>() {
        @Override
        public String visitType(TypeElement e, String s) {
          Name name = e.getSimpleName();
          return name.length() == 0 ? "" : visit(e.getEnclosingElement(), name + "." + s);
        }

        @Override
        public String visitPackage(PackageElement e, String s) {
          return e.isUnnamed() ? s : e.getQualifiedName() + "." + s;
        }

        @Override
        protected String defaultAction(Element e, String s) {
          return ""; // Local class
        }
      }, name);
    }
  }

  //endregion
}
