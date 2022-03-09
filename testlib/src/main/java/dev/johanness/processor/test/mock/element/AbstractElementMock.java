package dev.johanness.processor.test.mock.element;

import dev.johanness.processor.test._internal.InterfaceConflationProxy;
import dev.johanness.processor.test.mock._common.AnnotatedConstructMock;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;

abstract class AbstractElementMock<THIS extends AbstractElementMock<THIS>> extends AnnotatedConstructMock<THIS> implements Element {
  AbstractElementMock(@NotNull Class<THIS> self) {
    super(self);
  }

  //region Interface methods

  @Override
  public abstract String toString();

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  //endregion
  //region Internal methods

  protected final <T extends Element> @NotNull T thisAs(@NotNull Class<T> type) {
    return InterfaceConflationProxy.create(type.cast(this), type);
  }

  //endregion
}
