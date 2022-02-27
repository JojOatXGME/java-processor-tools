package dev.johanness.processor;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ValueType<T> {

  private ValueType() {} // Only inherited by nested classes

  abstract @NotNull T convert(@NotNull AnnotationValue value);

  //region Public methods

  public static @NotNull ValueType<AnnotationValue> untyped() {
    return Constants.NOOP;
  }

  public static @NotNull ValueType<Boolean> bool() {
    return Constants.BOOLEAN;
  }

  public static @NotNull ValueType<Byte> byte_() {
    return Constants.BYTE;
  }

  public static @NotNull ValueType<Short> short_() {
    return Constants.SHORT;
  }

  public static @NotNull ValueType<Integer> integer() {
    return Constants.INTEGER;
  }

  public static @NotNull ValueType<Long> long_() {
    return Constants.LONG;
  }

  public static @NotNull ValueType<Float> float_() {
    return Constants.FLOAT;
  }

  public static @NotNull ValueType<Double> double_() {
    return Constants.DOUBLE;
  }

  public static @NotNull ValueType<Character> character() {
    return Constants.CHARACTER;
  }

  public static @NotNull ValueType<String> string() {
    return Constants.STRING;
  }

  public static @NotNull ValueType<TypeMirror> class_() {
    return Constants.CLASS;
  }

  public static @NotNull ValueType<VariableElement> enum_() {
    return Constants.ENUM;
  }

  public static <T extends Enum<T>> @NotNull ValueType<T> enum_(@NotNull Class<T> type) {
    return new EnumType<>(type);
  }

  public static @NotNull ValueType<AnnotationMirror> annotation() {
    return Constants.ANNOTATION;
  }

  public static <T> @NotNull ValueType<T> annotation(@NotNull AnnotationType<T> type) {
    return new AnnotationTypeWrapper<>(type);
  }

  public static @NotNull ValueType<List<AnnotationValue>> array() {
    return Constants.ARRAY;
  }

  public static <T> @NotNull ValueType<List<T>> array(@NotNull ValueType<T> itemType) {
    return new ArrayType<>(itemType);
  }

  //endregion

  private static final class Constants {
    private static final ValueType<AnnotationValue> NOOP = new NoneType();

    private static final SimpleType<Boolean> BOOLEAN = new SimpleType<>(new ConversionVisitor<>("boolean") {
      @Override
      public Boolean visitBoolean(boolean b, AnnotationValue unused) {
        return b;
      }
    });

    private static final SimpleType<Byte> BYTE = new SimpleType<>(new ConversionVisitor<>("byte") {
      @Override
      public Byte visitByte(byte b, AnnotationValue unused) {
        return b;
      }
    });

    private static final SimpleType<Short> SHORT = new SimpleType<>(new ConversionVisitor<>("short") {
      @Override
      public Short visitByte(byte b, AnnotationValue unused) {
        return visitShort(b, unused);
      }

      @Override
      public Short visitShort(short s, AnnotationValue unused) {
        return s;
      }
    });

    private static final SimpleType<Integer> INTEGER = new SimpleType<>(new ConversionVisitor<>("integer") {
      @Override
      public Integer visitByte(byte b, AnnotationValue unused) {
        return visitInt(b, unused);
      }

      @Override
      public Integer visitShort(short s, AnnotationValue unused) {
        return visitInt(s, unused);
      }

      @Override
      public Integer visitInt(int i, AnnotationValue unused) {
        return i;
      }
    });

    private static final SimpleType<Long> LONG = new SimpleType<>(new ConversionVisitor<>("long") {
      @Override
      public Long visitByte(byte b, AnnotationValue unused) {
        return visitLong(b, unused);
      }

      @Override
      public Long visitShort(short s, AnnotationValue unused) {
        return visitLong(s, unused);
      }

      @Override
      public Long visitInt(int i, AnnotationValue unused) {
        return visitLong(i, unused);
      }

      @Override
      public Long visitLong(long i, AnnotationValue unused) {
        return i;
      }
    });

    private static final SimpleType<Float> FLOAT = new SimpleType<>(new ConversionVisitor<>("float") {
      @Override
      public Float visitFloat(float f, AnnotationValue unused) {
        return f;
      }
    });

    private static final SimpleType<Double> DOUBLE = new SimpleType<>(new ConversionVisitor<>("double") {
      @Override
      public Double visitFloat(float f, AnnotationValue unused) {
        return visitDouble(f, unused);
      }

      @Override
      public Double visitDouble(double d, AnnotationValue unused) {
        return d;
      }
    });

    private static final SimpleType<Character> CHARACTER = new SimpleType<>(new ConversionVisitor<>("character") {
      @Override
      public Character visitChar(char c, AnnotationValue unused) {
        return c;
      }
    });

    private static final SimpleType<String> STRING = new SimpleType<>(new ConversionVisitor<>("string") {
      @Override
      public String visitString(String s, AnnotationValue unused) {
        return s;
      }
    });

    private static final SimpleType<TypeMirror> CLASS = new SimpleType<>(new ConversionVisitor<>("class") {
      @Override
      public TypeMirror visitType(TypeMirror t, AnnotationValue unused) {
        return t;
      }
    });

    private static final SimpleType<VariableElement> ENUM = new SimpleType<>(new ConversionVisitor<>("enum constant") {
      @Override
      public VariableElement visitEnumConstant(VariableElement c, AnnotationValue unused) {
        return c;
      }
    });

    private static final SimpleType<AnnotationMirror> ANNOTATION = new SimpleType<>(new ConversionVisitor<>("annotation") {
      @Override
      public AnnotationMirror visitAnnotation(AnnotationMirror a, AnnotationValue unused) {
        return a;
      }
    });

    private static final SimpleType<List<AnnotationValue>> ARRAY = new SimpleType<>(new ConversionVisitor<>("array") {
      // Note: There is a second array visitor implementation in ValueType.ArrayVisitor.
      @Override
      protected List<AnnotationValue> defaultAction(Object o, AnnotationValue origin) {
        return visitArray(List.of(origin), origin);
      }

      @Override
      public List<AnnotationValue> visitArray(List<? extends AnnotationValue> vals, AnnotationValue unused) {
        return List.copyOf(vals);
      }
    });
  }

  //region Internal type classes

  private static final class NoneType extends ValueType<AnnotationValue> {
    @Override
    @NotNull AnnotationValue convert(@NotNull AnnotationValue value) {
      return value;
    }

    @Override
    public String toString() {
      return "NoneType()";
    }
  }

  private static final class SimpleType<T> extends ValueType<T> {
    private final @NotNull ConversionVisitor<T> visitor;

    private SimpleType(@NotNull ConversionVisitor<T> visitor) {
      this.visitor = visitor;
    }

    @Override
    @NotNull T convert(@NotNull AnnotationValue value) {
      return value.accept(visitor, value);
    }

    @Override
    public String toString() {
      return "SimpleType(" + visitor.typeName + ")";
    }
  }

  private static final class EnumType<T extends Enum<T>> extends ValueType<T> {
    private final @NotNull Class<T> type;

    private EnumType(@NotNull Class<T> type) {
      this.type = type;
    }

    @Override
    @NotNull T convert(@NotNull AnnotationValue value) {
      VariableElement element = Constants.ENUM.convert(value);
      // TODO: Validated that `element` actually belongs to `type`
      return Enum.valueOf(type, element.getSimpleName().toString());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EnumType<?> enumType = (EnumType<?>) o;
      return type.equals(enumType.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type);
    }

    @Override
    public String toString() {
      return "EnumType(" + type + ")";
    }
  }

  private static final class AnnotationTypeWrapper<P> extends ValueType<P> {
    private final @NotNull AnnotationType<P> type;

    private AnnotationTypeWrapper(@NotNull AnnotationType<P> type) {
      this.type = type;
    }

    @Override
    @NotNull P convert(@NotNull AnnotationValue value) {
      AnnotationMirror mirror = Constants.ANNOTATION.convert(value);
      return type.proxy(mirror);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      AnnotationTypeWrapper<?> that = (AnnotationTypeWrapper<?>) o;
      return type.equals(that.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type);
    }

    @Override
    public String toString() {
      return "AnnotationType(" + type.nameWithModule() + ")";
    }
  }

  private static final class ArrayType<T> extends ValueType<List<T>> {
    private final @NotNull ArrayVisitor<T> visitor;

    private ArrayType(@NotNull ValueType<T> itemType) {
      this.visitor = new ArrayVisitor<>(itemType);
    }

    @Override
    @NotNull List<T> convert(@NotNull AnnotationValue value) {
      return value.accept(visitor, value);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ArrayType<?> arrayType = (ArrayType<?>) o;
      return visitor.itemType.equals(arrayType.visitor.itemType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(visitor.itemType);
    }

    @Override
    public String toString() {
      ValueType<T> itemType = visitor.itemType;
      String itemStr;
      if (itemType instanceof SimpleType) {
        itemStr = ((SimpleType<?>) itemType).visitor.typeName;
      }
      else if (itemType instanceof EnumType) {
        itemStr = ((EnumType<?>) itemType).type.toString();
      }
      else if (itemType instanceof AnnotationTypeWrapper) {
        itemStr = ((AnnotationTypeWrapper<T>) itemType).type.nameWithModule();
      }
      else {
        itemStr = itemType.toString();
      }
      return "ArrayType(" + itemStr + ")";
    }
  }

  //endregion
  //region Internal visitor classes

  private static abstract class ConversionVisitor<T> extends SimpleAnnotationValueVisitor9<T, AnnotationValue> {
    private final @NotNull String typeName;

    private ConversionVisitor(@NotNull String typeName) {
      this.typeName = typeName;
    }

    @Override
    protected T defaultAction(Object o, AnnotationValue unused) {
      throw new IllegalArgumentException("Not a valid " + typeName + ": " + o);
    }
  }

  private static final class ArrayVisitor<T> extends SimpleAnnotationValueVisitor9<List<T>, AnnotationValue> {
    // Note: There is a second array visitor implementation in ValueType.Constants.ARRAY.
    private final @NotNull ValueType<T> itemType;

    private ArrayVisitor(@NotNull ValueType<T> itemType) {
      this.itemType = itemType;
    }

    @Override
    protected List<T> defaultAction(Object o, AnnotationValue origin) {
      return visitArray(List.of(origin), origin);
    }

    @Override
    public List<T> visitArray(List<? extends AnnotationValue> vals, AnnotationValue unused) {
      return vals.stream().map(itemType::convert).collect(Collectors.toUnmodifiableList());
    }
  }

  //endregion
}
