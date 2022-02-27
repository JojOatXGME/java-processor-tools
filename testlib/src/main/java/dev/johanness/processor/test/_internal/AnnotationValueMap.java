package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleElementVisitor9;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class AnnotationValueMap {
  private final @NotNull Map<ExecutableElement, AnnotationValue> unmodifiableMap = new AsUnmodifiableMap();

  private final @NotNull DeclaredType annotationType;
  private final @NotNull ExecutableElement @NotNull [] elements;
  private final @Nullable AnnotationValue @NotNull [] values;
  private final @NotNull Map<ExecutableElement, Integer> indexMapByElement;
  private final @NotNull Map<String, Integer> indexMapByName;

  public AnnotationValueMap(@NotNull DeclaredType annotationType) {
    this.annotationType = annotationType;
    this.elements = methodsOf(annotationType);
    this.values = new AnnotationValue[elements.length];
    this.indexMapByElement = new HashMap<>(elements.length);
    this.indexMapByName = new HashMap<>(elements.length);
    for (int i = 0; i < elements.length; ++i) {
      ExecutableElement element = elements[i];
      indexMapByElement.put(element, i);
      indexMapByName.put(element.getSimpleName().toString(), i);
    }
  }

  public int size() {
    int size = 0;
    for (var entry : values) {
      size += entry == null ? 0 : 1;
    }
    return size;
  }

  public void put(@NotNull String key, @Nullable AnnotationValue value) {
    int index = indexMapByName.getOrDefault(key, -1);
    if (index >= 0) {
      values[index] = value;
    }
    else {
      throw new IllegalArgumentException(String.format(
          "%s not a member of %s",
          key, annotationType));
    }
  }

  public @NotNull Map<ExecutableElement, AnnotationValue> asUnmodifiableMap() {
    return unmodifiableMap;
  }

  private static @NotNull ExecutableElement @NotNull [] methodsOf(@NotNull DeclaredType type) {
    List<? extends Element> members = type.asElement().getEnclosedElements();
    List<ExecutableElement> result = new ArrayList<>(members.size());
    for (Element member : members) {
      if (member.getKind() == ElementKind.METHOD) {
        result.add(asExecutableElement(member));
      }
    }
    return result.toArray(ExecutableElement[]::new);
  }

  private static @NotNull ExecutableElement asExecutableElement(@NotNull Element element) {
    return element.accept(new SimpleElementVisitor9<>() {
      @Override
      public ExecutableElement visitExecutable(ExecutableElement e, Object ignore) {
        return e;
      }

      @Override
      protected ExecutableElement defaultAction(Element e, Object ignore) {
        throw new IllegalArgumentException("Not an ExecutableElement: " + e);
      }
    }, null);
  }

  private final class AsUnmodifiableMap extends AbstractMap<ExecutableElement, AnnotationValue> {
    private final @NotNull Set<Map.Entry<ExecutableElement, AnnotationValue>> entrySet = new AsUnmodifiableEntrySet();

    @Override
    public int size() {
      return AnnotationValueMap.this.size();
    }

    @Override
    public @NotNull Set<Entry<ExecutableElement, AnnotationValue>> entrySet() {
      return entrySet;
    }

    @Override
    public boolean containsKey(Object key) {
      return get(key) != null;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public AnnotationValue get(Object key) {
      int index = indexMapByElement.getOrDefault(key, -1);
      return index >= 0 ? values[index] : null;
    }
  }

  private final class AsUnmodifiableEntrySet extends AbstractSet<Map.Entry<ExecutableElement, AnnotationValue>> {
    @Override
    public @NotNull Iterator<Map.Entry<ExecutableElement, AnnotationValue>> iterator() {
      return new EntryIterator();
    }

    @Override
    public int size() {
      return AnnotationValueMap.this.size();
    }
  }

  private final class EntryIterator implements Iterator<Map.Entry<ExecutableElement, AnnotationValue>> {
    private int index;

    @Override
    public boolean hasNext() {
      while (true) {
        if (index >= values.length) {
          return false;
        }
        else if (values[index] != null) {
          return true;
        }
        index += 1;
      }
    }

    @Override
    public Map.Entry<ExecutableElement, AnnotationValue> next() {
      if (hasNext()) {
        return new AbstractMap.SimpleImmutableEntry<>(elements[index], values[index]);
      }
      else {
        throw new NoSuchElementException();
      }
    }
  }
}
