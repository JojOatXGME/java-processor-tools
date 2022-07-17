package dev.johanness.processor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor9;

public final class TypeCast {
  private TypeCast() {} // Cannot be instantiated

  //region Public methods

  @Contract("null -> null; !null -> _")
  public static @Nullable IntersectionType asIntersectionType(@Nullable TypeMirror type) {
    return as(INTERSECTION_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable IntersectionType toIntersectionType(@Nullable TypeMirror type) {
    return to(INTERSECTION_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable UnionType asUnionType(@Nullable TypeMirror type) {
    return as(UNION_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable UnionType toUnionType(@Nullable TypeMirror type) {
    return to(UNION_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable ExecutableType asExecutableType(@Nullable TypeMirror type) {
    return as(EXECUTABLE_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable ExecutableType toExecutableType(@Nullable TypeMirror type) {
    return to(EXECUTABLE_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable NoType asNoType(@Nullable TypeMirror type) {
    return as(NOTYPE_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable NoType toNoType(@Nullable TypeMirror type) {
    return to(NOTYPE_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable PrimitiveType asPrimitiveType(@Nullable TypeMirror type) {
    return as(PRIMITIVE_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable PrimitiveType toPrimitiveType(@Nullable TypeMirror type) {
    return to(PRIMITIVE_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable WildcardType asWildcardType(@Nullable TypeMirror type) {
    return as(WILDCARD_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable WildcardType toWildcardType(@Nullable TypeMirror type) {
    return to(WILDCARD_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable ReferenceType asReferenceType(@Nullable TypeMirror type) {
    return as(REFERENCE_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable ReferenceType toReferenceType(@Nullable TypeMirror type) {
    return to(REFERENCE_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable ArrayType asArrayType(@Nullable TypeMirror type) {
    return as(ARRAY_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable ArrayType toArrayType(@Nullable TypeMirror type) {
    return to(ARRAY_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable NullType asNullType(@Nullable TypeMirror type) {
    return as(NULL_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable NullType toNullType(@Nullable TypeMirror type) {
    return to(NULL_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable DeclaredType asDeclaredType(@Nullable TypeMirror type) {
    return as(DECLARED_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable DeclaredType toDeclaredType(@Nullable TypeMirror type) {
    return to(DECLARED_VISITOR, type);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable TypeVariable asTypeVariable(@Nullable TypeMirror type) {
    return as(TYPE_VARIABLE_VISITOR, type);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable TypeVariable toTypeVariable(@Nullable TypeMirror type) {
    return to(TYPE_VARIABLE_VISITOR, type);
  }

  //endregion
  //region Private methods

  @Contract("_, null -> null; _, !null -> _")
  private static <T> @Nullable T as(@NotNull ConversionVisitor<T> visitor, @Nullable TypeMirror type) {
    return type == null ? null : type.accept(visitor, null);
  }

  @Contract("_, _ -> param2")
  private static <T> @Nullable T to(@NotNull ConversionVisitor<T> visitor, @Nullable TypeMirror type) {
    T typed = as(visitor, type);
    if (typed == null && type != null) {
      throw new IllegalArgumentException(String.format(
          "Type of kind %s cannot be converted to %s: %s",
          type.getKind(), visitor.type.getSimpleName(), type));
    }
    return typed;
  }

  //endregion
  //region Visitor implementations

  private static abstract class ConversionVisitor<T> extends SimpleTypeVisitor9<@Nullable T, Void> {
    private final @NotNull Class<T> type;

    private ConversionVisitor(@NotNull Class<T> type) {
      this.type = type;
    }
  }

  private static final @NotNull ConversionVisitor<IntersectionType> INTERSECTION_VISITOR = new ConversionVisitor<>(IntersectionType.class) {
    @Override
    public @Nullable IntersectionType visitIntersection(IntersectionType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<UnionType> UNION_VISITOR = new ConversionVisitor<>(UnionType.class) {
    @Override
    public @Nullable UnionType visitUnion(UnionType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<ExecutableType> EXECUTABLE_VISITOR = new ConversionVisitor<>(ExecutableType.class) {
    @Override
    public @Nullable ExecutableType visitExecutable(ExecutableType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<NoType> NOTYPE_VISITOR = new ConversionVisitor<>(NoType.class) {
    @Override
    public @Nullable NoType visitNoType(NoType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<PrimitiveType> PRIMITIVE_VISITOR = new ConversionVisitor<>(PrimitiveType.class) {
    @Override
    public @Nullable PrimitiveType visitPrimitive(PrimitiveType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<WildcardType> WILDCARD_VISITOR = new ConversionVisitor<>(WildcardType.class) {
    @Override
    public @Nullable WildcardType visitWildcard(WildcardType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<ArrayType> ARRAY_VISITOR = new ConversionVisitor<>(ArrayType.class) {
    @Override
    public @Nullable ArrayType visitArray(ArrayType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<NullType> NULL_VISITOR = new ConversionVisitor<>(NullType.class) {
    @Override
    public @Nullable NullType visitNull(NullType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<DeclaredType> DECLARED_VISITOR = new ConversionVisitor<>(DeclaredType.class) {
    @Override
    public @Nullable DeclaredType visitDeclared(DeclaredType t, Void unused) {
      return t;
    }

    @Override
    public @Nullable DeclaredType visitError(ErrorType t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<TypeVariable> TYPE_VARIABLE_VISITOR = new ConversionVisitor<>(TypeVariable.class) {
    @Override
    public @Nullable TypeVariable visitTypeVariable(TypeVariable t, Void unused) {
      return t;
    }
  };

  private static final @NotNull ConversionVisitor<ReferenceType> REFERENCE_VISITOR = new ConversionVisitor<>(ReferenceType.class) {
    @Override
    public @Nullable ReferenceType visitNull(NullType t, Void unused) {
      return t;
    }

    @Override
    public @Nullable ReferenceType visitArray(ArrayType t, Void unused) {
      return t;
    }

    @Override
    public @Nullable ReferenceType visitDeclared(DeclaredType t, Void unused) {
      return t;
    }

    @Override
    public @Nullable ReferenceType visitError(ErrorType t, Void unused) {
      return t;
    }

    @Override
    public @Nullable ReferenceType visitTypeVariable(TypeVariable t, Void unused) {
      return t;
    }
  };

  //endregion
}
