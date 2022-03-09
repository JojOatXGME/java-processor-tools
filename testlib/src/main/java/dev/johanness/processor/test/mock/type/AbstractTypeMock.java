package dev.johanness.processor.test.mock.type;

import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import dev.johanness.processor.test.mock._common.AnnotatedConstructMock;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.TypeMirror;

abstract class AbstractTypeMock<THIS extends AbstractTypeMock<THIS>> extends AnnotatedConstructMock<THIS> implements TypeMirror {
  AbstractTypeMock(@NotNull Class<THIS> self) {
    super(self);
  }

  //region Interface methods

  @Override
  public abstract String toString();

  //endregion
  //region Internal methods

  protected final <T extends TypeMirror> @NotNull T thisAs(@NotNull Class<T> type) {
    return InterfaceConflationProxy.create(type.cast(this), type);
  }

  //endregion
}
