package dev.johanness.processor.segmented;

import dev.johanness.processor.AnnotationType;
import dev.johanness.processor.segmented._internal.KeyValueMap;
import dev.johanness.processor.segmented._internal.Task;
import dev.johanness.processor.segmented._internal.VisitorInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class Preliminary {
  private final @NotNull Task<?> task;
  private final @NotNull ProcessingEnvironment processingEnv;
  private final @NotNull Collection<ActionContainer<?>> actions = new ArrayList<>();
  private final @NotNull Collection<Task<?>> children = new ArrayList<>();
  private final @NotNull Collection<Task<?>> finalizers = new ArrayList<>();
  private final @NotNull KeyValueMap keyValueMap = new KeyValueMap();

  Preliminary(@NotNull Task<?> task, @NotNull ProcessingEnvironment processingEnv) {
    this.task = task;
    this.processingEnv = processingEnv;
    this.keyValueMap.setFallback(task.getMap());
  }

  public @NotNull SourceVersion sourceVersion() {
    return processingEnv.getSourceVersion();
  }

  public @NotNull Elements elementUtils() {
    return processingEnv.getElementUtils();
  }

  public @NotNull Types typeUtils() {
    return processingEnv.getTypeUtils();
  }

  public @Nullable Locale locale() {
    return processingEnv.getLocale();
  }

  public @NotNull Map<String, String> options() {
    return processingEnv.getOptions();
  }

  public <T> @NotNull T get(@NotNull Key<T> key) {
    return keyValueMap.get(key);
  }

  public <T> @Nullable T getOrNull(@NotNull Key<T> key) {
    return keyValueMap.getOrNull(key);
  }

  @Contract("_, null -> null; _, !null -> param2")
  public <T> @Nullable T set(@NotNull Key<T> key, @Nullable T value) {
    keyValueMap.set(key, value);
    return value;
  }

  public <T> @NotNull T setDefault(@NotNull Key<T> key, @NotNull Supplier<T> factory) {
    T result = keyValueMap.getOrNull(key);
    if (result == null) {
      result = factory.get();
      keyValueMap.set(key, result);
    }
    return result;
  }

  public <T> void action(T item, @NotNull Action<T> action) {
    actions.add(new ActionContainer<>(action, item));
  }

  public <T> void process(T item, @NotNull SubProcessor<T> processor) {
    children.add(new Task<>(processor, item));
  }

  public void visit(@NotNull Element element, @NotNull ElementVisitor<Boolean, Preliminary> visitor, boolean recursive) {
    process(element, new VisitorInfo(visitor, null, null, recursive));
  }

  public void visit(@NotNull Element element, @NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull AnnotationType<?>... annotationTypes) {
    process(element, new VisitorInfo(visitor, null, Set.of(annotationTypes), true));
  }

  public void visit(@NotNull Element element, @NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull ElementKind... kinds) {
    process(element, new VisitorInfo(visitor, Set.of(kinds), null, true));
  }

  public <T> void finalize(T item, @NotNull SubProcessor<T> processor) {
    finalizers.add(new Task<>(processor, item));
  }

  void apply(@NotNull RoundEnvironment roundEnv) {
    Definitely definitely = new Definitely(task, processingEnv, roundEnv);
    actions.forEach(action -> action.run(definitely));
    keyValueMap.writeBackToFallback();
    children.forEach(task::addChild);
    finalizers.forEach(task::addFinalizer);
  }

  private static final class ActionContainer<T> {
    private final @NotNull Action<T> action;
    private final T item;

    private ActionContainer(@NotNull Action<T> action, T item) {
      this.action = action;
      this.item = item;
    }

    private void run(@NotNull Definitely definitely) {
      action.run(item, definitely);
    }
  }
}
