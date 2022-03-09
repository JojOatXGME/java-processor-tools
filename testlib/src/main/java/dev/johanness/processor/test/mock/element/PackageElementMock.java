package dev.johanness.processor.test.mock.element;

import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import dev.johanness.processor.test.mock._common.NameMock;
import dev.johanness.processor.test.mock.type.NoTypeMock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PackageElementMock extends AbstractElementMock<PackageElementMock> implements PackageElement {
  private final @NotNull String name;
  private final @NotNull Set<TypeElement> enclosedElements = new LinkedHashSet<>();
  private @Nullable ModuleElement moduleElement;

  public PackageElementMock(@NotNull String name) {
    super(PackageElementMock.class);
    this.name = name;
  }

  public PackageElementMock(@NotNull Package base) {
    super(PackageElementMock.class);
    this.name = base.getName();
  }

  //region Builder methods

  @Contract(value = "_ -> this", mutates = "this")
  public PackageElementMock setModule(@NotNull ModuleElement moduleElement) {
    if (this.moduleElement != null) {
      throw new IllegalStateException("Module already set");
    }
    InterfaceConflationProxy.getImplementation(moduleElement, ModuleElementMock.class)
        .addPackage(this);
    this.moduleElement = InterfaceConflationProxy.create(moduleElement, ModuleElement.class);
    return this;
  }

  @Contract(value = "_ -> this", mutates = "this")
  public PackageElementMock addType(@NotNull TypeElement typeElement) {
    if (enclosedElements.add(InterfaceConflationProxy.create(typeElement, TypeElement.class))) {
      InterfaceConflationProxy.getImplementation(typeElement, TypeElementMock.class)
          .setParent(this);
    }
    return this;
  }

  //endregion
  //region Interface methods

  @Override
  public @NotNull Name getSimpleName() {
    int lastDot = name.lastIndexOf('.');
    return new NameMock(lastDot == -1 ? name : name.substring(lastDot + 1));
  }

  @Override
  public @NotNull Name getQualifiedName() {
    return new NameMock(name);
  }

  @Override
  public boolean isUnnamed() {
    return name.isEmpty();
  }

  @Override
  public @NotNull TypeMirror asType() {
    return new NoTypeMock(TypeKind.PACKAGE, name);
  }

  @Override
  public @NotNull ElementKind getKind() {
    return ElementKind.PACKAGE;
  }

  @Override
  public @NotNull Set<Modifier> getModifiers() {
    return Set.of();
  }

  @Override
  public @Nullable Element getEnclosingElement() {
    return moduleElement;
  }

  @Override
  public @NotNull List<? extends Element> getEnclosedElements() {
    // TODO: Load other classes within this package with reflection when using PackageElementMock(Package)?
    return List.copyOf(enclosedElements);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(thisAs(PackageElement.class), p);
  }

  @Override
  public @NotNull String toString() {
    return "PackageElementMock{" + name + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PackageElementMock that = (PackageElementMock) o;
    return name.equals(that.name) && Objects.equals(moduleElement, that.moduleElement);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, moduleElement);
  }

  //endregion
}
