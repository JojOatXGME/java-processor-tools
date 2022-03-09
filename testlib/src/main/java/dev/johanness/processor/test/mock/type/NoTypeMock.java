package dev.johanness.processor.test.mock.type;

import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

public final class NoTypeMock extends AbstractTypeMock<NoTypeMock> implements NoType {
  public static final @NotNull NoType NONE = InterfaceConflationProxy.create(
      new NoTypeMock(TypeKind.NONE, "NONE"),
      NoType.class);

  private final @NotNull TypeKind kind;
  private final @NotNull String asString;

  public NoTypeMock(@NotNull TypeKind kind, @NotNull String asString) {
    super(NoTypeMock.class);
    this.kind = kind;
    this.asString = asString;
  }

  @Override
  public @NotNull TypeKind getKind() {
    return kind;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitNoType(thisAs(NoType.class), p);
  }

  @Override
  public @NotNull String toString() {
    return asString;
  }
}
