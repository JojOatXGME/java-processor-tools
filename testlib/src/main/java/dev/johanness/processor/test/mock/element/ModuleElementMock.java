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
import java.lang.module.ModuleDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModuleElementMock extends AbstractElementMock<ModuleElementMock> implements ModuleElement {
  private final @NotNull String name;
  private final @NotNull Set<PackageElement> packageElements = new LinkedHashSet<>();
  private final @NotNull List<Directive> directives = new ArrayList<>();
  private boolean open;

  public ModuleElementMock(@NotNull String name) {
    super(ModuleElementMock.class);
    this.name = name;
  }

  //region Builder methods

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock setOpen(boolean value) {
    this.open = value;
    return this;
  }

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock addPackage(@NotNull PackageElement packageElement) {
    if (this.packageElements.add(InterfaceConflationProxy.create(packageElement, PackageElement.class))) {
      InterfaceConflationProxy.getImplementation(packageElement, PackageElementMock.class)
          .setModule(this);
    }
    return this;
  }

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock addDirective(@NotNull Directive directive) {
    this.directives.add(InterfaceConflationProxy.create(directive, Directive.class));
    return this;
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public ModuleElementMock requires(@NotNull ModuleElement dependency, @NotNull ModuleDescriptor.Requires.Modifier... modifiers) {
    List<ModuleDescriptor.Requires.Modifier> mods = Arrays.asList(modifiers);
    return addDirective(new Requires(
        dependency,
        mods.contains(ModuleDescriptor.Requires.Modifier.STATIC),
        mods.contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE)));
  }

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock exports(@NotNull PackageElement packageElement) {
    return addDirective(new Exports(packageElement, null));
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public ModuleElementMock exports(@NotNull PackageElement packageElement, @NotNull ModuleElement... targetModules) {
    return addDirective(new Exports(packageElement, Arrays.asList(targetModules)));
  }

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock opens(@NotNull PackageElement packageElement) {
    return addDirective(new Opens(packageElement, null));
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public ModuleElementMock opens(@NotNull PackageElement packageElement, @NotNull ModuleElement... targetModules) {
    return addDirective(new Opens(packageElement, Arrays.asList(targetModules)));
  }

  @Contract(value = "_ -> this", mutates = "this")
  public ModuleElementMock uses(@NotNull TypeElement service) {
    return addDirective(new Uses(service));
  }

  @Contract(value = "_, _ -> this", mutates = "this")
  public ModuleElementMock provides(@NotNull TypeElement service, @NotNull TypeElement... implementations) {
    return addDirective(new Provides(service, Arrays.asList(implementations)));
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
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean isUnnamed() {
    return name.isEmpty();
  }

  @Override
  public @NotNull List<? extends Directive> getDirectives() {
    return Collections.unmodifiableList(directives);
  }

  @Override
  public @NotNull TypeMirror asType() {
    return new NoTypeMock(TypeKind.MODULE, name);
  }

  @Override
  public @NotNull ElementKind getKind() {
    return ElementKind.MODULE;
  }

  @Override
  public @NotNull Set<Modifier> getModifiers() {
    return Set.of();
  }

  @Override
  public @Nullable Element getEnclosingElement() {
    return null;
  }

  @Override
  public @NotNull List<? extends Element> getEnclosedElements() {
    return List.copyOf(packageElements);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitModule(thisAs(ModuleElement.class), p);
  }

  @Override
  public @NotNull String toString() {
    return "ModuleElementMock{" + name + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModuleElementMock that = (ModuleElementMock) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  //endregion
  //region Directives

  private static abstract class AbstractDirective implements Directive {
    final <T extends Directive> @NotNull T thisAs(@NotNull Class<T> type) {
      return InterfaceConflationProxy.create(type.cast(this), type);
    }
  }

  private static final class Uses extends AbstractDirective implements UsesDirective {
    private final @NotNull TypeElement service;

    private Uses(@NotNull TypeElement service) {
      this.service = InterfaceConflationProxy.create(service, TypeElement.class);
    }

    @Override
    public @NotNull TypeElement getService() {
      return service;
    }

    @Override
    public @NotNull DirectiveKind getKind() {
      return DirectiveKind.USES;
    }

    @Override
    public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
      return v.visitUses(thisAs(UsesDirective.class), p);
    }
  }

  private static final class Opens extends AbstractDirective implements OpensDirective {
    private final @NotNull PackageElement packageElement;
    private final @Nullable List<ModuleElement> targetModules;

    private Opens(@NotNull PackageElement packageElement, @Nullable Collection<ModuleElement> targetModules) {
      this.packageElement = InterfaceConflationProxy.create(packageElement, PackageElement.class);
      this.targetModules = targetModules == null ? null : targetModules.stream()
          .map(module -> InterfaceConflationProxy.create(module, ModuleElement.class))
          .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull PackageElement getPackage() {
      return packageElement;
    }

    @Override
    public @Nullable List<? extends ModuleElement> getTargetModules() {
      return targetModules;
    }

    @Override
    public @NotNull DirectiveKind getKind() {
      return DirectiveKind.OPENS;
    }

    @Override
    public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
      return v.visitOpens(thisAs(OpensDirective.class), p);
    }
  }

  private static final class Provides extends AbstractDirective implements ProvidesDirective {
    private final @NotNull TypeElement service;
    private final @NotNull List<TypeElement> implementations;

    private Provides(@NotNull TypeElement service, @NotNull Collection<TypeElement> implementations) {
      if (implementations.isEmpty()) {
        throw new IllegalArgumentException("No implementation specified");
      }
      this.service = InterfaceConflationProxy.create(service, TypeElement.class);
      this.implementations = implementations.stream()
          .map(type -> InterfaceConflationProxy.create(type, TypeElement.class))
          .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull TypeElement getService() {
      return service;
    }

    @Override
    public @NotNull List<? extends TypeElement> getImplementations() {
      return implementations;
    }

    @Override
    public @NotNull DirectiveKind getKind() {
      return DirectiveKind.PROVIDES;
    }

    @Override
    public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
      return v.visitProvides(thisAs(ProvidesDirective.class), p);
    }
  }

  private static final class Requires extends AbstractDirective implements RequiresDirective {
    private final @NotNull ModuleElement dependency;
    private final boolean isStatic;
    private final boolean isTransitive;

    private Requires(@NotNull ModuleElement dependency, boolean isStatic, boolean isTransitive) {
      this.dependency = InterfaceConflationProxy.create(dependency, ModuleElement.class);
      this.isStatic = isStatic;
      this.isTransitive = isTransitive;
    }

    @Override
    public boolean isStatic() {
      return isStatic;
    }

    @Override
    public boolean isTransitive() {
      return isTransitive;
    }

    @Override
    public @NotNull ModuleElement getDependency() {
      return dependency;
    }

    @Override
    public @NotNull DirectiveKind getKind() {
      return DirectiveKind.REQUIRES;
    }

    @Override
    public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
      return v.visitRequires(thisAs(RequiresDirective.class), p);
    }
  }

  private static final class Exports extends AbstractDirective implements ExportsDirective {
    private final @NotNull PackageElement packageElement;
    private final @Nullable List<ModuleElement> targetModules;

    private Exports(@NotNull PackageElement packageElement, @Nullable Collection<ModuleElement> targetModules) {
      this.packageElement = InterfaceConflationProxy.create(packageElement, PackageElement.class);
      this.targetModules = targetModules == null ? null : targetModules.stream()
          .map(module -> InterfaceConflationProxy.create(module, ModuleElement.class))
          .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull PackageElement getPackage() {
      return packageElement;
    }

    @Override
    public @Nullable List<? extends ModuleElement> getTargetModules() {
      return targetModules;
    }

    @Override
    public @NotNull DirectiveKind getKind() {
      return DirectiveKind.EXPORTS;
    }

    @Override
    public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
      return v.visitExports(thisAs(ExportsDirective.class), p);
    }
  }

  //endregion
}
