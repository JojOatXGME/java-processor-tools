package dev.johanness.processor.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateAnnotationTypes {
  String className() default "";
}
