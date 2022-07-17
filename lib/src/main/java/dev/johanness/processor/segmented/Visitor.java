package dev.johanness.processor.segmented;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

@ApiStatus.OverrideOnly
public interface Visitor extends ElementVisitor<@NotNull Boolean, @NotNull Preliminary> {
  @Override
  default @NotNull Boolean visit(@NotNull Element element, @NotNull Preliminary preliminary) {
    return element.accept(this, preliminary);
  }

  @Override
  default @NotNull Boolean visit(@NotNull Element e) {
    throw new UnsupportedOperationException();
  }

  @Override
  default @NotNull Boolean visitModule(@NotNull ModuleElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitPackage(@NotNull PackageElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitType(@NotNull TypeElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitVariable(@NotNull VariableElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitExecutable(@NotNull ExecutableElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitTypeParameter(@NotNull TypeParameterElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitRecordComponent(@NotNull RecordComponentElement element, @NotNull Preliminary preliminary) {
    return visitUnknown(element, preliminary);
  }

  @Override
  default @NotNull Boolean visitUnknown(@NotNull Element element, @NotNull Preliminary preliminary) {
    return true;
  }
}
