package dev.johanness.processor.segmented;

import org.jetbrains.annotations.NotNull;

public final class Key<T> {
  private final @NotNull String description;

  public Key() {
    this("");
  }

  public Key(@NotNull String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description.isEmpty() ? super.toString() : "Key{'" + description + "'}";
  }
}
