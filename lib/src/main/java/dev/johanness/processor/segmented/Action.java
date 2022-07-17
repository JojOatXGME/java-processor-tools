package dev.johanness.processor.segmented;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@ApiStatus.OverrideOnly
public interface Action<T> {
  void run(T item, @NotNull Definitely definitely);
}
