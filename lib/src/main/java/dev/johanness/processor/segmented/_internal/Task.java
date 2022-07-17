package dev.johanness.processor.segmented._internal;

import dev.johanness.processor.segmented.Preliminary;
import dev.johanness.processor.segmented.SubProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Task<T> {
  private final @NotNull SubProcessor<T> subProcessor;
  private final T item;

  private final @NotNull KeyValueMap propagatedValues = new KeyValueMap();
  private final @NotNull Set<Task<?>> children = new HashSet<>();
  private final @NotNull Set<Task<?>> finalizers = new HashSet<>();
  private @Nullable Task<?> owner;

  public Task(@NotNull SubProcessor<T> subProcessor, T item) {
    this.subProcessor = subProcessor;
    this.item = item;
  }

  public T getItem() {
    return item;
  }

  public @NotNull SubProcessor<T> getSubProcessor() {
    return subProcessor;
  }

  public @NotNull KeyValueMap getMap() {
    return propagatedValues;
  }

  public void addChild(@NotNull Task<?> child) {
    if (child.owner != null && child.owner != this) {
      throw new IllegalArgumentException("Task already assigned to another task");
    }
    if (finalizers.contains(child)) {
      throw new IllegalArgumentException("Task already assigned as action");
    }
    if (children.add(child)) {
      child.owner = this;
      child.propagatedValues.setFallback(propagatedValues);
    }
  }

  public void addFinalizer(@NotNull Task<?> action) {
    if (action.owner != null && action.owner != this) {
      throw new IllegalArgumentException("Task already assigned to another task");
    }
    if (children.contains(action)) {
      throw new IllegalArgumentException("Task already assigned as child");
    }
    if (finalizers.add(action)) {
      action.owner = this;
      action.propagatedValues.setFallback(propagatedValues);
    }
  }

  public boolean run(@NotNull Preliminary preliminary) {
    return subProcessor.process(item, preliminary);
  }

  public @NotNull Stream<Task<?>> finish() {
    List<Stream<Task<?>>> containers = new ArrayList<>();
    containers.add(children.stream());
    if (children.isEmpty()) {
      containers.add(finalizers.stream());
      if (finalizers.isEmpty() && owner != null) {
        containers.add(owner.childFinished(this));
      }
    }
    return containers.stream().flatMap(Function.identity());
  }

  private @NotNull Stream<Task<?>> childFinished(@NotNull Task<?> origin) {
    assert children.contains(origin) || finalizers.contains(origin);
    assert !children.contains(origin) || !finalizers.contains(origin);
    if (children.remove(origin) && children.isEmpty()) {
      if (finalizers.isEmpty() && owner != null) {
        return owner.childFinished(this);
      }
      else {
        return finalizers.stream();
      }
    }
    else if (finalizers.remove(origin) && finalizers.isEmpty() && owner != null) {
      return owner.childFinished(this);
    }
    else {
      return Stream.empty();
    }
  }

  @Override
  public String toString() {
    return "Task{processor=" + subProcessor + ", item=" + item + '}';
  }
}
