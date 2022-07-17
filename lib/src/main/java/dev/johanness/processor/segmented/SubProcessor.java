package dev.johanness.processor.segmented;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@ApiStatus.OverrideOnly
public interface SubProcessor<T> {
  boolean process(T item, @NotNull Preliminary preliminary);
}
