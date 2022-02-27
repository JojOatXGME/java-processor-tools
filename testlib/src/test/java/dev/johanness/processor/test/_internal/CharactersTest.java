package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class CharactersTest {
  @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
  @ArgumentsSource(EscapeSource.class)
  void testEscape(@NotNull String expectedResult, char input) {
    StringBuilder builder = new StringBuilder();
    Characters.escape(builder, input);
    Assertions.assertEquals(expectedResult, builder.toString());
  }

  private static final class EscapeSource implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          arguments("\\u0000", '\u0000'),
          arguments("\\b", '\b'),
          arguments("\\t", '\t'),
          arguments("\\n", '\n'),
          arguments("\\f", '\f'),
          arguments("\\r", '\r'),
          arguments("\\\"", '"'),
          arguments("\\'", '\''),
          arguments("\\\\", '\\'),
          arguments("A", 'A'),
          arguments("b", 'b'),
          arguments("é", 'é'),
          arguments(".", '.'),
          arguments("(", '('),
          arguments("\\u00A0", '\u00A0'),
          arguments("\\uFEFF", '\uFEFF'));
    }
  }
}
