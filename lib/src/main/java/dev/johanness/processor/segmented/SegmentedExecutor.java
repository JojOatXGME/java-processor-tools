package dev.johanness.processor.segmented;

import dev.johanness.processor.AnnotationType;
import dev.johanness.processor.segmented._internal.Task;
import dev.johanness.processor.segmented._internal.VisitorInfo;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.tools.Diagnostic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class SegmentedExecutor {
  private static final @NotNull SubProcessor<?> FAKE_PROCESSOR = (item, preliminary) -> {
    throw new UnsupportedOperationException();
  };

  private final @NotNull ProcessingEnvironment processingEnv;
  private final @NotNull Task<?> rootTask = new Task<>(FAKE_PROCESSOR, null);
  private final @NotNull Task<?> fakeChild = new Task<>(FAKE_PROCESSOR, null);
  private final @NotNull Queue<Task<?>> queue = new ArrayDeque<>();
  private final @NotNull List<SubProcessor<? super Element>> rootProcessors = new ArrayList<>();

  public SegmentedExecutor(@NotNull ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    rootTask.addChild(fakeChild);
  }

  public @NotNull Preliminary getRoot() {
    return new Preliminary(rootTask, processingEnv);
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, boolean recursive) {
    rootProcessors.add(new VisitorInfo(visitor, null, null, recursive));
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull AnnotationType<?>... annotationTypes) {
    rootProcessors.add(new VisitorInfo(visitor, null, Set.of(annotationTypes), true));
  }

  public void addVisitor(@NotNull ElementVisitor<Boolean, Preliminary> visitor, @NotNull ElementKind... kinds) {
    rootProcessors.add(new VisitorInfo(visitor, Set.of(kinds), null, true));
  }

  public void process(@NotNull RoundEnvironment roundEnv) {
    for (SubProcessor<? super Element> processor : rootProcessors) {
      for (Element element : roundEnv.getRootElements()) {
        Task<?> task = new Task<>(processor, element);
        rootTask.addChild(task);
        queue.add(task);
      }
    }

    if (roundEnv.processingOver()) {
      fakeChild.finish();
    }

    Queue<Task<?>> nextRound = new ArrayDeque<>();
    while (!queue.isEmpty()) {
      Task<?> task = queue.remove();
      if (!run(task, roundEnv)) {
        nextRound.add(task);
      }
    }
    queue.addAll(nextRound);

    if (roundEnv.processingOver()) {
      for (Task<?> task : queue) {
        Object item = task.getItem();
        if (item instanceof Element && task.getSubProcessor() instanceof VisitorInfo) {
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.ERROR,
              "Unprocessed element remaining after end of compilation. " +
              "You may report this to the developers of the annotation processor.",
              (Element) item);
        }
        else {
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.ERROR,
              "Unprocessed task remaining after end of compilation. " +
              "You may report this to the developers of the annotation processor. " +
              "Task: " + task);
        }
      }
    }
  }

  private boolean run(@NotNull Task<?> task, @NotNull RoundEnvironment roundEnv) {
    Preliminary preliminary = new Preliminary(task, processingEnv);
    if (task.run(preliminary)) {
      preliminary.apply(roundEnv);
      task.finish().forEach(queue::add);
      return true;
    }
    return false;
  }
}
