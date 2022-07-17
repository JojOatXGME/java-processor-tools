package dev.johanness.processor.segmented._internal;

import dev.johanness.processor.segmented.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class KeyValueMap {
  private final @NotNull Map<Key<?>, Object> map = new IdentityHashMap<>();
  private @Nullable KeyValueMap fallback;

  public void setFallback(@NotNull KeyValueMap newFallback) {
    fallback = newFallback;
  }

  public <T> void set(@NotNull Key<T> key, T value) {
    map.put(key, value);
  }

  public <T> T get(@NotNull Key<T> key) {
    T result = getOrNull(key);
    if (result == null) {
      throw new NoSuchElementException(key.toString());
    }
    else {
      return result;
    }
  }

  public <T> @Nullable T getOrNull(@NotNull Key<T> key) {
    @SuppressWarnings("unchecked") T result = (T) map.get(key);
    if (result == null && fallback != null) {
      return fallback.getOrNull(key);
    }
    else {
      return result;
    }
  }

  public void writeBackToFallback() {
    if (fallback == null) {
      throw new IllegalStateException("no fallback");
    }
    fallback.map.putAll(map);
    map.clear();
  }
}
