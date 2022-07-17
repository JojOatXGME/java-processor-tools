package dev.johanness.processor.segmented;

import dev.johanness.processor.segmented._internal.Task;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.function.Supplier;

public final class Definitely {
  private final @NotNull Task<?> task;
  private final @NotNull ProcessingEnvironment processingEnv;
  private final @NotNull RoundEnvironment roundEnv;

  Definitely(@NotNull Task<?> task, @NotNull ProcessingEnvironment processingEnv, @NotNull RoundEnvironment roundEnv) {
    this.task = task;
    this.processingEnv = processingEnv;
    this.roundEnv = roundEnv;
  }

  public @NotNull ProcessingEnvironment processingEnv() {
    return processingEnv;
  }

  public @NotNull Messager messager() {
    return processingEnv.getMessager();
  }

  public @NotNull Filer filer() {
    return processingEnv.getFiler();
  }

  public boolean processingOver() {
    return roundEnv.processingOver();
  }

  public <T> @NotNull T get(@NotNull Key<T> key) {
    return task.getMap().get(key);
  }

  public <T> @Nullable T getOrNull(@NotNull Key<T> key) {
    return task.getMap().getOrNull(key);
  }

  @Contract("_, null -> null; _, !null -> param2")
  public <T> @Nullable T set(@NotNull Key<T> key, @Nullable T value) {
    task.getMap().set(key, value);
    return value;
  }

  public <T> @NotNull T setDefault(@NotNull Key<T> key, @NotNull Supplier<T> factory) {
    T result = task.getMap().getOrNull(key);
    if (result == null) {
      result = factory.get();
      task.getMap().set(key, result);
    }
    return result;
  }
}
