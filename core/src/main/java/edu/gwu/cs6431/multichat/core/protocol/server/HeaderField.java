package edu.gwu.cs6431.multichat.core.protocol.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HeaderField {
    String name();
    boolean required() default true;
    boolean inherited() default true;
}
