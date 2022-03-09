package dev.johanness.processor.test.mock._common;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Name;
import java.util.Objects;
import java.util.stream.IntStream;

public final class NameMock implements Name {
  private final @NotNull String content;

  public NameMock(@NotNull String content) {
    this.content = content;
  }

  @Override
  public boolean contentEquals(CharSequence cs) {
    return content.contentEquals(cs);
  }

  @Override
  public int length() {
    return content.length();
  }

  @Override
  public char charAt(int index) {
    return content.charAt(index);
  }

  @Override
  public @NotNull CharSequence subSequence(int start, int end) {
    return content.subSequence(start, end);
  }

  @Override
  public @NotNull IntStream chars() {
    return content.chars();
  }

  @Override
  public @NotNull IntStream codePoints() {
    return content.codePoints();
  }

  @Override
  public @NotNull String toString() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NameMock nameMock = (NameMock) o;
    return content.equals(nameMock.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content);
  }
}
