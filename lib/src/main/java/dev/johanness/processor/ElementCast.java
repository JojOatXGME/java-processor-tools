package dev.johanness.processor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor9;

public final class ElementCast {
  private ElementCast() {} // Cannot be instantiated

  //region Public methods

  @Contract("null -> null; !null -> _")
  public static @Nullable ModuleElement asModuleElement(@Nullable Element element) {
    return as(MODULE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable ModuleElement toModuleElement(@Nullable Element element) {
    return to(MODULE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable PackageElement asPackageElement(@Nullable Element element) {
    return as(PACKAGE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable PackageElement toPackageElement(@Nullable Element element) {
    return to(PACKAGE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable TypeElement asTypeElement(@Nullable Element element) {
    return as(TYPE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable TypeElement toTypeElement(@Nullable Element element) {
    return to(TYPE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable RecordComponentElement asRecordComponentElement(@Nullable Element element) {
    return as(RecordComponentElementVisitor.INSTANCE, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable RecordComponentElement toRecordComponentElement(@Nullable Element element) {
    return to(RecordComponentElementVisitor.INSTANCE, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable ExecutableElement asExecutableElement(@Nullable Element element) {
    return as(EXECUTABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable ExecutableElement toExecutableElement(@Nullable Element element) {
    return to(EXECUTABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable VariableElement asVariableElement(@Nullable Element element) {
    return as(VARIABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable VariableElement toVariableElement(@Nullable Element element) {
    return to(VARIABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable TypeParameterElement asTypeParameterElement(@Nullable Element element) {
    return as(TYPE_PARAMETER_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable TypeParameterElement toTypeParameterElement(@Nullable Element element) {
    return to(TYPE_PARAMETER_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable QualifiedNameable asQualifiedNameable(@Nullable Element element) {
    return as(NAMEABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable QualifiedNameable toQualifiedNameable(@Nullable Element element) {
    return to(NAMEABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> _")
  public static @Nullable Parameterizable asParameterizable(@Nullable Element element) {
    return as(PARAMETERIZABLE_VISITOR, element);
  }

  @Contract("null -> null; !null -> param1")
  public static @Nullable Parameterizable toParameterizable(@Nullable Element element) {
    return to(PARAMETERIZABLE_VISITOR, element);
  }

  //endregion
  //region Private methods

  @Contract("_, null -> null; _, !null -> _")
  private static <T> @Nullable T as(@NotNull ConversionVisitor<T> visitor, @Nullable Element element) {
    return element == null ? null : element.accept(visitor, null);
  }

  @Contract("_, _ -> param2")
  private static <T> @Nullable T to(@NotNull ConversionVisitor<T> visitor, @Nullable Element element) {
    T typed = as(visitor, element);
    if (typed == null && element != null) {
      throw new IllegalArgumentException(String.format(
          "Element of kind %s cannot be converted to %s: %s",
          element.getKind(), visitor.type.getSimpleName(), element));
    }
    return typed;
  }

  //endregion
  //region Visitor implementations

  private static abstract class ConversionVisitor<T> extends SimpleElementVisitor9<@Nullable T, Void> {
    private final @NotNull Class<T> type;

    private ConversionVisitor(@NotNull Class<T> type) {
      this.type = type;
    }
  }

  private static final @NotNull ConversionVisitor<ModuleElement> MODULE_VISITOR = new ConversionVisitor<>(ModuleElement.class) {
    @Override
    public @Nullable ModuleElement visitModule(ModuleElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<PackageElement> PACKAGE_VISITOR = new ConversionVisitor<>(PackageElement.class) {
    @Override
    public @Nullable PackageElement visitPackage(PackageElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<TypeElement> TYPE_VISITOR = new ConversionVisitor<>(TypeElement.class) {
    @Override
    public @Nullable TypeElement visitType(TypeElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<ExecutableElement> EXECUTABLE_VISITOR = new ConversionVisitor<>(ExecutableElement.class) {
    @Override
    public @Nullable ExecutableElement visitExecutable(ExecutableElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<VariableElement> VARIABLE_VISITOR = new ConversionVisitor<>(VariableElement.class) {
    @Override
    public @Nullable VariableElement visitVariable(VariableElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<TypeParameterElement> TYPE_PARAMETER_VISITOR = new ConversionVisitor<>(TypeParameterElement.class) {
    @Override
    public @Nullable TypeParameterElement visitTypeParameter(TypeParameterElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<QualifiedNameable> NAMEABLE_VISITOR = new ConversionVisitor<>(QualifiedNameable.class) {
    @Override
    public @Nullable QualifiedNameable visitModule(ModuleElement e, Void unused) {
      return e;
    }

    @Override
    public @Nullable QualifiedNameable visitPackage(PackageElement e, Void unused) {
      return e;
    }

    @Override
    public @Nullable QualifiedNameable visitType(TypeElement e, Void unused) {
      return e;
    }
  };

  private static final @NotNull ConversionVisitor<Parameterizable> PARAMETERIZABLE_VISITOR = new ConversionVisitor<>(Parameterizable.class) {
    @Override
    public @Nullable Parameterizable visitType(TypeElement e, Void unused) {
      return e;
    }

    @Override
    public @Nullable Parameterizable visitExecutable(ExecutableElement e, Void unused) {
      return e;
    }
  };

  private static final class RecordComponentElementVisitor extends ConversionVisitor<RecordComponentElement> {
    private static final @NotNull RecordComponentElementVisitor INSTANCE = new RecordComponentElementVisitor();

    private RecordComponentElementVisitor() {
      super(RecordComponentElement.class);
    }

    @Override
    public @Nullable RecordComponentElement visitRecordComponent(RecordComponentElement e, Void unused) {
      return e;
    }
  }

  //endregion
}
