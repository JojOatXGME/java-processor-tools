package dev.johanness.processor.test._internal;

import org.jetbrains.annotations.NotNull;

public final class Characters {
  private static final int VISIBLE_CHARACTER_TYPES = characterTypeMask(
      // See https://www.compart.com/de/unicode/category
      Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER,
      Character.MODIFIER_LETTER, Character.OTHER_LETTER,
      Character.NON_SPACING_MARK, Character.ENCLOSING_MARK, Character.COMBINING_SPACING_MARK,
      Character.DECIMAL_DIGIT_NUMBER, Character.LETTER_NUMBER, Character.OTHER_NUMBER,
      Character.DASH_PUNCTUATION, Character.START_PUNCTUATION, Character.END_PUNCTUATION,
      Character.CONNECTOR_PUNCTUATION, Character.OTHER_PUNCTUATION,
      Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
      Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL, Character.MODIFIER_SYMBOL, Character.OTHER_SYMBOL);

  private Characters() {} // Cannot be instantiated

  public static void escape(@NotNull StringBuilder builder, char c) {
    switch (c) {
      // See https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.10.7
      case '\b':
        builder.append('\\').append('b');
        return;
      case '\t':
        builder.append('\\').append('t');
        return;
      case '\n':
        builder.append('\\').append('n');
        return;
      case '\f':
        builder.append('\\').append('f');
        return;
      case '\r':
        builder.append('\\').append('r');
        return;
      case '\"':
        builder.append('\\').append('"');
        return;
      case '\'':
        builder.append('\\').append('\'');
        return;
      case '\\':
        builder.append('\\').append('\\');
        return;
    }
    if (!isVisible(c)) {
      // See https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.3
      builder.append('\\').append('u').append(String.format("%04X", (int) c));
      return;
    }
    builder.append(c);
  }

  private static boolean isVisible(char c) {
    return /*!Character.isISOControl(c) && */isInCharacterTypeMask(c, VISIBLE_CHARACTER_TYPES) ||
           c == ' ';
  }

  private static int characterTypeMask(int... types) {
    int mask = 0;
    for (int type : types) {
      assert type < 32 : type;
      mask |= (1 << type);
    }
    return mask;
  }

  private static boolean isInCharacterTypeMask(char c, int mask) {
    return ((mask >>> Character.getType(c)) & 1) != 0;
  }
}
