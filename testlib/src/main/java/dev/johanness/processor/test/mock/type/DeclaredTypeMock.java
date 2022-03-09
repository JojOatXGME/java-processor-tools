package dev.johanness.processor.test.mock.type;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.reflect.Type;
import java.util.List;

public final class DeclaredTypeMock extends AbstractTypeMock<DeclaredTypeMock> implements DeclaredType {
  // TODO: Implement!
  private final @NotNull TypeElement element;

  public DeclaredTypeMock(@NotNull Type type) {
    super(DeclaredTypeMock.class);
    throw new UnsupportedOperationException("Not implemented");
  }

  public DeclaredTypeMock(@NotNull TypeElement type) {
    super(DeclaredTypeMock.class);
    this.element = type;
  }

  @Override
  public Element asElement() {
    return element;
  }

  @Override
  public TypeMirror getEnclosingType() {
    return null;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return null;
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.DECLARED;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(thisAs(DeclaredType.class), p);
  }

  @Override
  public String toString() {
    return element.getQualifiedName().toString();
  }
}
