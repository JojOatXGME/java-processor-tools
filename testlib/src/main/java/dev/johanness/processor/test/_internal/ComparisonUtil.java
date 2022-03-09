package dev.johanness.processor.test._internal;

import dev.johanness.processor.test.mock.element.TypeElementMock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor9;
import java.util.Iterator;
import java.util.List;

import static dev.johanness.processor.ElementCast.asExecutableElement;
import static dev.johanness.processor.ElementCast.asModuleElement;
import static dev.johanness.processor.ElementCast.asPackageElement;
import static dev.johanness.processor.ElementCast.asTypeElement;
import static dev.johanness.processor.ElementCast.asTypeParameterElement;
import static dev.johanness.processor.ElementCast.asVariableElement;
import static dev.johanness.processor.TypeCast.toArrayType;
import static dev.johanness.processor.TypeCast.toDeclaredType;
import static dev.johanness.processor.TypeCast.toTypeVariable;

public final class ComparisonUtil {
  // TODO: Test!
  private ComparisonUtil() {} // This class cannot be instantiated

  public static boolean matches(@NotNull DeclaredType type, @NotNull Class<?> clazz) {
    return matches(type.asElement(), new TypeElementMock(clazz));
  }

  public static boolean matches(@NotNull DeclaredType type1, @NotNull DeclaredType type2) {
    return matches(type1.asElement(), type2.asElement());
  }

  public static boolean matches(@Nullable Element element1, @Nullable Element element2) {
    return element1 == element2 ||
           element1 != null && element2 != null && element1.accept(COMPARATOR, element2);
  }

  private static final @NotNull ElementVisitor<Boolean, Element> COMPARATOR = new AbstractElementVisitor9<>() {
    private @NotNull Boolean compare(@Nullable Element element1, @Nullable Element element2) {
      return element1 == element2 ||
             element1 != null && element2 != null && visit(element1, element2);
    }

    @Override
    public Boolean visitModule(ModuleElement e, Element otherElement) {
      ModuleElement other = asModuleElement(otherElement);
      return other != null &&
             e.getQualifiedName().contentEquals(other.getQualifiedName()) &&
             enclosingElementMatch(e, other);
    }

    @Override
    public Boolean visitPackage(PackageElement e, Element otherElement) {
      PackageElement other = asPackageElement(otherElement);
      return other != null &&
             e.getQualifiedName().contentEquals(other.getQualifiedName()) &&
             enclosingElementMatch(e, other);
    }

    @Override
    public Boolean visitType(TypeElement e, Element otherElement) {
      TypeElement other = asTypeElement(otherElement);
      if (e.getSimpleName().contentEquals("")) {
        // Anonymous class
        return e.equals(other);
      }
      else {
        return other != null &&
               e.getSimpleName().contentEquals(other.getSimpleName()) &&
               enclosingElementMatch(e, other);
      }
    }

    @Override
    public Boolean visitVariable(VariableElement e, Element otherElement) {
      VariableElement other = asVariableElement(otherElement);
      return other != null &&
             e.getKind() == other.getKind() &&
             e.getSimpleName().contentEquals(other.getSimpleName()) &&
             enclosingElementMatch(e, other);
    }

    @Override
    public Boolean visitExecutable(ExecutableElement e, Element otherElement) {
      ExecutableElement other = asExecutableElement(otherElement);
      return other != null &&
             e.getSimpleName().contentEquals(other.getSimpleName()) &&
             e.getKind() == other.getKind() &&
             typesMatch(e.getReceiverType(), other.getReceiverType()) &&
             typesMatch(e.getReturnType(), other.getReturnType()) &&
             typesMatch(e.getParameters(), other.getParameters()) &&
             enclosingElementMatch(e, other);
    }

    @Override
    public Boolean visitTypeParameter(TypeParameterElement e, Element otherElement) {
      TypeParameterElement other = asTypeParameterElement(otherElement);
      return other != null &&
             e.getKind() == other.getKind() &&
             e.getSimpleName().contentEquals(other.getSimpleName()) &&
             enclosingElementMatch(e, other);
    }

    private @NotNull Boolean typesMatch(@NotNull TypeMirror type1, @NotNull TypeMirror type2) {
      TypeKind kind = type1.getKind();
      if (type1 == type2) {
        return Boolean.TRUE;
      }
      else if (kind != type2.getKind()) {
        return Boolean.FALSE;
      }
      switch (kind) {
        case BOOLEAN:
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case CHAR:
        case FLOAT:
        case DOUBLE:
        case VOID:
        case NONE:
          return Boolean.TRUE;
        case ARRAY:
          return typesMatch(toArrayType(type1).getComponentType(), toArrayType(type2).getComponentType());
        case DECLARED:
          return compare(toDeclaredType(type1).asElement(), toDeclaredType(type2).asElement());
        case ERROR:
          return type1.toString().equals(type2.toString());
        case TYPEVAR:
          return compare(toTypeVariable(type1).asElement(), toTypeVariable(type2).asElement());
        default:
          throw new IllegalArgumentException(kind + ": " + type1);
      }
    }

    private @NotNull Boolean typesMatch(@NotNull List<? extends VariableElement> params1, @NotNull List<? extends VariableElement> params2) {
      Iterator<? extends VariableElement> iterator1 = params1.iterator();
      Iterator<? extends VariableElement> iterator2 = params2.iterator();
      while (iterator1.hasNext() && iterator2.hasNext()) {
        if (!typesMatch(iterator1.next().asType(), iterator2.next().asType())) {
          return false;
        }
      }
      return !iterator1.hasNext() && !iterator2.hasNext();
    }

    private @NotNull Boolean enclosingElementMatch(@NotNull Element element1, @NotNull Element element2) {
      return compare(element1.getEnclosingElement(), element2.getEnclosingElement());
    }
  };
}
