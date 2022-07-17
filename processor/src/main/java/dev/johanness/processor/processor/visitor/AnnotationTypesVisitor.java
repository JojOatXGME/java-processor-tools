package dev.johanness.processor.processor.visitor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import dev.johanness.processor.AnnotationProxy;
import dev.johanness.processor.AnnotationType;
import dev.johanness.processor.ValueType;
import dev.johanness.processor.processor.annotation.Annotations;
import dev.johanness.processor.processor.annotation.GenerateAnnotationTypes;
import dev.johanness.processor.segmented.Definitely;
import dev.johanness.processor.segmented.Key;
import dev.johanness.processor.segmented.Preliminary;
import dev.johanness.processor.segmented.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static dev.johanness.processor.AnnotationAccess.searchAnnotation;
import static dev.johanness.processor.ElementCast.asExecutableElement;
import static dev.johanness.processor.ElementCast.toTypeElement;
import static dev.johanness.processor.TypeCast.asDeclaredType;
import static dev.johanness.processor.TypeCast.toArrayType;
import static dev.johanness.processor.TypeCast.toDeclaredType;

public final class AnnotationTypesVisitor implements Visitor {
  private static final @NotNull String GENERATOR_SUFFIX = "Generator";
  private static final @NotNull Key<Collector> COLLECTOR = new Key<>("collector");

  @Override
  public @NotNull Boolean visitType(@NotNull TypeElement element, @NotNull Preliminary preliminary) {
    GenerateAnnotationTypes annotation = searchAnnotation(element, Annotations.GENERATE_ANNOTATION_TYPES);
    if (annotation == null) {
      // Set to null to avoid collecting fields of nested classes.
      preliminary.set(COLLECTOR, null);
    }
    else if (element.getQualifiedName().contentEquals("")) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Anonymous classes are not supported",
          element, annotation.javaAnnotationMirror()));
    }
    else {
      String className = annotation.className();
      if (className.isEmpty()) {
        String generatorName = element.getSimpleName().toString();
        if (generatorName.endsWith(GENERATOR_SUFFIX)) {
          className = generatorName.substring(0, generatorName.length() - GENERATOR_SUFFIX.length());
        }
        else {
          preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
              Diagnostic.Kind.ERROR,
              String.format("The name of the generator class (%s) must either end with '%s', or you must specify the target class name within the annotation.",
                  generatorName, GENERATOR_SUFFIX),
              element, annotation.javaAnnotationMirror()));
        }
      }
      Collector collector = new Collector(element, className);
      preliminary.set(COLLECTOR, collector);
      preliminary.finalize(collector, this::generateConstants);
    }
    return true;
  }

  @Override
  public @NotNull Boolean visitVariable(@NotNull VariableElement element, @NotNull Preliminary preliminary) {
    Collector collector = preliminary.getOrNull(COLLECTOR);
    if (collector == null || element.getKind() != ElementKind.FIELD) {
      return true;
    }
    TypeMirror type = element.asType();
    if (type.getKind() == TypeKind.ERROR) {
      return false;
    }
    if (type.getKind() != TypeKind.DECLARED) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid type of field. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }
    DeclaredType classType = toDeclaredType(type);
    if (!toTypeElement(classType.asElement()).getQualifiedName().contentEquals("java.lang.Class")) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid type of field. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }
    if (classType.getTypeArguments().isEmpty()) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid annotation type. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }
    if (classType.getTypeArguments().isEmpty()) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid annotation type. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }
    DeclaredType annotationType = asDeclaredType(classType.getTypeArguments().get(0));
    if (annotationType == null) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid annotation type. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }
    if (annotationType.getKind() == TypeKind.ERROR) {
      return false;
    }
    TypeElement annotationElement = toTypeElement(annotationType.asElement());
    if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
      preliminary.action(null, (item, definitely) -> definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "Invalid annotation type. You must use Class<A>, with A being any annotation type.",
          element));
      return true;
    }

    AnnotationData annotationData = new AnnotationData(
        element.getSimpleName().toString(),
        annotationElement,
        preliminary.elementUtils().getPackageOf(collector.generatorElement));
    preliminary.action(annotationData, this::generateAnnotationType);
    collector.fields.add(annotationData);
    return true;
  }

  private void generateAnnotationType(@NotNull AnnotationData data, @NotNull Definitely definitely) {
    ClassName className = getProxyClassName(data.destinationPackage, data.annotationElement);

    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .superclass(AnnotationProxy.class)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        // TODO: Add @Generated annotation if available
        .addJavadoc("Annotation type for {@link $T}.", data.annotationElement)
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(AnnotationMirror.class, "mirror")
            .addStatement("super(mirror)")
            .build());

    for (Element enclosedElement : data.annotationElement.getEnclosedElements()) {
      ExecutableElement method = asExecutableElement(enclosedElement);
      if (method == null) {
        continue;
      }
      String name = method.getSimpleName().toString();
      ReturnType returnType = getTypeInfo(method.getReturnType());
      builder
          .addField(FieldSpec.builder(returnType.returnType.box(), name)
              .addModifiers(Modifier.PRIVATE)
              .build())
          .addMethod(MethodSpec.methodBuilder(name)
              .returns(returnType.returnType)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("See {@link $T#$L $L} on annotation interface.", data.annotationElement, method, method)
              .addStatement("this.$N = readValue(this.$N, $S, $L)", name, name, name, returnType.valueTypeMethod)
              .addStatement("return this.$N", name)
              .build());
    }

    writeFile(definitely, data.destinationPackage, builder.build(),
        definitely.get(COLLECTOR).generatorElement, data.annotationElement);
  }

  private boolean generateConstants(@NotNull Collector collector, @NotNull Preliminary preliminary) {
    preliminary.action(collector, this::generateConstants);
    return true;
  }

  private void generateConstants(@NotNull Collector collector, @NotNull Definitely definitely) {
    PackageElement packageElement = definitely.processingEnv().getElementUtils().getPackageOf(collector.generatorElement);
    ClassName className = ClassName.get(packageElement.getQualifiedName().toString(), collector.targetClassName);

    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        // TODO: Add @Generated annotation if available
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addComment("This class cannot be instantiated.")
            .build());

    for (AnnotationData field : collector.fields) {
      ClassName proxyType = getProxyClassName(packageElement, field.annotationElement);
      builder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(AnnotationType.class), proxyType), field.fieldName)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .addJavadoc("Annotation type for {@link $T}.", field.annotationElement)
          .initializer("new $T<>($Z$S,$W$S,$W$T::new)",
              className.nestedClass("Type"),
              definitely.processingEnv().getElementUtils().getBinaryName(field.annotationElement),
              getModuleName(definitely.processingEnv().getElementUtils(), field.annotationElement),
              proxyType)
          .build());
    }

    TypeVariableName typeVariable = TypeVariableName.get("P");
    TypeName factoryType = ParameterizedTypeName.get(ClassName.get(Function.class), ClassName.get(AnnotationMirror.class), typeVariable);
    builder.addType(TypeSpec.classBuilder("Type")
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .addTypeVariable(typeVariable)
        .superclass(ParameterizedTypeName.get(ClassName.get(AnnotationType.class), typeVariable))
        .addField(FieldSpec.builder(factoryType, "proxyFactory")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(String.class, "binaryName")
            .addParameter(String.class, "moduleName")
            .addParameter(factoryType, "proxyFactory")
            .addStatement("super(binaryName, moduleName)")
            .addStatement("this.proxyFactory = proxyFactory")
            .build())
        .addMethod(MethodSpec.methodBuilder("createProxy")
            .returns(typeVariable)
            .addModifiers(Modifier.PROTECTED)
            .addAnnotation(Override.class)
            .addParameter(AnnotationMirror.class, "mirror")
            .addStatement("return proxyFactory.apply(mirror)")
            .build())
        .build());

    writeFile(definitely, packageElement, builder.build(),
        collector.generatorElement);
  }

  private static @NotNull ClassName getProxyClassName(@NotNull PackageElement packageElement, @NotNull TypeElement annotationElement) {
    return ClassName.get(
        packageElement.getQualifiedName().toString(),
        annotationElement.getSimpleName().toString());
  }

  private static @Nullable String getModuleName(@NotNull Elements elementUtils, @NotNull TypeElement annotationElement) {
    ModuleElement moduleElement = elementUtils.getModuleOf(annotationElement);
    if (moduleElement == null) return null;
    String moduleName = moduleElement.getQualifiedName().toString();
    return moduleName.isEmpty() ? null : moduleName;
  }

  private static @NotNull ReturnType getTypeInfo(@NotNull TypeMirror returnType) {
    switch (returnType.getKind()) {
      case BOOLEAN:
        return new ReturnType(TypeName.BOOLEAN, CodeBlock.of("$T.bool()", ValueType.class));
      case BYTE:
        return new ReturnType(TypeName.BYTE, CodeBlock.of("$T.byte_()", ValueType.class));
      case SHORT:
        return new ReturnType(TypeName.SHORT, CodeBlock.of("$T.short_()", ValueType.class));
      case INT:
        return new ReturnType(TypeName.INT, CodeBlock.of("$T.integer()", ValueType.class));
      case LONG:
        return new ReturnType(TypeName.LONG, CodeBlock.of("$T.long_()", ValueType.class));
      case FLOAT:
        return new ReturnType(TypeName.FLOAT, CodeBlock.of("$T.float_()", ValueType.class));
      case DOUBLE:
        return new ReturnType(TypeName.DOUBLE, CodeBlock.of("$T.double_()", ValueType.class));
      case CHAR:
        return new ReturnType(TypeName.CHAR, CodeBlock.of("$T.character()", ValueType.class));
      case ARRAY: {
        ReturnType component = getTypeInfo(toArrayType(returnType).getComponentType());
        return new ReturnType(
            ParameterizedTypeName.get(ClassName.get(List.class), component.returnType.box()),
            CodeBlock.of("$T.array($L)", ValueType.class, component.valueTypeMethod));
      }
      case ERROR:
        // TODO: Handle this case
        throw new IllegalStateException("Type could not be resolved");
      case DECLARED: {
        TypeElement element = toTypeElement(toDeclaredType(returnType).asElement());
        String qualifiedName = element.getQualifiedName().toString();
        if (qualifiedName.equals("java.lang.String")) {
          return new ReturnType(ClassName.get(String.class), CodeBlock.of("$T.string()", ValueType.class));
        }
        else if (qualifiedName.equals("java.lang.Class")) {
          return new ReturnType(ClassName.get(TypeMirror.class), CodeBlock.of("$T.class_()", ValueType.class));
        }
        else if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
          // TODO: Return the correct proxy
          return new ReturnType(ClassName.get(AnnotationMirror.class), CodeBlock.of("$T.annotation()", ValueType.class));
        }
        else if (element.getKind() == ElementKind.ENUM) {
          // TODO: Return the enum itself if it is available in the classpath
          return new ReturnType(ClassName.get(VariableElement.class), CodeBlock.of("$T.enum_()", ValueType.class));
        }
      }
      default:
        throw new IllegalStateException("Unknown type for annotation method");
    }
  }

  private static void writeFile(@NotNull Definitely definitely, @NotNull PackageElement packageElement, @NotNull TypeSpec typeSpec, @NotNull Element... originatingElements) {
    ModuleElement moduleElement = definitely.processingEnv().getElementUtils().getModuleOf(packageElement);
    String moduleName = moduleElement == null ? "" : moduleElement.getQualifiedName().toString();
    String packageName = packageElement.getQualifiedName().toString();
    String moduleAndPackage = moduleName.isEmpty() ? packageName : moduleName + '/' + packageName;

    try {
      JavaFileObject sourceFile = definitely.filer().createSourceFile(
          moduleAndPackage + '.' + typeSpec.name,
          originatingElements);
      try (Writer writer = sourceFile.openWriter()) {
        JavaFile.builder(packageName, typeSpec)
            .indent("  ")
            .build().writeTo(writer);
      }
      catch (Throwable t) {
        try {
          sourceFile.delete();
        }
        catch (Exception ignored) {
          // ignore
        }
        throw t;
      }
    }
    catch (IOException e) {
      // TODO: How to handle this?
      definitely.messager().printMessage(
          Diagnostic.Kind.ERROR,
          "IOException: " + e.getLocalizedMessage());
    }
  }

  private static final class Collector {
    private final @NotNull TypeElement generatorElement;
    private final @NotNull String targetClassName;
    private final List<AnnotationData> fields = new ArrayList<>();

    private Collector(@NotNull TypeElement generatorElement,
                      @NotNull String targetClassName)
    {
      this.generatorElement = generatorElement;
      this.targetClassName = targetClassName;
    }
  }

  private static final class AnnotationData {
    private final @NotNull String fieldName;
    private final @NotNull TypeElement annotationElement;
    private final @NotNull PackageElement destinationPackage;

    private AnnotationData(@NotNull String fieldName,
                           @NotNull TypeElement annotationElement,
                           @NotNull PackageElement destinationPackage)
    {
      this.fieldName = fieldName;
      this.annotationElement = annotationElement;
      this.destinationPackage = destinationPackage;
    }
  }

  private static final class ReturnType {
    private final @NotNull TypeName returnType;
    private final @NotNull CodeBlock valueTypeMethod;

    private ReturnType(@NotNull TypeName returnType, @NotNull CodeBlock valueTypeMethod) {
      this.returnType = returnType;
      this.valueTypeMethod = valueTypeMethod;
    }
  }
}
